package com.telei.gravity

import android.graphics.Paint
import android.graphics.PointF

infix fun Float.and(y: Float): PointF = PointF(this, y)

 fun createPaint() = Paint().apply {
    style = Paint.Style.FILL
    isAntiAlias = true
    isDither = true
}