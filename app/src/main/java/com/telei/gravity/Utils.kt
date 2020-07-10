package com.telei.gravity

import android.app.Activity
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import com.telei.gravity.game.Distance
import com.telei.gravity.game.GameData
import com.telei.gravity.game.GameEntity

object Files {
    var gameData: GameData? = null
}

infix fun Float.and(y: Float): PointF = PointF(this, y)

infix fun PointF.isIn(entity: GameEntity): Boolean =
    Distance(x, entity.x, y, entity.y).calculate() < entity.r

infix fun PointF.isInHaloOf(entity: GameEntity): Boolean =
    Distance(x, entity.x, y, entity.y).calculate() < entity.haloR

fun GameEntity.draw(canvas: Canvas, paint: Paint) {
    canvas.drawCircle(x, y, r, paint)
}

fun GameEntity.drawHalo(canvas: Canvas, paint: Paint) {
    canvas.drawCircle(x, y, haloR, paint)
}

fun createPaint() = Paint().apply {
    style = Paint.Style.FILL
    isAntiAlias = true
    isDither = true
}

inline fun <reified T : Activity> Activity.start() {
    startActivity(Intent(this, T::class.java))
}

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