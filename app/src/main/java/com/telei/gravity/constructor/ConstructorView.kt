package com.telei.gravity.constructor

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.*
import com.telei.gravity.*
import com.telei.gravity.game.*

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
    private var dragging: Boolean = false
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
        aim.init(w, h)
        point = Point(0.2f, 0.8f)
        point.init(w, h)
        attractors = mutableListOf(
            Attractor(
                id = attractorId++,
                xR = 0.5f,
                yR = 0.5f
            ).also {
                it.init(w, h)
            }
        )
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return
        if (dragged == aim) {
            aim.drawHalo(canvas, haloPaint)
        }
        aim.draw(canvas, aimPaint)
        attractors.forEach {
            if (dragged == it) {
                it.drawHalo(canvas, haloPaint)
            }
            it.draw(canvas, attractorPaint)
        }
        if (dragged == point) {
            point.drawHalo(canvas, haloPaint)
        }
        point.draw(canvas, pointPaint)
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
                        dragging = true
                        dragged = findTouched()
                    }
                }
            }
            ACTION_UP -> {
                if (!dragging) {
                    val touched = findTouchedAttractor()
                    if (touched != null) {
                        attractors.removeMatching {
                            it.id == touched.id
                        }
                    } else {
                        attractors.add(
                            Attractor(
                                id = attractorId++,
                                xR = touch.x / width,
                                yR = touch.y / height
                            ).also {
                                it.init(width, height)
                            }
                        )
                    }
                }
                dragged = null
                dragging = false

                invalidate()
            }
        }
        return true
    }

    private fun findTouched(): GameEntity? = when {
        touch isInHaloOf aim -> aim
        touch isInHaloOf point -> point
        else -> {
            var draggedAttractor: GameEntity? = null
            for (attractor in attractors) {
                if (touch isInHaloOf attractor) {
                    draggedAttractor = attractor
                    break
                }
            }
            draggedAttractor
        }
    }

    private fun findTouchedAttractor(): Attractor? {
        var draggedAttractor: Attractor? = null
        for (attractor in attractors) {
            if (touch isIn attractor) {
                draggedAttractor = attractor
                break
            }
        }
        return draggedAttractor
    }

}