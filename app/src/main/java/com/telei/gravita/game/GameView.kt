package com.telei.gravita.game

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.Choreographer
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.View
import com.telei.gravita.R
import com.telei.gravita.and
import com.telei.gravita.color
import com.telei.gravita.createPaint
import com.telei.gravita.levels.Level
import kotlin.math.sqrt

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), Choreographer.FrameCallback {
    private val cursorLength = context.resources.getDimension(R.dimen.cursor)

    private val pointColor = context.color(R.color.colorAccent)

    private val tracePath = Path()
    private val tracePathMeasure = PathMeasure(tracePath, false)
    private val tracePaint = createPaint(pointColor).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f // TODO: 7/10/2020
//        pathEffect = DashPathEffect(floatArrayOf(7f, 4f), 0f)
    }

    private val slingPath = Path()
    private val slingPaint = createPaint(pointColor).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f // TODO: 7/10/2020
    }

    private val cursorPath = Path()
    private val cursorPaint = createPaint(pointColor).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f // TODO: 7/10/2020
        pathEffect = DashPathEffect(floatArrayOf(3f, 5f), 0f)
    }

    private lateinit var aim: Aim
    private lateinit var point: Point
    private lateinit var attractors: List<Attractor>
    private lateinit var portals: List<Portal>
    private lateinit var chords: List<Chord>

    private var traceMaxLength = 0f

    private var pointStart = 0f and 0f
    private var launched = false
    private var lastFrameTimeNanos = System.nanoTime()
    private var hasAttractableAttractors = false

    var playMode = false

    var level: Level? = null
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
                if (it.attractable) hasAttractableAttractors = true
            }
            portals = value.portals
            portals.forEach {
                it.init(width, height)
            }
            chords = value.chords
            chords.forEach {
                it.init(width, height)
            }
            traceMaxLength = height.toFloat()
            finish()
        }

    private fun finish() {
        launched = false
        aim.reset()
        point.reset()
        portals.forEach(Portal::reset)
        attractors.forEach(Attractor::reset)
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
            for (i in attractors.indices) {
                val attractor = attractors[i]
                attractor.attract(point, timeSeconds)
                if (point reached attractor) attracted = true
                if (hasAttractableAttractors) {
                    for (j in i + 1 until attractors.size) {
                        attractor.attract(attractors[j], timeSeconds)
                    }
                }
                attractor.move(timeSeconds)
            }
            point.move(timeSeconds)
            if (attracted || point reached aim) {
                finish()
            } else {
                chords.forEach {
                    it.receive(point)
                }
                var ported = false
                portals.forEach {
                    if (it.port(point)) ported = true
                }

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

                if (ported) {
                    tracePath.rewind()
                    tracePath.moveTo(point.x, point.y)
                } else {
                    tracePath.lineTo(point.x, point.y)
                }

                invalidate()
            }
        }
        lastFrameTimeNanos = frameTimeNanos
        Choreographer.getInstance().postFrameCallback(this)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return
        level ?: return
        chords.forEach { it.draw(canvas) }
        if (!launched) {
            canvas.drawPath(slingPath, slingPaint)
            canvas.drawPath(cursorPath, cursorPaint)
        }
        canvas.drawPath(tracePath, tracePaint)
        aim.draw(canvas)
        attractors.forEach { it.draw(canvas) }
        point.draw(canvas)
        portals.forEach { it.draw(canvas) }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        performClick()
        if (!playMode) return false
        event ?: return false
        level ?: return false
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