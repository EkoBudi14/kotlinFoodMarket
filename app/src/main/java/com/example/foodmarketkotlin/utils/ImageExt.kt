package com.example.foodmarketkotlin.utils

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.foodmarketkotlin.R

/**
 * Loads a thumbnail with a smooth cross-fade, center-crop, and rounded corners.
 * Used for list/grid item images.
 */
fun ImageView.loadThumbnail(url: String?, radiusDp: Int = 12) {
    val radiusPx = (radiusDp * resources.displayMetrics.density).toInt()
    Glide.with(this)
        .load(url)
        .placeholder(R.drawable.bg_image_placeholder)
        .error(R.drawable.bg_image_placeholder)
        .transition(DrawableTransitionOptions.withCrossFade())
        .transform(CenterCrop(), RoundedCorners(radiusPx))
        .into(this)
}

/**
 * Loads a cover image with a smooth cross-fade, scaling the whole image to
 * fit without cropping. Used for detail/header images where the full subject
 * should stay visible (no zoom-in crop).
 */
fun ImageView.loadCover(url: String?) {
    Glide.with(this)
        .load(url)
        .placeholder(R.drawable.bg_image_placeholder)
        .error(R.drawable.bg_image_placeholder)
        .transition(DrawableTransitionOptions.withCrossFade())
        .fitCenter()
        .into(this)
}
