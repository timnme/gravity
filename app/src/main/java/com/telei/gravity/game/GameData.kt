package com.telei.gravity.game

import android.util.TypedValue
import com.telei.gravity.App
import com.telei.gravity.R
import kotlin.math.sqrt

private const val M: Double = 1E25        // Attractor unit mass
private const val G: Double = 6.67408E-11 // gravitational constant
private const val MPDP = 10000f           // meters per dp

private val pixelsPerDip: Float by lazy {
    TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, 1f, App.context.resources.displayMetrics
    )
}
private val pointSize: Float by lazy { App.context.resources.getDimension(R.dimen.point) }
private val aimSize: Float by lazy { App.context.resources.getDimension(R.dimen.aim) }
private val attractorSize: Float by lazy { App.context.resources.getDimension(R.dimen.attractor) }

data class GameData(
    val aim: Aim,
    val point: Point,
    val attractors: List<Attractor> = emptyList(),
    val portals: List<Portal> = emptyList()
)

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
    protected abstract val haloRatio: Float

    var x0: Float = 0f
    var y0: Float = 0f
    var r: Float = 0f
    var x: Float = 0f
    var y: Float = 0f
    var haloR: Float = 0f
        private set

    fun init(width: Int, height: Int) {
        x0 = width * xR
        y0 = height * yR
        r = when (this) {
            is Aim -> aimSize
            is Point -> pointSize
            is Attractor -> attractorSize
            is Portal -> attractorSize
        }
        haloR = r * haloRatio
        x = x0
        y = y0
    }

    protected fun distance(other: GameEntity): Distance = Distance(x - other.x, y - other.y)

    infix fun reached(other: GameEntity): Boolean = distance(other).calculate() < other.r

    fun normalize(width: Int, height: Int) {
        xR = x / width
        yR = y / height
    }

}

data class Aim(
    override var xR: Float,
    override var yR: Float,
    override val haloRatio: Float = 1.5f
) : GameEntity()

sealed class Body : GameEntity() {
    abstract val m: Double              // kg
    abstract var vX: Float              // pixels/s
    abstract var vY: Float              // pixels/s
    abstract val attractable: Boolean

    private val ppdp = pixelsPerDip

    fun attract(body: Body, time: Float) {
        if (body.attractable || attractable) {
            val distance = distance(body)
            distance.scale(MPDP / ppdp)
            val d = distance.calculate()
            val force = G * m * body.m / (d * d)
            val unitForce = force / d
            val forceX = unitForce * distance.xD
            val forceY = unitForce * distance.yD
            if (body.attractable) {
                val accelerationX = (forceX / body.m).toFloat()
                val accelerationY = (forceY / body.m).toFloat()
                body.move(accelerationX, accelerationY, time)
            }
            if (attractable) {
                val accelerationX = -(forceX / m).toFloat()
                val accelerationY = -(forceY / m).toFloat()
                move(accelerationX, accelerationY, time)
            }
        }
    }

    private fun move(accelerationX: Float, accelerationY: Float, time: Float) {
        vX += accelerationX * time
        vY += accelerationY * time
        x += vX * time
        y += vY * time
    }

    fun reset() {
        x = x0
        y = y0
        vX = 0f
        vY = 0f
    }
}

class Point(
    override var xR: Float,
    override var yR: Float,
    override val haloRatio: Float = 10f,
    override val m: Double = 1.0,
    override var vX: Float = 0f,
    override var vY: Float = 0f,
    override val attractable: Boolean = true
) : Body()

class Attractor(
    val id: Int,
    override var xR: Float,
    override var yR: Float,
    override val haloRatio: Float = 2f,
    override val m: Double = M,
    override var vX: Float = 0f,
    override var vY: Float = 0f,
    override val attractable: Boolean = false
) : Body()

class Portal(
    override var xR: Float,
    override var yR: Float,
    override val haloRatio: Float = 2f
) : GameEntity()