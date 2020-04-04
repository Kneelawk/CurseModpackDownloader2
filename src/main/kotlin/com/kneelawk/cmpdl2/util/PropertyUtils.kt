package com.kneelawk.cmpdl2.util

import javafx.application.Platform
import javafx.beans.property.Property
import tornadofx.runLater
import java.util.concurrent.atomic.AtomicReference
import kotlin.reflect.KProperty

/**
 * Utility to only run the operation later if no on the JavaFX application thread.
 */
fun maybeRunLater(op: () -> Unit) {
    if (Platform.isFxApplicationThread()) {
        op()
    } else runLater(op)
}

/**
 * Utility to wrap properties so that set operations only happen on the JavaFX application thread.
 */
class ObjectPropertyWrapper<T>(private val property: Property<T>) {
    private val update = AtomicReference<T>()

    operator fun setValue(thisRef: Any, kProperty: KProperty<*>, value: T) {
        if (Platform.isFxApplicationThread()) {
            property.value = value
        } else if (update.getAndSet(value) == null) {
            runLater { property.value = update.getAndSet(null) }
        }
    }

    operator fun getValue(thisRef: Any, kProperty: KProperty<*>): T {
        return property.value!!
    }
}

/**
 * Version of [ObjectPropertyWrapper] that supports null values.
 */
class NullableObjectPropertyWrapper<T>(private val property: Property<T>) {
    private val update = AtomicReference<Update<T>>()

    operator fun setValue(thisRef: Any, kProperty: KProperty<*>, value: T?) {
        if (Platform.isFxApplicationThread()) {
            property.value = value
        } else if (update.getAndSet(Update(value)) == null) {
            runLater { property.value = update.getAndSet(null).value }
        }
    }

    operator fun getValue(thisRef: Any, kProperty: KProperty<*>): T? {
        return property.value
    }

    private data class Update<T>(val value: T?)
}
