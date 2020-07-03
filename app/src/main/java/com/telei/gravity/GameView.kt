package com.telei.gravity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd
import kotlin.math.sqrt

private infix fun Float.and(y: Float): PointF = PointF(this, y)

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val pointPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.BLACK
        isAntiAlias = true
        isDither = true
    }
    private val aimPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.BLACK
        alpha = (255 * 0.5).toInt()
        isAntiAlias = true
        isDither = true
    }
    private val linePaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f
        color = Color.BLUE
        isAntiAlias = true
        isDither = true
    }
    private val path = Path()

    private var p = 0f and 0f
    private var t = -100f and -100f
    private var aim = 0f and 0f
    private val pointR = 15f
    private val aimR = 50f

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        p = 0.2f * w and 0.8f * h
        aim = 0.8f * w and 0.2f * h
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return
        canvas.drawPath(path, linePaint)
        canvas.drawCircle(aim.x, aim.y, aimR, aimPaint)
        canvas.drawCircle(p.x, p.y, 15f, pointPaint)
    }

    private var valueAnimator: ValueAnimator? = null

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false


        t = event.x and event.y
        path.reset()
        path.moveTo(p.x, p.y)
        path.lineTo(t.x, t.y)
        when (event.action) {
            ACTION_DOWN -> {
                valueAnimator?.cancel()
                finish()
            }
            ACTION_MOVE -> {
                invalidate()
            }
            ACTION_UP -> {
                path.reset()
                val dx = t.x - p.x
                val dy = t.y - p.y
                val x: Float
                val y: Float
                when {
                    dx > 0 -> {
                        x = 0f
                        y = t.y - dy / dx * (t.x - x)
                    }
                    dx < 0 -> {
                        x = width.toFloat()
                        y = t.y - dy / dx * (t.x - x)
                    }
                    else -> {
                        x = t.x
                        y = when {
                            dy > 0 -> 0f
                            dy < 0 -> height.toFloat()
                            else -> t.y
                        }
                    }
                }
                val speed = sqrt(dx * dx + dy * dy)
                val pDx = x - p.x
                val pDy = y - p.y
                val dist = sqrt(pDx * pDx + pDy * pDy)
                val time = dist / speed * 500
                println("dist: $dist, speed: $speed, time: $time, x: $x, y: $y")
                t = -100f and -100f
                valueAnimator = ValueAnimator.ofFloat(0f, time).apply {
                    duration = time.toLong()
                    val x0 = p.x
                    val y0 = p.y
                    interpolator = LinearInterpolator()
                    addUpdateListener {
                        val traveled = (it.animatedValue as Float) / time
                        p.x = x0 + pDx * traveled
                        p.y = y0 + pDy * traveled
                        if (p.y < 0 || p.y > height) {
                            finish()
                            removeAllUpdateListeners()
                        } else {
                            val aDx = aim.x - p.x
                            val aDy = aim.y - p.y
                            val r = sqrt(aDx * aDx + aDy * aDy)
                            if (r < aimR) {
                                finish()
                                removeAllUpdateListeners()
                            } else {
                                invalidate()
                            }
                        }
                    }
                    doOnEnd {
                        finish()
                    }
                    start()
                }
            }
        }

        return true
    }

    private fun finish() {
        p.x = 0.2f * width
        p.y = 0.8f * height
        invalidate()
    }

}