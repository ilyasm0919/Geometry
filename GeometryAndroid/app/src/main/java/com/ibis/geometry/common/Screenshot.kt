package com.ibis.geometry.common

sealed class Screenshot {
    object No : Screenshot()
    object Png : Screenshot()
    object Svg : Screenshot()
}
