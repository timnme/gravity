package com.telei.gravity.constructor

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.View
import com.telei.gravity.*
import com.telei.gravity.game.*

private const val LONG_PRESS_DURATION = 300

class ConstructorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val haloPaint = createPaint(context.color(R.color.colorHalo))

    private var touch = 0f and 0f
    private var touchStartTime = 0L

    private lateinit var aim: Aim
    private lateinit var point: Point
    private lateinit var attractors: MutableList<Attractor>
    private lateinit var portals: MutableList<Portal>

    private var attractorId = 0
    private var dragging: Boolean = false
    private var dragged: GameEntity? = null
    private var creatingPortalExit = false

    lateinit var onCreate: () -> Unit
    lateinit var onEdit: (GameEntity) -> Unit

    fun onCreated(entity: GameEntity) {
        when (entity) {
            is Aim, is Point -> Unit
            is Attractor -> {
                attractors.add(
                    entity.copy(
                        id = attractorId++,
                        xR = touch.x / width,
                        yR = touch.y / height
                    ).also {
                        it.init(width, height)
                    }
                )
            }
            is Portal -> {
            }
        }
        invalidate()
    }

    fun onPortalEnterCreated() {
        creatingPortalExit = true
        portals.add(
            Portal(
                xR = touch.x / width,
                yR = touch.y / height,
                xR2 = 0.1f,
                yR2 = 0.1f
            ).also {
                it.init(width, height)
            }
        )
        invalidate()
    }

    fun onEdited(entity: GameEntity) {
        invalidate()
    }

    fun onDeleted(entity: GameEntity) {
        when (entity) {
            is Aim, is Point -> Unit
            is Attractor -> {
                attractors.removeMatching {
                    it.id == entity.id
                }
            }
            is Portal -> {
            }
        }
        invalidate()
    }

    fun construct(): GameData = GameData(
        aim = aim.apply { normalize(width, height) },
        point = point.apply { normalize(width, height) },
        attractors = attractors.apply {
            forEach { it.normalize(width, height) }
        },
        portals = portals.apply {
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
        portals = mutableListOf()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return
        if (dragged == aim) {
            aim.drawHalo(canvas, haloPaint)
        }
        aim.draw(canvas)
        attractors.forEach {
            if (dragged == it) {
                it.drawHalo(canvas, haloPaint)
            }
            it.draw(canvas)
        }
        portals.forEach { it.draw(canvas) }
        if (dragged == point) {
            point.drawHalo(canvas, haloPaint)
        }
        point.draw(canvas)
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
                        dragged = findTouchedInHalo()
                    }
                }
            }
            ACTION_UP -> {
                if (!dragging) {
                    if (creatingPortalExit) {
                        portals.lastOrNull()?.apply {
                            xR2 = touch.x / width
                            yR2 = touch.y / height
                            init(width, height)
                        }
                        creatingPortalExit = false
                    } else {
                        val touched = findTouched()
                        if (touched == null) {
                            onCreate()
                        } else {
                            when (touched) {
                                is Aim, is Point, is Portal -> Unit
                                is Attractor -> onEdit(touched)
                            }
                        }
                    }
                }
                dragged = null
                dragging = false

                invalidate()
            }
        }
        return true
    }

    private fun findTouchedInHalo(): GameEntity? = when {
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

    private fun findTouched(): GameEntity? = when {
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