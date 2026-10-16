package com.example.foodmarketkotlin.utils

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

/**
 * Loads a thumbnail with a smooth cross-fade, center-crop, and rounded corners.
 * Used for list/grid item images.
 *
 * The placeholder is transparent on purpose: Glide's cross-fade builds a
 * TransitionDrawable that keeps the "from" layer behind the loaded image. With
 * transparent product PNGs that layer would otherwise show through (e.g. the
 * old ic_picture_empty circle or the XML sample src), so a transparent
 * placeholder guarantees nothing bleeds behind the image.
 */
fun ImageView.loadThumbnail(url: String?, radiusDp: Int = 12) {
    val radiusPx = (radiusDp * resources.displayMetrics.density).toInt()
    Glide.with(this)
        .load(url)
        .placeholder(android.R.color.transparent)
        .error(android.R.color.transparent)
        .transition(DrawableTransitionOptions.withCrossFade())
        .transform(CenterCrop(), RoundedCorners(radiusPx))
        .into(this)
}

/**
 * Loads a cover image with a smooth cross-fade, scaling the whole image to
 * fit without cropping. Used for detail/header images where the full subject
 * should stay visible (no zoom-in crop). Transparent placeholder for the same
 * reason as [loadThumbnail].
 */
fun ImageView.loadCover(url: String?) {
    Glide.with(this)
        .load(url)
        .placeholder(android.R.color.transparent)
        .error(android.R.color.transparent)
        .transition(DrawableTransitionOptions.withCrossFade())
        .fitCenter()
        .into(this)
}
