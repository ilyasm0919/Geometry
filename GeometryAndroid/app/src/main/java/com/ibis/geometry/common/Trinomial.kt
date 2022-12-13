package com.ibis.geometry.common

sealed class TrinomialOption {
    object Root1: TrinomialOption()
    object Root2: TrinomialOption()
    data class RootNot(val x: Complex): TrinomialOption()
}

fun root(option: TrinomialOption, a: Complex, b: Complex, c: Complex) = when (option) {
    TrinomialOption.Root1 -> ((b * b / 4 - a * c).sqrt() - b / 2) / a
    TrinomialOption.Root2 -> (-(b * b / 4 - a * c).sqrt() - b / 2) / a
    is TrinomialOption.RootNot -> {
//        (a * option.x * option.x + b * option.x + c).let {
//            check(it.norm < (a.norm + b.norm + c.norm) * 0.0001) { "Given value is not root (value is $it)" }
//        }
        - b / a - option.x
    }
}
