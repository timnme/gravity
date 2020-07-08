package com.telei.gravity

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.Choreographer
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.math.sqrt

infix fun Float.and(y: Float): PointF = PointF(this, y)

private const val G: Double = 6.67408E-11 // gravitational constant
private const val M: Double = 1.0         // point mass
private const val MPP = 10000f             // meters per pixel

private const val POINT_X = 2f / 4f
private const val POINT_Y = 8f / 9f

data class Attraction(val completed: Boolean)

private fun createPaint() = Paint().apply {
    style = Paint.Style.FILL
    isAntiAlias = true
    isDither = true
}

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), Choreographer.FrameCallback {
    private val cursorLength = context.resources.getDimension(R.dimen.cursor)

    private val pointColor = ContextCompat.getColor(context, R.color.colorAccent)
    private val aimColor = ContextCompat.getColor(context, R.color.colorPrimaryDark)

    private val pointSize = context.resources.getDimension(R.dimen.point)
    private val aimSize = context.resources.getDimension(R.dimen.aim)
    private val attractorSize = context.resources.getDimension(R.dimen.attractor)

    private var pointStart = 0f and 0f

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

    private lateinit var aim: Body
    private lateinit var point: Body

    private var launched = false
    private var lastFrameTimeNanos = System.nanoTime()

    private lateinit var extraBitmap: Bitmap
    private lateinit var extraCanvas: Canvas

    private inner class Body(
        val id: Int,
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
        var previousDrawFinishX = x0
        var previousDrawFinishY = y0

        init {
            draw()
        }

        fun draw() {
            extraCanvas.drawCircle(x0, y0, r, paint)
        }

        fun attract(body: Body, time: Float): Attraction {
            val (dxPixels, dyPixels, distancePixels) = distance(body)
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
                    body.vX += accelerationX * time
                    body.vY += accelerationY * time
                    body.x += body.vX * time
                    body.y += body.vY * time
                    val dX = body.x - previousDrawFinishX
                    val dY = body.y - previousDrawFinishY
                    val dist = sqrt(dX * dX + dY * dY)
                    if (dist >= 10) {
                        // TODO: 7/7/2020 move from here
//                        body.tracePath.reset()
//                        body.tracePath.moveTo(previousDrawFinishX, previousDrawFinishY)
//                        body.tracePath.lineTo(body.x, body.y)
//                        extraCanvas.drawPath(body.tracePath, body.tracePaint)
                        extraCanvas.drawPoint(body.x, body.y, body.tracePaint)
                        previousDrawFinishX = body.x
                        previousDrawFinishY = body.y
                    }
                }
                if (attractable) {
                    val accelerationX = -(forceX / m).toFloat()
                    val accelerationY = -(forceY / m).toFloat()
                    vX += accelerationX * time
                    vY += accelerationY * time
//                    tracePath.reset()
//                    tracePath.moveTo(x, y)
                    x += vX * time
                    y += vY * time
//                    tracePath.lineTo(x, y)
//                    extraCanvas.drawPath(tracePath, tracePaint)
                }
            }
            return Attraction(completed = distancePixels < r)
        }

        private fun distance(body: Body): Triple<Float, Float, Float> {
            val dxPixels = (x - body.x)
            val dyPixels = (y - body.y)
            val distancePixels = sqrt(dxPixels * dxPixels + dyPixels * dyPixels)
            return Triple(dxPixels, dyPixels, distancePixels)
        }

        fun attraction(body: Body): Attraction {
            val (_, _, distance) = distance(body)
            return Attraction(completed = distance < r)
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

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        initBitmap()

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

        pointStart = POINT_X * w and POINT_Y * h

        var id = 0
        aim = Body(
            id = id++,
            x0 = 0.5f * w,
            y0 = 0.1f * h,
            m = 0.0,
            r = aimSize,
            paint = aimPaint
        )
        point = Body(
            id = id++,
            x0 = pointStart.x,
            y0 = pointStart.y,
            m = M,
            r = pointSize,
            attractable = true,
            paint = pointPaint,
            tracePaint = travelPaint
        )
//        attractors.add(
//            Body(
//                id = id++,
//                x0 = 0.2f * w,
//                y0 = 0.5f * h,
//                m = 1E25,
//                r = attractorSize,
//                paint = attractorPaint
//            )
//        )
        attractors.add(
            Body(
                id = id++,
                x0 = 0.5f * w,
                y0 = 0.5f * h,
                m = 1E26,
                r = attractorSize,
                paint = attractorPaint
            )
        )
//        attractors.add(
//            Body(
//                id = id++,
//                x0 = 0.8f * w,
//                y0 = 0.5f * h,
//                m = 1E25,
//                r = attractorSize,
//                paint = attractorPaint
//            )
//        )

        Choreographer.getInstance().postFrameCallback(this)
    }

    private fun initBitmap() {
        if (::extraBitmap.isInitialized) extraBitmap.recycle()
        extraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        extraCanvas = Canvas(extraBitmap)
    }

    var a = -1

    override fun doFrame(frameTimeNanos: Long) {
        if (launched) {
            val timeSeconds = ((frameTimeNanos - lastFrameTimeNanos) / 1E9).toFloat()
            var attracted = false
            attractors.forEach {
//                if (it.y < 0.3 * height) a = +1
//                if (it.y > 0.7 * height) a = -1
//                it.y = it.y + a * (timeSeconds * height / 10f)
                if (it.attract(point, timeSeconds).completed) {
                    attracted = true
                }
                attractors.forEach { nested ->
                    if (nested.id != it.id) {
                        it.attract(nested, timeSeconds)
                    }
                }
            }
            if (attracted || aim.attraction(point).completed) {
                finish()
            } else {
                invalidate()
            }
        }
        lastFrameTimeNanos = frameTimeNanos
        Choreographer.getInstance().postFrameCallback(this)
    }

    private fun finish() {
        launched = false
        point.reset()
        initBitmap()
        aim.draw()
        attractors.forEach(Body::draw)
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return
//        canvas.drawBitmap(extraBitmap, 0f, 0f, null)
        if (!launched) {
            canvas.drawPath(slingPath, slingPaint)
            canvas.drawPath(cursorPath, cursorPaint)
        }
        canvas.drawCircle(point.x, point.y, point.r, point.paint)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false
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