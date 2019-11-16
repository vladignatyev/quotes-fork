package com.quote.mosaic.core.binding

import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions

class ImageViewBindingAdapters {

    @BindingAdapter("srcCompatRes")
    fun srcCompatRes(view: ImageView, @DrawableRes drawableRes: Int) {
        view.setImageResource(drawableRes)
    }

    @BindingAdapter("imageUri", "imagePlaceholder", requireAll = false)
    fun imageUriBindingAdapter(imageView: ImageView, imageUri: String?, imagePlaceholder: Int = 0) {

        Glide.with(imageView.context)
            .load(imageUri)
            .apply {
                if (imagePlaceholder != 0) apply(RequestOptions().placeholder(imagePlaceholder))

                apply(RequestOptions.centerCropTransform())
                apply(RequestOptions.bitmapTransform(RoundedCorners(100)))
            }
            .into(imageView)
    }
}