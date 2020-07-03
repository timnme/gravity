package com.telei.gravity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.View
import androidx.core.animation.doOnEnd
import kotlin.math.sqrt

private infix fun Float.and(y: Float): PointF = PointF(this, y)

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val paint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.BLACK
        isAntiAlias = true
        isDither = true
    }
    private val paint2 = Paint().apply {
        style = Paint.Style.FILL
        color = Color.BLUE
        isAntiAlias = true
        isDither = true
    }

    private var p = PointF(200f, 900f)
    private var t = PointF(-100f, -100f)

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return
        canvas.drawCircle(p.x, p.y, 15f, paint)
        canvas.drawCircle(t.x, t.y, 10f, paint2)
    }

    private var valueAnimator: ValueAnimator? = null

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false


        t = event.x and event.y
        when (event.action) {
            ACTION_DOWN -> {
                valueAnimator?.cancel()
                p.x = 200f
                p.y = 900f
                invalidate()
            }
            ACTION_MOVE -> {
                invalidate()
            }
            ACTION_UP -> {
                val dx = t.x - p.x
                val dy = t.y - p.y
                val x: Float
                val y: Float
                when {
                    dx > 0 -> {
                        x = 0f
                        y = t.y - dy / dx * t.x
                    }
                    dx < 0 -> {
                        x = width.toFloat()
                        y = t.y - dy / dx * (width - t.x)
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
                val time = dist / speed * 10000
                t = -100f and -100f
                valueAnimator = ValueAnimator.ofFloat(0f, time).apply {
                    duration = time.toLong() * 100
                    val startX = p.x
                    val startY = p.y
                    addUpdateListener {
                        val traveled = it.animatedValue as Float
                        p.x = startX + pDx * traveled
                        p.y = startY + pDy * traveled
                        invalidate()
                    }
                    doOnEnd {
                        p.x = 200f
                        p.y = 900f
                        invalidate()
                    }
                    start()
                }
            }
        }

        return true
    }

}