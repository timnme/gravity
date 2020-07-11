package com.telei.gravity

import android.graphics.Paint
import android.util.TypedValue

val pixelsPerDip: Float by lazy {
    TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, 1f, App.context.resources.displayMetrics
    )
}
val pointSize: Float by lazy { App.context.resources.getDimension(
    R.dimen.point
) }
val aimSize: Float by lazy { App.context.resources.getDimension(
    R.dimen.aim
) }
val attractorSize: Float by lazy { App.context.resources.getDimension(
    R.dimen.attractor
) }
val portalSize: Float by lazy { App.context.resources.getDimension(
    R.dimen.portal
) }

val pointColor: Int by lazy { App.context.color(R.color.colorAccent) }
val aimColor: Int by lazy { App.context.color(R.color.colorPrimaryDark) }
val attractorColor: Int by lazy { App.context.color(R.color.colorAttractor) }
val portalEnterColor: Int by lazy { App.context.color(R.color.colorPortalEnter) }
val portalExitColor: Int by lazy { App.context.color(R.color.colorPortalExit) }
val haloColor: Int by lazy { App.context.color(R.color.colorHalo) }

val pointPaint: Paint = createPaint(pointColor)
val aimPaint: Paint = createPaint(aimColor)
val attractorPaint: Paint = createPaint(attractorColor)
val portalEnterPaint: Paint = createPaint(portalEnterColor)
val portalExitPaint: Paint = createPaint(portalExitColor)