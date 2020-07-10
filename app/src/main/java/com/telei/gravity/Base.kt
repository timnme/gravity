package com.telei.gravity

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

abstract class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
}

abstract class FieldView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    protected val pointColor = ContextCompat.getColor(context, R.color.colorAccent)
    protected val aimColor = ContextCompat.getColor(context, R.color.colorPrimaryDark)
    protected val attractorColor = Color.BLACK

    protected val pointPaint = createPaint().apply {
        color = pointColor
    }
    protected val aimPaint = createPaint().apply {
        color = aimColor
    }
    protected val attractorPaint = createPaint().apply {
        color = attractorColor
    }
    protected val haloPaint = createPaint().apply {
        color = pointColor
        alpha = (255 * 0.5f).toInt()
    }
}