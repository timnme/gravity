package com.telei.gravity.game

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.Choreographer
import android.view.MotionEvent
import android.view.MotionEvent.*
import com.telei.gravity.FieldView
import com.telei.gravity.R
import com.telei.gravity.and
import com.telei.gravity.createPaint
import kotlin.math.sqrt

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FieldView(context, attrs, defStyleAttr), Choreographer.FrameCallback {
    private val cursorLength = context.resources.getDimension(R.dimen.cursor)

    private val tracePath = Path()
    private val tracePathMeasure = PathMeasure(tracePath, false)
    private val tracePaint = createPaint().apply {
        style = Paint.Style.STROKE
        color = pointColor
        strokeWidth = 2f // TODO: 7/10/2020
//        pathEffect = DashPathEffect(floatArrayOf(7f, 4f), 0f)
    }

    private val slingPath = Path()
    private val slingPaint = createPaint().apply {
        style = Paint.Style.STROKE
        color = pointColor
        strokeWidth = 4f // TODO: 7/10/2020
    }

    private val cursorPath = Path()
    private val cursorPaint = createPaint().apply {
        style = Paint.Style.STROKE
        color = pointColor
        strokeWidth = 3f // TODO: 7/10/2020
        pathEffect = DashPathEffect(floatArrayOf(3f, 5f), 0f)
    }

    private lateinit var aim: Aim
    private lateinit var point: Point
    private lateinit var attractors: List<Attractor>

    private var traceMaxLength = 0f

    private var pointStart = 0f and 0f
    private var launched = false
    private var lastFrameTimeNanos = System.nanoTime()

    var gameData: GameData? = null
        set(value) {
            field = value ?: return
            aim = value.aim
            aim.init(width, height)
            point = value.point
            point.init(width, height)
            pointStart = point.x0 and point.y0
            attractors = value.attractors
            attractors.forEach {
                it.init(width, height)
            }
            traceMaxLength = height.toFloat()
            finish()
        }

    private fun finish() {
        launched = false
        point.reset()
        tracePath.reset()
        tracePath.moveTo(point.x0, point.y0)
        invalidate()
    }

    fun start() {
        Choreographer.getInstance().postFrameCallback(this)
    }

    fun stop() {
        Choreographer.getInstance().removeFrameCallback(this)
    }

    override fun doFrame(frameTimeNanos: Long) {
        if (launched) {
            val timeSeconds = ((frameTimeNanos - lastFrameTimeNanos) / 1E9).toFloat()
            var attracted = false
            attractors.forEach {
                it.attract(point, timeSeconds)
                if (point reached it) {
                    attracted = true
                }
                attractors.forEach { nested ->
                    if (nested.id != it.id) {
                        it.attract(nested, timeSeconds)
                    }
                }
            }
            if (attracted || point reached aim) {
                finish()
            } else {
                tracePathMeasure.setPath(tracePath, false)
                val length = tracePathMeasure.length
                if (length > traceMaxLength) {
                    tracePath.rewind()
                    tracePathMeasure.getSegment(
                        length - traceMaxLength,
                        length,
                        tracePath,
                        true
                    )
                }
                tracePath.lineTo(point.x, point.y)
                invalidate()
            }
        }
        lastFrameTimeNanos = frameTimeNanos
        Choreographer.getInstance().postFrameCallback(this)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return
        gameData ?: return
        canvas.drawCircle(aim.x0, aim.y0, aim.r, aimPaint)
        attractors.forEach {
            canvas.drawCircle(it.x0, it.y0, it.r, attractorPaint)
        }
        if (!launched) {
            canvas.drawPath(slingPath, slingPaint)
            canvas.drawPath(cursorPath, cursorPaint)
        }
        canvas.drawPath(tracePath, tracePaint)
        canvas.drawCircle(point.x, point.y, point.r, pointPaint)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false
        gameData ?: return false
        val x = event.x
        val y = event.y
        with(slingPath) {
            reset()
            moveTo(pointStart.x, pointStart.y)
            lineTo(x, y)
        }
        val slingDx = pointStart.x - x
        val slingDy = pointStart.y - y
        with(cursorPath) {
            reset()
            moveTo(pointStart.x, pointStart.y)
            val slingHypothesis = sqrt(slingDx * slingDx + slingDy * slingDy)
            val cursorDx = cursorLength * slingDx / slingHypothesis
            val cursorDy = cursorLength * slingDy / slingHypothesis
            lineTo(pointStart.x + cursorDx, pointStart.y + cursorDy)
        }
        when (event.action) {
            ACTION_DOWN -> {
                finish()
            }
            ACTION_MOVE -> {
                invalidate()
            }
            ACTION_UP -> {
                slingPath.reset()
                cursorPath.reset()
                point.vX = slingDx
                point.vY = slingDy
                launched = true
            }
        }
        return true
    }

}