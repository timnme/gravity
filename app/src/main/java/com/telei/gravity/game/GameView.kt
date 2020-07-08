package com.telei.gravity.game

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Choreographer
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.View
import androidx.core.content.ContextCompat
import com.telei.gravity.R
import com.telei.gravity.and
import com.telei.gravity.createPaint
import kotlin.math.sqrt

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), Choreographer.FrameCallback {
    private var pixelsPerDip = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, 1f, resources.displayMetrics
    )

    private val cursorLength = context.resources.getDimension(R.dimen.cursor)

    private val pointColor = ContextCompat.getColor(
        context,
        R.color.colorAccent
    )
    private val aimColor = ContextCompat.getColor(
        context,
        R.color.colorPrimaryDark
    )

    private val pointSize = context.resources.getDimension(R.dimen.point)
    private val aimSize = context.resources.getDimension(R.dimen.aim)
    private val attractorSize = context.resources.getDimension(R.dimen.attractor)

    private val pointPaint = createPaint().apply {
        color = pointColor
    }
    private val aimPaint = createPaint().apply {
        color = aimColor
    }
    private val attractorPaint = createPaint().apply {
        color = Color.BLACK
    }
    private val tracePath = Path()
    private val tracePaint = createPaint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        pathEffect = DashPathEffect(floatArrayOf(7f, 4f), 0f)
        color = pointColor
    }

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

    private lateinit var aim: Aim
    private lateinit var point: Point
    private lateinit var attractors: List<Attractor>

    private var pointStart = 0f and 0f
    private var launched = false
    private var lastFrameTimeNanos = System.nanoTime()

    var gameData: GameData? = null
        set(value) {
            field = value ?: return
            aim = value.aim
            aim.init(width, height, aimSize)
            point = value.point
            point.init(width, height, pointSize)
            pointStart = point.x0 and point.y0
            attractors = value.attractors
            attractors.forEach {
                it.init(width, height, attractorSize)
            }
            finish()
            Choreographer.getInstance().postFrameCallback(this)
        }

    private fun finish() {
        launched = false
        point.reset()
        tracePath.reset()
        tracePath.moveTo(point.x0, point.y0)
        invalidate()
    }

    override fun doFrame(frameTimeNanos: Long) {
        if (launched) {
            val timeSeconds = ((frameTimeNanos - lastFrameTimeNanos) / 1E9).toFloat()
            var attracted = false
            attractors.forEach {
                it.attract(point, timeSeconds, pixelsPerDip)
                if (point reached it) {
                    attracted = true
                }
                attractors.forEach { nested ->
                    if (nested.id != it.id) {
                        it.attract(nested, timeSeconds, pixelsPerDip)
                    }
                }
            }
            if (attracted || point reached aim) {
                finish()
            } else {
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