package com.telei.gravity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.sqrt
import kotlin.random.Random

private infix fun Float.and(y: Float): PointF = PointF(this, y)

private const val G: Double = 6.67408E-11
private const val M: Double = 1.0 // point mass
private const val SPF: Float = 1f / 120f // seconds per frame
private const val MPP = 10000 // meters per pixel

data class Attractor(
    val x: Float,
    val y: Float,
    val m: Double,
    val r: Float // pixels todo meters?
) {
    fun calculateAttraction(point: PointF): Triple<Boolean, Float, Float> {
        val attrDxPixels = x - point.x
        val attrDyPixels = y - point.y
        val attrDistPixels = sqrt(attrDxPixels * attrDxPixels + attrDyPixels * attrDyPixels)
        val attrDx = attrDxPixels * MPP
        val attrDy = attrDyPixels * MPP
        val attrDist = sqrt(attrDx * attrDx + attrDy * attrDy)
        val effectiveM = m * (if (attrDistPixels > r) 1f else attrDistPixels / r)
        val force = G * M * effectiveM / (attrDist * attrDist)
        val forceDelta = force / attrDist
        val forceX = forceDelta * attrDx
        val forceY = forceDelta * attrDy
        val accelX = forceX / M
        val accelY = forceY / M
        return Triple(attrDist < r, accelX.toFloat(), accelY.toFloat())
    }
}

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val pointR = 15f
    private val aimR = 50f

    private val pointPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.BLACK
        isAntiAlias = true
        isDither = true
    }
    private val aimPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.BLACK
        alpha = (255 * 0.3).toInt()
        isAntiAlias = true
        isDither = true
    }
    private val attractorPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.BLACK
        alpha = (255 * 0.6).toInt()
        isAntiAlias = true
        isDither = true
    }
    private val slingPath = Path()
    private val slingPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f
        color = Color.BLUE
        isAntiAlias = true
        isDither = true
    }

    private val travelPath = Path()
    private val travelPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = Color.RED
        isAntiAlias = true
        isDither = true
    }

    private val attractors: MutableList<Attractor> = mutableListOf()

    private var valueAnimator: ValueAnimator? = null

    private var pointP = 0f and 0f
    private var touchP = -100f and -100f
    private var aimP = 0f and 0f

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        pointP = 0.2f * w and 0.8f * h
        aimP = 0.8f * w and 0.2f * h
        repeat(5) {
            attractors.add(
                Attractor(
                    x = Random.nextInt(3, 8).toFloat() * 0.1f * w,
                    y = Random.nextInt(3, 8).toFloat() * 0.1f * h,
                    m = Random.nextInt(1, 10) * 1E25,
                    r = Random.nextInt(15, 30).toFloat()
                )
            )
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return
        canvas.drawPath(slingPath, slingPaint)
        canvas.drawPath(travelPath, travelPaint)
        canvas.drawCircle(aimP.x, aimP.y, aimR, aimPaint)
        attractors.forEach {
            canvas.drawCircle(it.x, it.y, it.r, attractorPaint)
        }
        canvas.drawCircle(pointP.x, pointP.y, pointR, pointPaint)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false
        touchP = event.x and event.y
        slingPath.reset()
        slingPath.moveTo(pointP.x, pointP.y)
        slingPath.lineTo(touchP.x, touchP.y)
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
                var velocityX = pointP.x - touchP.x
                var velocityY = pointP.y - touchP.y
                val time = Float.MAX_VALUE
                valueAnimator = ValueAnimator.ofFloat(0f, time).apply {
                    duration = time.toLong()
                    interpolator = LinearInterpolator()
                    var prevTime = System.nanoTime()
                    var deltaT = 0f
                    addUpdateListener {
                        val currentTime = System.nanoTime()
                        deltaT += currentTime - prevTime
                        prevTime = currentTime

                        if (deltaT > SPF * 1E9) {
                            deltaT = 0f

                            var attracted = false

                            attractors.forEach {
                                it.calculateAttraction(pointP).run {
                                    if (first) {
                                        attracted = true
                                    } else {
                                        velocityX += second * SPF
                                        velocityY += third * SPF
                                    }
                                }
                            }

                            pointP.x = pointP.x + velocityX * SPF
                            pointP.y = pointP.y + velocityY * SPF

                            travelPath.lineTo(pointP.x, pointP.y)

                            if (attracted) {
                                finish()
                                removeAllUpdateListeners()
                            } else {
                                val aimDx = aimP.x - pointP.x
                                val aimDy = aimP.y - pointP.y
                                val aimDist = sqrt(aimDx * aimDx + aimDy * aimDy)
                                if (aimDist <= aimR) {
                                    finish()
                                    removeAllUpdateListeners()
                                } else {
//                                if (pointP.y < 0 || pointP.y > height) {
//                                    finish()
//                                    removeAllUpdateListeners()
//                                } else {
//                                }
                                }
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
        travelPath.reset()
        pointP.x = 0.2f * width
        pointP.y = 0.8f * height
        travelPath.moveTo(pointP.x, pointP.y)
        invalidate()
    }

}