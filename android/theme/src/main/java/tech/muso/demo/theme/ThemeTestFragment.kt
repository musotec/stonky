package tech.muso.demo.theme

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import org.hsluv.HUSLColorConverter.hpluvToRgb
import org.hsluv.HUSLColorConverter.rgbToHpluv
import java.util.*
import kotlin.math.roundToInt

/**
 * A simple fragment to view our Theme as we design it.
 */
class ThemeTestFragment : Fragment(), LifecycleOwner {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_theme_testing, container, false)
    }
}