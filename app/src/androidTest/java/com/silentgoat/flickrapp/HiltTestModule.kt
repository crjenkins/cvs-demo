package com.silentgoat.flickrapp

import com.silentgoat.flickrapp.gallery.IGalleryApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [FlickrModule::class]
)
object HiltTestAppModule {
    @Singleton
    @Provides
    fun provideSearchApi(): IGalleryApi {
        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl("http://localhost:8080/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        return retrofit.create(IGalleryApi::class.java)
    }
}