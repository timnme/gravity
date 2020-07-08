package com.telei.gravity.game

import kotlin.math.sqrt

private const val M: Double = 1E25        // Attractor unit mass
private const val G: Double = 6.67408E-11 // gravitational constant
private const val MPDP = 10000f           // meters per dp

data class GameData(
    val aim: Aim,
    val point: Point,
    val attractors: List<Attractor> = emptyList(),
    val portals: List<Portal> = emptyList()
)

data class Distance(var xD: Float, var yD: Float) {
    fun calculate(): Float = sqrt(xD * xD + yD * yD)

    fun scale(factor: Float) {
        xD *= factor
        yD *= factor
    }
}

sealed class GameEntity {
    protected abstract val xR: Float // 0..1 x position
    protected abstract val yR: Float // 0..1 y position

    var x0: Float = 0f
    var y0: Float = 0f
    var r: Float = 0f
    var x: Float = 0f
    var y: Float = 0f

    fun init(width: Int, height: Int, radius: Float) {
        x0 = width * xR
        y0 = height * yR
        r = radius
        x = x0
        y = y0
    }

    infix fun reached(other: GameEntity): Boolean = distance(other).calculate() < other.r

    protected fun distance(other: GameEntity): Distance = Distance(x - other.x, y - other.y)
}

data class Aim(
    override val xR: Float,
    override val yR: Float
) : GameEntity()

sealed class Body : GameEntity() {
    abstract val m: Double              // kg
    abstract var vX: Float              // pixels/s
    abstract var vY: Float              // pixels/s
    abstract val attractable: Boolean

    fun attract(body: Body, time: Float, pixelsPerDp: Float) {
        if (body.attractable || attractable) {
            val distance = distance(body)
            distance.scale(MPDP / pixelsPerDp)
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
    override val xR: Float,
    override val yR: Float,
    override val m: Double = 1.0,
    override var vX: Float = 0f,
    override var vY: Float = 0f,
    override val attractable: Boolean = true
) : Body()

class Attractor(
    val id: Int,
    override val xR: Float,
    override val yR: Float,
    override val m: Double = M,
    override var vX: Float = 0f,
    override var vY: Float = 0f,
    override val attractable: Boolean = false
) : Body()

class Portal(
    override val xR: Float,
    override val yR: Float
) : GameEntity()