package com.mintlifescience.app.helperUtils

import android.graphics.drawable.GradientDrawable

object AvatarUtils {

    private val COLORS = intArrayOf(
        0xFF1565C0.toInt(),
        0xFF2E7D32.toInt(),
        0xFF6A1B9A.toInt(),
        0xFFC62828.toInt(),
        0xFF00695C.toInt(),
        0xFFEF6C00.toInt(),
        0xFF4527A0.toInt(),
        0xFF0277BD.toInt(),
        0xFF558B2F.toInt(),
        0xFF4E342E.toInt(),
    )

    fun initials(name: String): String {
        val parts = name.trim().split(" ").filter { it.isNotEmpty() }
        return when {
            parts.size >= 2 -> "${parts[0][0]}${parts[1][0]}".uppercase()
            parts.size == 1 -> parts[0].take(2).uppercase()
            else -> "?"
        }
    }

    fun colorFor(name: String): Int = COLORS[Math.abs(name.hashCode()) % COLORS.size]

    fun tintedCircle(name: String): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(colorFor(name))
        }
}
