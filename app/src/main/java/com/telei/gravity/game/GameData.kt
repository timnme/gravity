package com.telei.gravity.game

import android.graphics.Canvas
import com.telei.gravity.*
import kotlin.math.sqrt

private const val G: Double = 6.67408E-11 // gravitational constant
private const val MPDP = 10000f // meters per dp

data class GameData(
    val aim: Aim,
    val point: Point,
    val attractors: List<Attractor> = emptyList(),
    val portals: List<Portal> = emptyList()
) {
    fun clone(): GameData = GameData(
        aim = aim.copy(),
        point = point.copy(),
        attractors = attractors.map { it.copy().also { attractor -> attractor.f = it.f } },
        portals = portals.map { it.copy() }
    )
}

data class Distance(var xD: Float, var yD: Float) {
    constructor(x1: Float, x2: Float, y1: Float, y2: Float) : this(x1 - x2, y1 - y2)

    fun calculate(): Float = sqrt(xD * xD + yD * yD)

    fun scale(factor: Float) {
        xD *= factor
        yD *= factor
    }
}

sealed class GameEntity {
    protected abstract var xR: Float // 0..1 x position
    protected abstract var yR: Float // 0..1 y position
    protected abstract var haloF: Float

    var x0: Float = 0f
    var y0: Float = 0f
    var r: Float = 0f
    var x: Float = 0f
    var y: Float = 0f
    var haloR: Float = 0f
        protected set

    open fun init(width: Int, height: Int) {
        x0 = width * xR
        y0 = height * yR
        r = when (this) {
            is Aim -> aimSize
            is Point -> pointSize
            is Attractor -> attractorSize
            is Portal -> portalSize
        }
        haloR = r * haloF
        x = x0
        y = y0
    }

    protected fun distance(other: GameEntity): Distance = Distance(x - other.x, y - other.y)

    open infix fun reached(other: GameEntity): Boolean = distance(other).calculate() < other.r

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
                    is Attractor -> attractorPaint
                    is Portal -> throw Exception()
                }
            )
        }
    }
}

data class Aim(
    override var xR: Float,
    override var yR: Float,
    override var haloF: Float = 1.5f
) : GameEntity()

sealed class Body : GameEntity() {
    abstract var m: Double              // kg
    abstract var vX: Float              // pixels/s
    abstract var vY: Float              // pixels/s
    abstract var attractable: Boolean

    private val ppdp = pixelsPerDip

    fun attract(other: Body, time: Float) {
        if (other.attractable || this.attractable) {
            val distance = distance(other)
            val scaleFactor = MPDP / ppdp
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
    override var vX: Float = 0f,
    override var vY: Float = 0f,
    override var attractable: Boolean = true
) : Body()

data class Attractor(
    var id: Int = 0,
    override var xR: Float = 0f,
    override var yR: Float = 0f,
    override var haloF: Float = 4f,
    override var m: Double = M,
    override var vX: Float = 0f,
    override var vY: Float = 0f,
    override var attractable: Boolean = false
) : Body() {
    companion object {
        private const val M: Double = 1E24 // Attractor unit mass

        const val MAX_F = 6f
    }

    var f: Int = 1
        set(value) {
            field = value
            scale()
        }

    override fun init(width: Int, height: Int) {
        super.init(width, height)
        scale()
    }

    private fun scale() {
        m = M * f
        r = attractorSize * (1f + f / MAX_F)
        haloR = r * haloF
    }
}

data class Portal(
    var xR2: Float = 0f,
    var yR2: Float = 0f,
    var x2: Float = 0f,
    var y2: Float = 0f,
    override var xR: Float = 0f,
    override var yR: Float = 0f,
    override var haloF: Float = 4f
) : GameEntity() {
    private var portedThroughExit: GameEntity? = null
    private var portedThroughEnter: GameEntity? = null

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

    override fun reset() {
        super.reset()
        portedThroughEnter = null
        portedThroughExit = null
    }

    override fun draw(canvas: Canvas) {
        canvas.drawCircle(x, y, r, portalEnterPaint)
        canvas.drawCircle(x2, y2, r, portalExitPaint)
    }

    fun tryPort(other: GameEntity): Boolean {
        if (other != portedThroughEnter && reachedEnter(other)) {
            other.x = x2
            other.y = y2
            portedThroughEnter = null
            portedThroughExit = other
            return true
        } else if (other != portedThroughExit && reachedExit(other)) {
            other.x = x
            other.y = y
            portedThroughExit = null
            portedThroughEnter = other
            return true
        }
        return false
    }

    private fun reachedEnter(other: GameEntity) = Distance(x, other.x, y, other.y).calculate() < r

    private fun reachedExit(other: GameEntity) = Distance(x2, other.x, y2, other.y).calculate() < r
}