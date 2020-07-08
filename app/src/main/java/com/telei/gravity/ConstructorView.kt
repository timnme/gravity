package com.telei.gravity

import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_MOVE
import android.view.View
import android.widget.Toast

class ConstructorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private lateinit var touchPosition: PointF
    private var touchStartTime = 0L
    private var touchHandled = false

    lateinit var showObjectSelection: () -> Unit

    fun onObjectSelected(selection: GameObject) {
        if (selection == GameObject.BLACK_HOLE) {

        }
        Toast.makeText(context, "Selected", Toast.LENGTH_SHORT).show()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return super.onTouchEvent(event)
        touchPosition = event.x and event.y
        when (event.action) {
            ACTION_DOWN -> {
                touchStartTime = System.currentTimeMillis()
            }
            ACTION_MOVE -> {
                val currentTime = System.currentTimeMillis()
                if (!touchHandled && currentTime - touchStartTime > 500) {
                    touchHandled = true
                    showObjectSelection()
                }
            }
        }

        return true
    }

}