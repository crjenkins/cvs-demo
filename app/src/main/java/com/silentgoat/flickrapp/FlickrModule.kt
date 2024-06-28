package com.silentgoat.flickrapp

import com.silentgoat.flickrapp.gallery.GalleryApiRetrofit
import com.silentgoat.flickrapp.gallery.IGalleryApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object FlickrModule {
    @Singleton
    @Provides
    fun provideGalleryApi(): IGalleryApi {
        return GalleryApiRetrofit.api
    }
}