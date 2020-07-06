package com.telei.gravity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.VectorDrawable
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_check.*

class Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check)
        (sssss.drawable as AnimatedVectorDrawable).start()
    }
}

class TitleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val paint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.BLACK
        strokeWidth = 5f
        isAntiAlias = true
        isDither = true
    }
    private val paths = mutableListOf<Path>()
    private val pathsToDraw = mutableListOf<Path>()
    private val startPoints = mutableListOf<PointF>()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val newW = w.toFloat()
        val newH = h.toFloat()
        val halfH = newH / 2

        val letterW = 0.85f * newW / 7
        val gapW = 0.15f * newW / 6

        fun add(startPoint: PointF, onPath: Path.() -> Unit) = run {
            paths.add(
                Path().apply {
                    startPoints.add(startPoint)
                    moveTo(startPoint.x, startPoint.y)
                    onPath()
                }
            )
        }

        // G
        var leftEdgeX: Float
        add(letterW and halfH) {
            lineTo(letterW, 0.95f * newH)
            quadTo(letterW / 2, newH, 0f, halfH)
            quadTo(letterW / 2, 0f, letterW, 0.05f * newH)
        }

        // R
        leftEdgeX = letterW + gapW
        add(leftEdgeX and newH) {
            lineTo(letterW + gapW, 0f)
            quadTo(0.8f * leftEdgeX, 0.1f * newH, leftEdgeX + letterW, 0.3f * newH)
            quadTo(0.8f * leftEdgeX, 0.5f * newH, leftEdgeX + 0.5f * letterW, 0.6f * newH)
            lineTo(leftEdgeX + letterW, newH)
        }

        // A
        leftEdgeX = 2 * (letterW + gapW)


        repeat(paths.size) {
            pathsToDraw.add(Path())
        }
        val animDuration = 1500L
        ValueAnimator.ofFloat(0f, animDuration.toFloat()).apply {
            duration = animDuration
            interpolator = LinearInterpolator()
            addUpdateListener {
                val traveled = it.animatedValue as Float
                val measure = PathMeasure()
                paths.forEachIndexed { index, path ->
                    measure.setPath(path, false)
                    val pathToDraw = pathsToDraw[index]
                    pathToDraw.reset()
                    val start = startPoints[index]
                    pathToDraw.moveTo(start.x, start.y)
                    measure.getSegment(
                        0f,
                        traveled / animDuration * measure.length,
                        pathToDraw,
                        false
                    )
                }
                invalidate()
            }
        }.start()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        pathsToDraw.forEach {
            canvas?.drawPath(it, paint)
        }
    }

}