package com.telei.gravity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.telei.gravity.game.Distance
import com.telei.gravity.game.GameEntity

infix fun Float.and(y: Float): PointF = PointF(this, y)

infix fun PointF.isIn(entity: GameEntity): Boolean =
    Distance(x, entity.x, y, entity.y).calculate() < entity.r

infix fun PointF.isInHaloOf(entity: GameEntity): Boolean =
    Distance(x, entity.x, y, entity.y).calculate() < entity.haloR

fun GameEntity.drawHalo(canvas: Canvas, paint: Paint) {
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