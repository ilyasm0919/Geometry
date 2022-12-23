package com.ibis.geometry.common

sealed class Screenshot {
    object No : Screenshot()
    object Png : Screenshot()
    object Svg : Screenshot()
    object Html : Screenshot()
    sealed class Record : Screenshot() {
        abstract fun frame(content: Drawer.() -> Unit)
        abstract fun finish()
    }
}
