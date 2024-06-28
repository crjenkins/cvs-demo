package com.silentgoat.flickrapp.gallery

import retrofit2.http.GET
import retrofit2.http.Query

interface IGalleryApi {
    @GET("photos_public.gne?format=json&nojsoncallback=1&")
    suspend fun search(@Query("tags") tags:String): GalleryData
}