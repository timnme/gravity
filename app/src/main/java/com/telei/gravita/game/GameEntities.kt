package com.telei.gravita.game

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import com.telei.gravita.*
import kotlin.math.pow
import kotlin.math.sqrt

private const val G: Double = 6.67408E-11 // gravitational constant
private const val MPDP = 5000f // meters per dp

data class Distance(var xD: Float, var yD: Float) {
    constructor(x1: Float, x2: Float, y1: Float, y2: Float) : this(x1 - x2, y1 - y2)

    fun calculate(): Float = sqrt(xD * xD + yD * yD)

    fun scale(factor: Float) {
        xD *= factor
        yD *= factor
    }
}

var levels = false

sealed class Body {
    protected abstract var xR: Float // 0..1 x position
    protected abstract var yR: Float // 0..1 y position
    protected abstract var haloF: Float

    var vX: Float = 0f              // pixels/s
    var vY: Float = 0f             // pixels/s

    var x0: Float = 0f
    var y0: Float = 0f
    var r: Float = 0f
    var x: Float = 0f
        set(value) {
            prevX = field
            field = value
            if (value < 0f || value > width) {
                vX = -vX
            }
        }
    var y: Float = 0f
        set(value) {
            prevY = field
            field = value
            if (value < 0f || value > height) {
                vY = -vY
            }
        }
    var prevX: Float = x
    var prevY: Float = y
    var haloR: Float = 0f
        protected set

    private var width = 0f
    private var height = 0f

    open fun init(width: Int, height: Int) {
        this.width = width.toFloat()
        this.height = height.toFloat()
        x0 = width * xR
        y0 = height * yR
        r = when (this) {
            is Aim -> aimSize
            is Point -> pointSize
            is Attractor -> attractorSize
            is Portal -> portalSize
            is Chord -> 0f
        } / if (levels) 5f else 1f
        haloR = r * haloF
        x = x0
        y = y0
    }

    protected fun distance(other: Body): Distance = Distance(x - other.x, y - other.y)

    open infix fun reached(other: Body): Boolean = distance(other).calculate() < other.r

    open fun normalize(width: Int, height: Int) {
        xR = x / width
        yR = y / height
    }

    open fun reset() {

    }

    open fun draw(canvas: Canvas) {
        if (this !is Portal) {
            canvas.drawCircle(
                x, y, r, when (this) {
                    is Aim -> aimPaint
                    is Point -> pointPaint
                    is Attractor, is Portal, is Chord -> throw Exception()
                }
            )
        }
    }
}

data class Aim(
    override var xR: Float,
    override var yR: Float,
    override var m: Double = 1.0,
    override var haloF: Float = 4f,
    override var attractable: Boolean = true
) : Massive()

sealed class Massive : Body() {
    abstract var m: Double              // kg
    abstract var attractable: Boolean

    private val ppdp = pixelsPerDip

    fun attract(other: Massive, time: Float) {
        if (other.attractable || this.attractable) {
            val distance = distance(other)
            val scaleFactor = MPDP / ppdp // TODO: 7/12/2020 optimize
            distance.scale(scaleFactor)
            val d = distance.calculate()
            val scaledR = r * scaleFactor
            val effectiveM = if (d < scaledR) 0.0 else m
            val force = G * effectiveM * other.m / (d * d)
            val unitForce = force / d
            val forceX = unitForce * distance.xD
            val forceY = unitForce * distance.yD
            other.accelerate(forceX, forceY, time)
            this.accelerate(-forceX, -forceY, time)
        }
    }

    private fun accelerate(forceX: Double, forceY: Double, time: Float) {
        if (attractable) {
            val accelerationX = (forceX / m).toFloat()
            val accelerationY = (forceY / m).toFloat()
            vX += accelerationX * time
            vY += accelerationY * time
        }
    }

    fun move(time: Float) {
        x += vX * time
        y += vY * time
    }

    override fun reset() {
        super.reset()
        x = x0
        y = y0
        vX = 0f
        vY = 0f
    }
}

data class Point(
    override var xR: Float,
    override var yR: Float,
    override var haloF: Float = 8f,
    override var m: Double = 1.0,
    override var attractable: Boolean = true
) : Massive()

data class Attractor(
    var id: Int = 0,
    var attracting: Boolean = true,
    override var xR: Float = 0f,
    override var yR: Float = 0f,
    override var haloF: Float = 4f,
    override var m: Double = M,
    override var attractable: Boolean = false
) : Massive() {
    companion object {
        private const val M: Double = 2E23 // Attractor unit mass

        const val MAX_F = 6f
    }

    private val tracePaint = createPaint(pointColor).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f // TODO: 7/10/2020
//        pathEffect = DashPathEffect(floatArrayOf(7f, 4f), 0f)
    }

    private val path = Path()

    var f: Int = 1
        set(value) {
            field = value
            scale()
        }

    override fun draw(canvas: Canvas) {
//        path.lineTo(x, y)
        canvas.drawCircle(x, y, r, if (attracting) attractorPaint else repulsorPaint)
//        canvas.drawPath(path, tracePaint)
    }

    override fun init(width: Int, height: Int) {
        super.init(width, height)
        scale()
//        path.moveTo(x, y)
    }

    private fun scale() {
        m = M * f * if (attracting) 1f else -1f
        r = attractorSize * (1f + f / MAX_F) / if (levels) 5f else 1f
        haloR = r * haloF
    }

    override fun reset() {
        super.reset()
//        path.rewind()
//        path.moveTo(x, y)
    }
}

sealed class Dual : Body() {
    abstract var xR2: Float
    abstract var yR2: Float
    abstract var x2: Float
    abstract var y2: Float

    override fun init(width: Int, height: Int) {
        super.init(width, height)
        x2 = width * xR2
        y2 = height * yR2
    }

    override fun normalize(width: Int, height: Int) {
        super.normalize(width, height)
        xR2 = x2 / width
        yR2 = y2 / height
    }
}

data class Portal(
    override var xR2: Float = 0f,
    override var yR2: Float = 0f,
    override var x2: Float = 0f,
    override var y2: Float = 0f,
    override var xR: Float = 0f,
    override var yR: Float = 0f,
    override var haloF: Float = 4f
) : Dual() {
    private var portedThroughExit: Body? = null
    private var portedThroughEnter: Body? = null

    override fun reset() {
        super.reset()
        portedThroughEnter = null
        portedThroughExit = null
    }

    override fun draw(canvas: Canvas) {
        canvas.drawCircle(x, y, r, portalEnterPaint)
        canvas.drawCircle(x2, y2, r, portalExitPaint)
    }

    fun port(entity: Body): Boolean {
        if (entity != portedThroughEnter && reachedEnter(entity)) {
            entity.x = x2
            entity.y = y2
            portedThroughEnter = null
            portedThroughExit = entity
            return true
        } else if (entity != portedThroughExit && reachedExit(entity)) {
            entity.x = x
            entity.y = y
            portedThroughExit = null
            portedThroughEnter = entity
            return true
        }
        return false
    }

    private fun reachedEnter(other: Body) = Distance(x, other.x, y, other.y).calculate() < r

    private fun reachedExit(other: Body) = Distance(x2, other.x, y2, other.y).calculate() < r
}

data class Chord(
    override var xR2: Float = 0f,
    override var yR2: Float = 0f,
    override var x2: Float = 0f,
    override var y2: Float = 0f,
    override var xR: Float = 0f,
    override var yR: Float = 0f,
    override var haloF: Float = 4f
) : Dual() {
    private val path: Path = Path()

    override fun init(width: Int, height: Int) {
        super.init(width, height)
        path.rewind()
        path.moveTo(x, y)
        path.lineTo(x2, y2)
    }

    override fun draw(canvas: Canvas) {
        canvas.drawPath(path, chordPaint)
    }

    fun receive(body: Massive) {
//        val d1 = calc(body.prevX, body.prevY)
//        val d2 = calc(body.x, body.y)
//        if (d1.xD < 0 && (d2.xD >= 0)) {
//            body.vX = -(body.vX + 200f)
//            body.vY = -(body.vY + 200f)
//        }
        if (body.prevX in x..x2 && body.prevY in y..y2) {
            val d01 = Distance(body.prevX, x, body.prevY, y2).calculate()
            val d02 = Distance(body.prevX, x2, body.prevY, y).calculate()
            val d1 = Distance(body.x, x, body.y, y2).calculate()
            val d2 = Distance(body.x, x2, body.y, y2).calculate()
            if ((d01 < d02) && (d1 >= d2)) {
                body.vX = -(body.vX + 200f)
                body.vY = -(body.vY + 200f)
            }
        }

    }

    private fun calc(bodyX: Float, bodyY: Float): Distance {
        val a2 = y - y2
        val a1 = x - x2
        val tgA = a2 / a1
        val cosA = 1 / sqrt(1 + tgA.pow(2))
        val c2 = sqrt((bodyX - x2).pow(2) + (bodyY - y2).pow(2))
        val c1 = sqrt(a2.pow(2) + a1.pow(2))
        val h2 = c2 * cosA
        val h1 = c1 - h2
        val b2 = h1 / sqrt(1 + tgA.pow(2))
        val b1 = b2 * tgA
        val x3 = x + b2
        val y3 = y - b1
        return Distance(bodyX, x3, bodyY, y3)
    }
}