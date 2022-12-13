package com.ibis.geometry.common

@JvmInline
value class ReactiveInput(val time: Int) {
    operator fun<T> Reactive<T>.invoke() = invoke(this@ReactiveInput)
}

sealed interface Reactive<out T> {
    operator fun invoke(input: ReactiveInput): T
}

class Static<out T>(val value: T): Reactive<T> {
    override fun invoke(input: ReactiveInput) = value
}

class Dynamic<out T>(val builder: ReactiveInput.() -> T): Reactive<T> {
    private var last: Pair<ReactiveInput, T>? = null
    override fun invoke(input: ReactiveInput) = last?.takeIf {
        it.first == input
    }?.second ?: input.builder().also {
        last = input to it
    }
}

fun<T, U> Reactive<T>.map(selector: (T) -> U) =
    if (this is Static) Static(selector(value))
    else Dynamic { selector(this@map()) }
fun<T, U> Collection<Reactive<T>>.traverse(selector: (List<T>) -> U) =
    if (all { it is Static }) Static(selector(map { (it as Static).value }))
    else Dynamic { selector(map { it() }) }
fun<T> Collection<Reactive<T>>.sequenceA() = traverse { it }

inline fun<T, reified U> cast(value: T) = value as? U ?: error("Value is not ${U::class.simpleName}")

inline fun<T, reified U, V> ((U) -> V).precast(value: Reactive<T>) =
    if (value is Static) Static(this(cast(value.value)))
    else Dynamic { this@precast(cast(value())) }
inline fun<T1, reified U1, T2, reified U2, V>
        ((U1, U2) -> V).precast(value1: Reactive<T1>, value2: Reactive<T2>) =
    if (value1 is Static && value2 is Static) Static(this(cast(value1.value), cast(value2.value)))
    else Dynamic { this@precast(cast(value1()), cast(value2())) }
