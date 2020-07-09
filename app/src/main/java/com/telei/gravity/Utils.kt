package com.telei.gravity

import android.app.Activity
import android.content.Intent
import android.graphics.Paint
import android.graphics.PointF
import com.telei.gravity.game.Distance
import com.telei.gravity.game.GameData
import com.telei.gravity.game.GameEntity

object Files {
    var gameData: GameData? = null
}

infix fun Float.and(y: Float): PointF = PointF(this, y)

infix fun PointF.isIn(entity: GameEntity): Boolean {
    return Distance(x, entity.x, y, entity.y).calculate() < entity.r
}

fun createPaint() = Paint().apply {
    style = Paint.Style.FILL
    isAntiAlias = true
    isDither = true
}

inline fun <reified T : Activity> Activity.start() {
    startActivity(Intent(this, T::class.java))
}