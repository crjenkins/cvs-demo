package com.silentgoat.flickrapp.gallery

import com.squareup.moshi.Json
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

data class GalleryData(
    val title:String,
    val link:String,
    val description:String,
    val modified: String,
    val generator:String,
    val items:List<GalleryPhotoData>
)

data class GalleryPhotoData(
    val title:String,
    val link:String,
    val media:GalleryPhotoMediaData,
    val description: String,
    val published:String,
    val author:String,
    @Json(name = "author_id")
    val authorId:String,
    val tags:String
)

data class GalleryPhotoMediaData (
    @Json(name = "m")
    val url:String
)

fun GalleryPhotoData.toGalleryPhotoUI(): GalleryPhotoUI {
    val instant = Instant.parse(published).truncatedTo(ChronoUnit.MINUTES)

    return GalleryPhotoUI(
        title = title,
        url = media.url,
        description = description,
        author = author,
        published = Date.from(instant).toString()
    )
}

fun List<GalleryPhotoData>.toGalleryPhotoUI(): List<GalleryPhotoUI> {
    return this.map { it.toGalleryPhotoUI() }
}