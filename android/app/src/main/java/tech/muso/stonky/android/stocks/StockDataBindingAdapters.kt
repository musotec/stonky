package tech.muso.stonky.android.stocks

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import tech.muso.demo.common.entity.Profile
import java.security.MessageDigest


/**
 * Create a custom DataBinding adapter so that we can hide the image loading logic of Glide.
 *
 * The [Profile] object only contains the image url for the stock, but could potentially handle
 * bindings for a more complex object (logo + background, graph, etc.)
 */
@BindingAdapter("stockProfile")
fun bindImageFromUrl(view: ImageView, stockProfile: Profile) {
    val imageUrl: String? = stockProfile.logoUrl
    if (!imageUrl.isNullOrEmpty()) {
        Glide.with(view.context)
            .load(imageUrl)
            .transform(MultiTransformation(ChromaKeyTransformation(), BlurImageTransformation(view.context)))
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(view)
    }

    //Grey scale
    val colorMatrix =  ColorMatrix()
    colorMatrix.setSaturation(0.0f)
    colorMatrix.postConcat(ColorMatrix(floatArrayOf(
        -0.5f, 0f, 0f, 0f, 255f,
        0f, -0.5f, 0f, 0f, 255f,
        0f, 0f, -0.5f, 0f, 255f,
        0f, 0f, 0f, 1f, 0f
    )))

    // apply slight teal tint
    colorMatrix.postConcat(ColorMatrix(floatArrayOf(
        0.95f, 0f, 0f, 0f, 0f,
        0f, .99f, 0f, 0f, 0f,
        0f, 0f, .98f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    )))

    // finally, bring closer to gray/black
    colorMatrix.postConcat(ColorMatrix(floatArrayOf(
        0.75f, 0f, 0f, 0f, 0f,
        0f, .75f, 0f, 0f, 0f,
        0f, 0f, .75f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    )))


    // create and apply
    val filter =  ColorMatrixColorFilter(colorMatrix)
    view.colorFilter = filter
}

class BlurImageTransformation(context: Context) : Transformation<Bitmap> {

    private var rs: RenderScript = RenderScript.create(context)

    override fun transform(
        context: Context,
        resource: Resource<Bitmap>,
        outWidth: Int,
        outHeight: Int
    ): Resource<Bitmap> {
        val blurredBitmap = resource.get().copy(Bitmap.Config.ARGB_8888, true)

        // Allocate memory for Renderscript to work with
        val input = Allocation.createFromBitmap(
            rs,
            blurredBitmap,
            Allocation.MipmapControl.MIPMAP_FULL,
            Allocation.USAGE_SHARED
        )
        val output = Allocation.createTyped(rs, input.type)

        // Load up an instance of the specific script that we want to use.
        val script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        script.setInput(input)

        // Set the blur radius
        script.setRadius(1f)

        // Start the ScriptIntrinisicBlur
        script.forEach(output)

        // Copy the output to the blurred bitmap
        output.copyTo(resource.get())
        return resource
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update("blur transformation".toByteArray())
    }
}


class ChromaKeyTransformation : Transformation<Bitmap> {

    companion object {
        private const val threshold = 0xee
    }

    override fun transform(
        context: Context,
        resource: Resource<Bitmap>,
        outWidth: Int,
        outHeight: Int
    ): Resource<Bitmap> {
        val bitmap = resource.get()

        val width = bitmap.width
        val height = bitmap.height

        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        pixels.forEachIndexed { i, c ->
            // if above threshold, then remove pixels
            if ((c shr 16 and 0xff) > threshold && (c shr 8 and 0xff)  > threshold && (c and 0xff)  > threshold) {
                pixels[i] = Color.TRANSPARENT
            }
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return resource
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update("chroma key transformation".toByteArray())
    }
}