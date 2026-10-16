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
        .placeholder(R.drawable.ic_picture_empty)
        .error(R.drawable.ic_picture_empty)
        .transition(DrawableTransitionOptions.withCrossFade())
        .transform(CenterCrop(), RoundedCorners(radiusPx))
        .into(this)
}

/**
 * Loads a full-bleed cover image with a smooth cross-fade and center-crop.
 * Used for detail/header images that fill the width.
 */
fun ImageView.loadCover(url: String?) {
    Glide.with(this)
        .load(url)
        .placeholder(R.drawable.ic_picture_empty)
        .error(R.drawable.ic_picture_empty)
        .transition(DrawableTransitionOptions.withCrossFade())
        .centerCrop()
        .into(this)
}
