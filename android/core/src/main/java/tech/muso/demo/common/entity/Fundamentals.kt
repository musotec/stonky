package tech.muso.demo.common.entity

data class Fundamentals(
    val marketCap: Long,
    val dividendYield: Double,
    val peRatio: Double,
    val peRatioGrade: Int
) {
    // we could add whatever logic or functions we need to for analysis or processing.
    // for now this is just a container.
}