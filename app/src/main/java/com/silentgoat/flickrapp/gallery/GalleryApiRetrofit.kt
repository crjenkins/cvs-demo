package com.silentgoat.flickrapp.gallery

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object GalleryApiRetrofit {
    const val BASE_URL = "https://api.flickr.com/services/feeds/"
    val api: IGalleryApi by lazy {
        val moshi =
            Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        retrofit.create(IGalleryApi::class.java)
    }
}