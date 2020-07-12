package com.telei.gravita

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.telei.gravita.game.Distance
import com.telei.gravita.game.Body

infix fun Float.and(y: Float): PointF = PointF(this, y)

infix fun PointF.isIn(body: Body): Boolean =
    Distance(x, body.x, y, body.y).calculate() < body.r

infix fun PointF.isInHaloOf(body: Body): Boolean =
    Distance(x, body.x, y, body.y).calculate() < body.haloR

fun Body.drawHalo(canvas: Canvas, paint: Paint) {
    canvas.drawCircle(x, y, haloR, paint)
}

fun createPaint(@ColorInt color: Int) = Paint().apply {
    this.color = color
    style = Paint.Style.FILL
    isAntiAlias = true
    isDither = true
}

inline fun <reified T : Activity> Activity.start() {
    startActivity(Intent(this, T::class.java))
}

@ColorInt
fun Context.color(@ColorRes colorResId: Int): Int = ContextCompat.getColor(this, colorResId)

fun <T> MutableList<T>.removeMatching(matcher: (T) -> Boolean): Boolean {
    val iterator = iterator()
    while (iterator.hasNext()) {
        val next = iterator.next()
        if (matcher(next)) {
            iterator.remove()
            return true
        }
    }
    return false
}