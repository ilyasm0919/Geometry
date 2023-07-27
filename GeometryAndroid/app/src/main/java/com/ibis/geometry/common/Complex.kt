package com.ibis.geometry.common

import androidx.compose.ui.geometry.Offset
import kotlin.math.*

data class Complex(val re: Float, val im: Float): Geometric() {
    init {
        check(re.isFinite() && im.isFinite()) { "Infinite value" }
    }

    val norm get() = re * re + im * im

    fun arg() = atan2(im, re)

    fun abs() = sqrt(norm)

    operator fun unaryMinus() = Complex(-re, -im)

    operator fun plus(value: Complex) = Complex(
        re + value.re,
        im + value.im
    )

    operator fun minus(value: Complex) = Complex(
        re - value.re,
        im - value.im
    )

    operator fun times(value: Complex) = Complex(
        re * value.re - im * value.im,
        re * value.im + im * value.re
    )

    operator fun div(value: Complex) = times(1 / value)

    fun conj() = Complex(re, -im)

    fun sqrt() = when(this) {
        ZERO -> ZERO
        else -> {
            val t = sqrt((abs(re) + abs()) / 2)
            if (re >= 0) {
                complex(t, im / (2 * t))
            } else {
                complex(abs(im) / (2 * t), 1.0.withSign(im.toDouble()) * t)
            }
        }
    }

    fun exp() = Complex(cos(im), sin(im)) * exp(re)

    fun ln() = Complex(ln(abs()), arg())

    fun toOffset() = Offset(re, -im)

    override fun symmetry(l: Line) = -(conj() * l.coef + l.free) / l.coef.conj()

    override fun translation(a: Complex) = plus(a)

    override fun homothety(a: Complex, k: Complex) = (this - a) * k + a

    override fun inversion(c: Circle) = c.radiusSqr / (this - c.center).conj() + c.center

    override fun choose(time: Float) = this

    override fun toDrawable() = Drawable(this) {
        if (it.border == Border.No || it.border == Border.Dot) return@Drawable
        val offset = toOffset()
        point(offset, it.color)
        it.name?.let { text ->
            text(offset, text, it.color)
        }
    }

    override fun toString() = when {
        this == ZERO -> "0"
        re == 0f -> "%.5fi".format(im)
        im == 0f -> "%.5f".format(re)
        im < 0f -> "%.5f%.5fi".format(re, im)
        else -> "%.5f+%.5fi".format(re, im)
    }

    companion object {
        val ZERO = Complex(0f, 0f)
        val ONE = Complex(1f, 0f)
        val I = Complex(0f, 1f)
    }
}

fun complex(real: Number, imagine: Number) = Complex(real.toFloat(), imagine.toFloat())

operator fun Number.plus(value: Complex) = real() + value
operator fun Complex.plus(value: Number) = this + value.real()
operator fun Number.minus(value: Complex) = real() - value
operator fun Complex.minus(value: Number) = this - value.real()
operator fun Complex.times(value: Number) = Complex(re * value.toFloat(), im * value.toFloat())
operator fun Number.times(value: Complex) = value * this
operator fun Complex.div(value: Number) = Complex(re / value.toFloat(), im / value.toFloat())
operator fun Number.div(value: Complex) = value.conj() * (this.toFloat() / value.norm)

fun Number.real() = complex(this, 0)
fun Number.imagine() = complex(0, this)
