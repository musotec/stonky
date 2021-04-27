package tech.muso.demo.theme

import org.hsluv.HUSLColorConverter
import java.util.*
import kotlin.math.roundToInt

object HSLuvUtil {

    fun argbIntToTuple(colorInt: Int): DoubleArray? {
        return doubleArrayOf(
            (colorInt shr 16 and 0xff) / 255.0,
            (colorInt shr 8 and 0xff) / 255.0,
            (colorInt and 0xff) / 255.0,
        )
    }

    fun rgbTupleToArgb(doubleArray: DoubleArray): Int {
        return 0xFF shl 24 or
                (doubleArray[0].roundToInt() shl 16) or
                (doubleArray[1].roundToInt() shl 8) or
                doubleArray[2].roundToInt()
    }

    private val random = Random()
    fun randomizeHue(colorInt: Int): Int {
        val hpluv = HUSLColorConverter.rgbToHpluv(argbIntToTuple(colorInt))
        hpluv[0] += (random.nextDouble() * 10f)
        return rgbTupleToArgb(HUSLColorConverter.hpluvToRgb(hpluv))
    }
}