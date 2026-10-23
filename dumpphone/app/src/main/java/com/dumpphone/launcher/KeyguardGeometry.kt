package com.dumpphone.launcher

/** Full active-display coordinates, in the same units as [width] and [height] (px or dp).
 *
 * Revision B assumes a centered 70.2 × 156.3 mm display in the 162.8 mm body.
 * Android logical density is NOT physical DPI: use the full display bounds so changing
 * display size, font size or resolution does not move the controls under the plastic.
 * The CAD's active-display origin is provisional and still needs a real-phone fit check.
 */
internal fun keyguardGeometry(width: Float, height: Float): KeyguardGeometry {
    require(width > 0 && height > 0)
    val scaleX = width / KeyguardSpec.displayWidth
    val scaleY = height / KeyguardSpec.displayHeight
    val bottomBezel = (KeyguardSpec.phoneHeight - KeyguardSpec.displayHeight) / 2f
    val rowY = height - (KeyguardSpec.rowFromBottom - bottomBezel) * scaleY
    return KeyguardGeometry(
        coveredHeight = (KeyguardSpec.guardHeight - bottomBezel) * scaleY,
        buttons = KeyguardSpec.centersX.zip(KeyguardSpec.diameters).map { (x, diameter) ->
            KeyguardButtonGeometry(width / 2f + x * scaleX, rowY, diameter * scaleX)
        },
    )
}

internal data class KeyguardGeometry(val coveredHeight: Float, val buttons: List<KeyguardButtonGeometry>)
internal data class KeyguardButtonGeometry(val centerX: Float, val centerY: Float, val diameter: Float)
