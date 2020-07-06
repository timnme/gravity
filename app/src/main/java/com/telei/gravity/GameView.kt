package com.telei.gravity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import kotlin.math.sqrt

private infix fun Float.and(y: Float): PointF = PointF(this, y)

private const val G: Double = 6.67408E-11 // gravitational constant
private const val M: Double = 1.0         // point mass
private const val SPF: Float = 1f / 120f  // seconds per frame
private const val SPF2: Float = SPF * SPF // seconds per frame squared
private const val MPP = 5000f             // meters per pixel

private const val POINT_X = 1f / 4f
private const val POINT_Y = 3f / 4f

data class Attraction(val completed: Boolean)

data class Body(
    val x0: Float,                   // pixels
    val y0: Float,                   // pixels
    val m: Double,                   // kg
    val r: Float,                    // pixels
    var x: Float = x0,               // pixels
    var y: Float = y0,               // pixels
    var vX: Float = 0f,              // pixels/s
    var vY: Float = 0f,              // pixels/s
    val attractable: Boolean = false,
    val paint: Paint = Paint(),
    val tracePath: Path = Path(),
    val tracePaint: Paint = Paint()
) {
    fun attract(body: Body): Attraction {
        val dxPixels = (x - body.x)
        val dyPixels = (y - body.y)
        val distancePixels = sqrt(dxPixels * dxPixels + dyPixels * dyPixels)
        if (body.attractable || attractable) {
            val dx = dxPixels * MPP
            val dy = dyPixels * MPP
            val distance = distancePixels * MPP
            val force = G * m * body.m / (distance * distance)
            val unitForce = force / distance
            val forceX = unitForce * dx
            val forceY = unitForce * dy
            if (body.attractable) {
                val accelerationX = (forceX / body.m).toFloat()
                val accelerationY = (forceY / body.m).toFloat()
                body.vX += accelerationX * SPF
                body.vY += accelerationY * SPF
                body.x += body.vX * SPF
                body.y += body.vY * SPF
                body.tracePath.lineTo(body.x, body.y)
            }
            if (attractable) {
                val accelerationX = -(forceX / m).toFloat()
                val accelerationY = -(forceY / m).toFloat()
                vX += accelerationX * SPF
                vY += accelerationY * SPF
                x += vX * SPF
                y += vY * SPF
//                    tracePath.lineTo(x, y)
            }
        }
        return Attraction(completed = distancePixels < r)
    }

    fun reset() {
        x = x0
        y = y0
        vX = 0f
        vY = 0f
        tracePath.reset()
        tracePath.moveTo(x0, y0)
    }
}

private fun createPaint() = Paint().apply {
    style = Paint.Style.FILL
    isAntiAlias = true
    isDither = true
}

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val cursorLength = context.resources.getDimension(R.dimen.cursor)

    private val pointColor = ContextCompat.getColor(context, R.color.colorAccent)
    private val aimColor = ContextCompat.getColor(context, R.color.colorPrimaryDark)

    private val pointSize = context.resources.getDimension(R.dimen.point)
    private val aimSize = context.resources.getDimension(R.dimen.aim)
    private val attractorSize = context.resources.getDimension(R.dimen.attractor)

    private val slingPath = Path()
    private val slingPaint = createPaint().apply {
        style = Paint.Style.STROKE
        color = pointColor
        strokeWidth = 4f
    }
    private val cursorPath = Path()
    private val cursorPaint = createPaint().apply {
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(3f, 5f), 0f)
        color = pointColor
        strokeWidth = 3f
    }

    private val attractors: MutableList<Body> = mutableListOf()

    private var valueAnimator: ValueAnimator? = null

    private var touchP: PointF = 0f and 0f

    private lateinit var aim: Body
    private lateinit var point: Body

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val pointPaint = createPaint().apply {
            color = pointColor
        }
        val aimPaint = createPaint().apply {
            color = aimColor
        }
        val attractorPaint = createPaint().apply {
            color = Color.BLACK
        }
        val travelPaint = createPaint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f
            pathEffect = DashPathEffect(floatArrayOf(7f, 4f), 0f)
            color = pointColor
        }

        aim = Body(
            x0 = 0.8f * w,
            y0 = 0.2f * h,
            m = 0.0,
            r = aimSize,
            paint = aimPaint
        )
        point = Body(
            x0 = POINT_X * w,
            y0 = POINT_Y * h,
            m = M,
            r = pointSize,
            attractable = true,
            paint = pointPaint,
            tracePaint = travelPaint
        )
        attractors.add(
            Body(
                x0 = 0.5f * w,
                y0 = 0.5f * h,
                m = 1E25,
                r = attractorSize,
                paint = attractorPaint
            )
        )
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return
        canvas.drawPath(slingPath, slingPaint)
        canvas.drawPath(cursorPath, cursorPaint)
        canvas.drawCircle(aim.x, aim.y, aim.r, aim.paint)
        canvas.drawPath(point.tracePath, point.tracePaint)
        attractors.forEach {
//            canvas.drawPath(it.tracePath, it.tracePaint)
            canvas.drawCircle(it.x, it.y, it.r, it.paint)
        }
        canvas.drawCircle(point.x, point.y, point.r, point.paint)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false
        touchP = event.x and event.y
        with(slingPath) {
            reset()
            moveTo(point.x, point.y)
            lineTo(touchP.x, touchP.y)
        }
        val slingDx = point.x - touchP.x
        val slingDy = point.y - touchP.y
        with(cursorPath) {
            reset()
            moveTo(point.x, point.y)
            val slingHypothesis = sqrt(slingDx * slingDx + slingDy * slingDy)
            val sin = slingDx / slingHypothesis
            val cos = slingDy / slingHypothesis
            val cursorDx = cursorLength * sin
            val cursorDy = cursorLength * cos
            lineTo(point.x + cursorDx, point.y + cursorDy)
        }
        when (event.action) {
            ACTION_DOWN -> {
                valueAnimator?.cancel()
                finish()
            }
            ACTION_MOVE -> {
                invalidate()
            }
            ACTION_UP -> {
                slingPath.reset()
                cursorPath.reset()
                point.vX = slingDx * 10
                point.vY = slingDy * 10
                val time = Float.MAX_VALUE
                valueAnimator = ValueAnimator.ofFloat(0f, time).apply {
                    duration = time.toLong()
                    interpolator = LinearInterpolator()
                    var prevTime = System.nanoTime()
                    addUpdateListener {
                        val currentTime = System.nanoTime()
                        if (currentTime - prevTime > SPF * 1E9) {
                            prevTime = currentTime

                            var attracted = false
                            attractors.forEach {
                                val attraction = it.attract(point)
                                if (attraction.completed) {
                                    attracted = true
                                }
                                attractors.forEach { nested ->
                                    if (nested != it) {
                                        it.attract(nested)
                                    }
                                }
                            }

                            if (attracted) {
                                finish()
                                removeAllUpdateListeners()
                            } else {
//                                if (aim.attract(point).completed) {
//                                    finish()
//                                    removeAllUpdateListeners()
//                                }
                            }

                            invalidate()
                        }
                    }
                    start()
                }
            }
        }
        return true
    }

    private fun finish() {
        point.reset()
        invalidate()
    }

}