package tech.muso.graphly.util

/**
 * Get the midpoint between two values.
 */
inline fun midpoint(p1: Float, p2: Float): Float = p1 + (p2 - p1)/2
inline fun midpoint(p1: Int, p2: Int): Int = p1 + (p2 - p1)/2       // TODO: 2/9/2020  this needs to wrap around for circle or this is useless (just the avg)...