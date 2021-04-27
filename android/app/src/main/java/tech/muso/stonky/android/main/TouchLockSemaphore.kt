package tech.muso.stonky.android.main

/**
 * A shared locking mechanism that can communicate the stop and resume of code by the
 * interface provided.
 *
 * NOTE: this implementation is not thread safe, so multiple callbacks might occur depending on
 *   future use. We could make it thread safe with either AtomicIntegers backing the locks or by
 *   using a Handler. Other implementations of thread safety I personally do not prefer in Android.
 *
 * @param listener the interface that cares about the locked/unlocked state.
 *   In this case, touch events.
 */
class TouchLockSemaphore(private val listener: Listener) {
    interface Listener {
        fun onLockChanged(isLocked: Boolean)
    }

    fun lock() = numLocksHeld++
    fun unlock() = numLocksHeld--

    private var numLocksHeld = 0
        set(value) {
            if (isLocked && value == 0) listener.onLockChanged(false)
            if (!isLocked && value > 0) listener.onLockChanged(true)
            // NOTE: positive assertion of state may be undesired or excessive
            //   however it is here to illustrate the concept.
            if (value < 0) throw IllegalStateException("Attempted to unlock without first acquiring!")
            field = value
        }

    val isLocked: Boolean get() = numLocksHeld > 0
}
