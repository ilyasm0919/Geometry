package com.ibis.geometry.common

data class ReactiveInput(val time: Int, val args: List<Geometric>) {
    operator fun<T> Reactive<T>.invoke() = invoke(this@ReactiveInput)
}

sealed interface Reactive<out T> {
    val timeUsed: Boolean
    val argsUsed: Boolean
    operator fun invoke(input: ReactiveInput): T
}

class Static<out T>(val value: T): Reactive<T> {
    override val argsUsed = false
    override val timeUsed = false
    override fun invoke(input: ReactiveInput) = value
}

class Dynamic<out T>(override val timeUsed: Boolean, override val argsUsed: Boolean, val builder: ReactiveInput.() -> T): Reactive<T> {
    private var last: Pair<ReactiveInput, T>? = null
    override fun invoke(input: ReactiveInput) = last?.takeIf {
        it.first == input
    }?.second ?: input.builder().also {
        last = input to it
    }
}

fun<T, U> Reactive<T>.map(selector: (T) -> U) =
    if (this is Static) Static(selector(value))
    else Dynamic(timeUsed, argsUsed) { selector(this@map()) }
fun<T, U> Collection<Reactive<T>>.traverse(selector: (List<T>) -> U) =
    if (all { it is Static }) Static(selector(map { (it as Static).value }))
    else Dynamic(any { it.timeUsed }, any { it.argsUsed }) { selector(map { it() }) }
fun<T> Collection<Reactive<T>>.sequenceA() = traverse { it }

inline fun<T, reified U> cast(value: T) = value as? U ?: error("Value is not ${U::class.simpleName}")

inline fun<T, reified U, V> ((U) -> V).precast(value: Reactive<T>) =
    if (value is Static) Static(this(cast(value.value)))
    else Dynamic(value.timeUsed, value.argsUsed) { this@precast(cast(value())) }
inline fun<T1, reified U1, T2, reified U2, V>
        ((U1, U2) -> V).precast(value1: Reactive<T1>, value2: Reactive<T2>) =
    if (value1 is Static && value2 is Static) Static(this(cast(value1.value), cast(value2.value)))
    else Dynamic(value1.timeUsed || value2.timeUsed, value1.argsUsed || value2.argsUsed) {
        this@precast(cast(value1()), cast(value2()))
    }

inline fun<T, U> Reactive<T>.fixTime(crossinline selector: ((Int) -> T) -> U) =
    if (argsUsed) Dynamic(timeUsed = false, argsUsed = true) { selector { this@fixTime(ReactiveInput(it, args)) } }
    else Static(selector { this(ReactiveInput(it, emptyList())) })

fun<T> Reactive<T>.withArgs(args: Reactive<List<Geometric>>) =
    if (timeUsed || args.timeUsed || args.argsUsed) Dynamic(timeUsed || args.timeUsed, args.argsUsed) {
        this@withArgs(ReactiveInput(time, args()))
    } else Static(this(ReactiveInput(0, args(ReactiveInput(0, emptyList())))))
