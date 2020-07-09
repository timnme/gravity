package com.telei.gravity.constructor

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.*
import com.telei.gravity.FieldView
import com.telei.gravity.and
import com.telei.gravity.game.*
import com.telei.gravity.isIn

private const val LONG_PRESS_DURATION = 300

class ConstructorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FieldView(context, attrs, defStyleAttr) {
    private var touch = 0f and 0f
    private var touchStartTime = 0L

    private lateinit var aim: Aim
    private lateinit var point: Point
    private lateinit var attractors: MutableList<Attractor>

    private var attractorId = 0
    private var dragged: GameEntity? = null

    fun construct(): GameData = GameData(
        aim = aim.apply { normalize(width, height) },
        point = point.apply { normalize(width, height) },
        attractors = attractors.apply {
            forEach { it.normalize(width, height) }
        }
    )

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        aim = Aim(0.8f, 0.2f)
        aim.init(w, h, aimSize)
        point = Point(0.2f, 0.8f)
        point.init(w, h, pointSize)
        attractors = mutableListOf(
            Attractor(
                id = attractorId++,
                xR = 0.5f,
                yR = 0.5f
            ).also {
                it.init(w, h, attractorSize)
            }
        )
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return
        canvas.drawCircle(aim.x, aim.y, aim.r, aimPaint)
        attractors.forEach {
            canvas.drawCircle(it.x, it.y, it.r, attractorPaint)
        }
        canvas.drawCircle(point.x, point.y, point.r, pointPaint)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false
        touch.x = event.x
        touch.y = event.y
        when (event.action) {
            ACTION_DOWN -> {
                touchStartTime = System.currentTimeMillis()
            }
            ACTION_MOVE -> {
                if (dragged != null) {
                    dragged?.x = touch.x
                    dragged?.y = touch.y
                    invalidate()
                } else {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - touchStartTime > LONG_PRESS_DURATION) {
                        dragged = when {
                            touch isIn aim -> aim
                            touch isIn point -> point
                            else -> {
                                var draggedAttractor: GameEntity? = null
                                for (attractor in attractors) {
                                    if (touch isIn attractor) {
                                        draggedAttractor = attractor
                                        break
                                    }
                                }
                                draggedAttractor
                            }
                        }
                    }
                }
            }
            ACTION_UP -> {
                dragged = null
            }
        }
        return true
    }

}