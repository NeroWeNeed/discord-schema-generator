package io.kod.commons

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.math.max
import kotlin.math.min
@Serializable
@JvmInline
value class Color(val value: Int)
sealed class ColorFormat {
    abstract fun red(color: Color): Float
    abstract fun green(color: Color): Float
    abstract fun blue(color: Color): Float
    abstract fun alpha(color: Color): Float
    open fun rgb(color: Color) = Triple(red(color), green(color), blue(color))
    open fun hsv(color: Color): Triple<Float, Float, Float> {
        val (r, g, b) = rgb(color)
        val cMax = max(max(r, g), b)
        val cMin = min(min(r, g), b)
        val delta = cMax - cMin
        return Triple(hue(r, g, b, cMax, delta), saturation(cMax, delta), value(cMax))
    }

    fun hsb(color: Color) = hsv(color)
    open fun hsl(color: Color): Triple<Float, Float, Float> {
        val (r, g, b) = rgb(color)
        val cMax = max(max(r, g), b)
        val cMin = min(min(r, g), b)
        val delta = cMax - cMin
        return Triple(hue(r, g, b, cMax, delta), saturation(cMax, delta), lightness(cMax, cMin))
    }

    open fun hue(color: Color): Float {
        val (r, g, b) = rgb(color)
        val cMax = max(max(r, g), b)
        val cMin = min(min(r, g), b)
        val delta = cMax - cMin
        return hue(r, g, b, cMax, delta)
    }

    open fun saturation(color: Color): Float {
        val (r, g, b) = rgb(color)
        val cMax = max(max(r, g), b)
        val cMin = min(min(r, g), b)
        val delta = cMax - cMin
        return if (cMax == 0f) 0f else delta / cMax
    }

    open fun value(color: Color): Float {
        val (r, g, b) = rgb(color)
        return max(max(r, g), b)
    }

    open fun lightness(color: Color): Float {
        val (r, g, b) = rgb(color)
        val cMax = max(max(r, g), b)
        val cMin = min(min(r, g), b)
        return lightness(cMax, cMin)
    }

    private fun hue(red: Float, green: Float, blue: Float, cMax: Float, delta: Float) = when {
        delta == 0f -> 0f
        cMax == red -> (((green - blue) / delta) % 6) * 60f
        cMax == green -> (((blue - red) / delta) + 2) * 60f
        cMax == blue -> (((red - green) / delta) + 4) * 60f
        else -> 0f
    }

    private fun saturation(cMax: Float, delta: Float) = if (cMax == 0f) 0f else delta / cMax
    private fun value(cMax: Float): Float = cMax

    private fun lightness(cMax: Float, cMin: Float): Float {
        return (cMax + cMin) / 2
    }

}

object RGBA32ColorFormat : ColorFormat() {
    override fun red(color: Color): Float {
        return color.value.shr(24) / 255f
    }

    override fun green(color: Color): Float {
        return (color.value.shr(16).and(0xFF)) / 255f
    }

    override fun blue(color: Color): Float {
        return (color.value.shr(8).and(0xFF)) / 255f
    }

    override fun alpha(color: Color): Float {
        return (color.value.and(0xFF)) / 255f
    }

}
object ARGB32ColorFormat : ColorFormat() {
    override fun red(color: Color): Float {
        return color.value.shr(16) / 255f
    }

    override fun green(color: Color): Float {
        return (color.value.shr(8).and(0xFF)) / 255f
    }

    override fun blue(color: Color): Float {
        return (color.value.and(0xFF)) / 255f
    }

    override fun alpha(color: Color): Float {
        return (color.value.shr(24).and(0xFF)) / 255f
    }

}