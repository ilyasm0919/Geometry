package com.ibis.geometry.common

import androidx.compose.foundation.gestures.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.unit.toSize
import kotlin.math.PI
import kotlin.math.abs

suspend fun PointerInputScope.detectDragOrTransform(
    transformation: TransformationState,
    onDragStart: (Offset) -> Boolean,
    onDragEnd: () -> Unit,
    onDrag: (Offset) -> Unit,
) {
    forEachGesture {
        awaitPointerEventScope {
            val down = awaitFirstDown(requireUnconsumed = false)
            if (onDragStart(transformation.screenToNormal(down.position, size.toSize()))) {
                drag(down.id) {
                    onDrag(transformation.screenToNormal(it.position, size.toSize()))
                    it.consume()
                }
                onDragEnd()
            } else {
                var rotation = 0f
                var zoom = 1f
                var pan = Offset.Zero
                var pastTouchSlop = false
                val touchSlop = viewConfiguration.touchSlop

                do {
                    val event = awaitPointerEvent()
                    val canceled = event.changes.any { it.isConsumed }
                    if (!canceled) {
                        val zoomChange = event.calculateZoom()
                        val rotationChange = event.calculateRotation()
                        val panChange = event.calculatePan()

                        if (!pastTouchSlop) {
                            zoom *= zoomChange
                            rotation += rotationChange
                            pan += panChange

                            val centroidSize = event.calculateCentroidSize(useCurrent = false)
                            val zoomMotion = abs(1 - zoom) * centroidSize
                            val rotationMotion = abs(rotation * PI.toFloat() * centroidSize / 180f)
                            val panMotion = pan.getDistance()

                            if (zoomMotion > touchSlop ||
                                rotationMotion > touchSlop ||
                                panMotion > touchSlop
                            ) {
                                pastTouchSlop = true
                            }
                        }

                        if (pastTouchSlop) {
                            val centroid = event.calculateCentroid(useCurrent = false)
                            if (rotationChange != 0f ||
                                zoomChange != 1f ||
                                panChange != Offset.Zero
                            ) {
                                transformation.apply(centroid - size.toSize().center, panChange, zoomChange, rotationChange)
                            }
                            event.changes.forEach {
                                if (it.positionChanged()) {
                                    it.consume()
                                }
                            }
                        }
                    }
                } while (!canceled && event.changes.any { it.pressed })
            }
        }
    }
}
