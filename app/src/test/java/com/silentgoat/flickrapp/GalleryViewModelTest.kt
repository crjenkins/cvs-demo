package com.silentgoat.flickrapp

import com.silentgoat.flickrapp.gallery.GalleryData
import com.silentgoat.flickrapp.gallery.GalleryPhotoData
import com.silentgoat.flickrapp.gallery.GalleryPhotoMediaData
import com.silentgoat.flickrapp.gallery.GalleryUiState
import com.silentgoat.flickrapp.gallery.GalleryViewModel
import com.silentgoat.flickrapp.gallery.IGalleryApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class GalleryViewModelTest {
    @get:Rule
    val dispatcherRule = TestViewModelScopeRule()

    private val photo1 = GalleryPhotoData(title = "photo1", link = "link1",
        media = GalleryPhotoMediaData(url = "url1"),
        author = "author1", authorId = "authorId1",
        description = "description1",
        published = "2024-06-27T10:17:53Z", tags = "tags1")

    private val gallery1 = GalleryData("gallery1", "link1",
        "description1", "2024-06-27T10:17:53Z", generator = "generator1",
        items = listOf(photo1)
    )

    private val searchDebounceTime = 1005L

    @Test
    fun `init gallery view model only once`() = runTest {
        val api = mockk<IGalleryApi>(relaxed = true) {
            coEvery { search(any()) } returns gallery1
        }

        GalleryViewModel(api = api).run {
            initialize()
            initialize() // don't subscribe to the state again
            searchForPhotos("cats")
        }

        advanceTimeBy(searchDebounceTime)

        coVerify(exactly = 1) { api.search(any()) }
    }

    @Test
    fun `try to call search when view model is not initialized`() {
        val api = mockk<IGalleryApi>(relaxed = true) {
            coEvery { search(any()) } returns gallery1
        }

        Assert.assertThrows(IllegalStateException::class.java) {
            GalleryViewModel(api = api).run {
                searchForPhotos("cats")
            }
        }
    }

    @Test
    fun `send an empty search after a non-empty one`() = runTest {
        val api = mockk<IGalleryApi>(relaxed = true) {
            coEvery { search(any()) } returns gallery1
        }

        GalleryViewModel(api = api).run {
            initialize()

            searchForPhotos("cats")
            advanceTimeBy(searchDebounceTime)

            searchForPhotos("")
            advanceTimeBy(searchDebounceTime)
        }

        coVerify(exactly = 1) { api.search("cats") }
    }

    @Test
    fun `clear search text`() = runTest {
        val api = mockk<IGalleryApi>(relaxed = true) {
            coEvery { search(any()) } returns gallery1
        }

        GalleryViewModel(api = api).run {
            initialize()

            Assert.assertFalse(uiState.value.isLoading)

            searchForPhotos("cats")
            advanceTimeBy(searchDebounceTime)

            clearSearch()
            advanceTimeBy(searchDebounceTime)

            Assert.assertEquals(searchState.value, "")
            Assert.assertTrue(uiState.value is GalleryUiState.NoSearchResults)
        }
    }

    @Test
    fun `send one successful search`() = runTest {
        val api = mockk<IGalleryApi>(relaxed = true) {
            coEvery { search(any()) } returns gallery1
        }

        GalleryViewModel(api = api).run {
            initialize()

            searchForPhotos("cats")
            Assert.assertTrue(uiState.value is GalleryUiState.NoSearchResults)

            advanceTimeBy(searchDebounceTime)

            coVerify(exactly = 1) { api.search("cats") }

            Assert.assertTrue(uiState.value is GalleryUiState.HasSearchResults)

            with(uiState.value as GalleryUiState.HasSearchResults) {
                Assert.assertEquals(photos.size, 1)
                Assert.assertEquals(photos[0].title, gallery1.items[0].title)
                Assert.assertEquals(photos[0].description, gallery1.items[0].description)
                Assert.assertEquals(photos[0].url, gallery1.items[0].media.url)

                // TODO: Need to mock out timezone since this may fail on other computers
                Assert.assertEquals("Thu Jun 27 06:17:00 EDT 2024", photos[0].published)
            }
        }
    }

    @Test
    fun `send two different searches`() = runTest {
        val api = mockk<IGalleryApi>(relaxed = true) {
            coEvery { search(any()) } returns gallery1
        }

        GalleryViewModel(api = api).run {
            initialize()

            searchForPhotos("cats")
            advanceTimeBy(searchDebounceTime)

            coVerify(exactly = 1) { api.search("cats") }
            Assert.assertTrue(uiState.value is GalleryUiState.HasSearchResults)
            Assert.assertEquals((uiState.value as GalleryUiState.HasSearchResults).photos.size, 1)

            searchForPhotos("dogs")
            advanceTimeBy(searchDebounceTime)

            coVerify(exactly = 1) { api.search("dogs") }
            Assert.assertTrue(uiState.value is GalleryUiState.HasSearchResults)
            Assert.assertEquals((uiState.value as GalleryUiState.HasSearchResults).photos.size, 1)
        }
    }

    @Test
    fun `select a photo in the list`() = runTest {
        val api = mockk<IGalleryApi>(relaxed = true) {
            coEvery { search(any()) } returns gallery1
        }

        GalleryViewModel(api = api).run {
            initialize()

            searchForPhotos("cats")

            advanceTimeBy(searchDebounceTime)

            Assert.assertTrue(uiState.value is GalleryUiState.HasSearchResults)
            Assert.assertEquals((uiState.value as GalleryUiState.HasSearchResults).selectedIndex, -1)

            selectPhoto(0)

            Assert.assertEquals((uiState.value as GalleryUiState.HasSearchResults).selectedIndex, 0)
        }
    }

    @Test
    fun `unselect a photo in the list`() = runTest {
        val api = mockk<IGalleryApi>(relaxed = true) {
            coEvery { search(any()) } returns gallery1
        }

        GalleryViewModel(api = api).run {
            initialize()

            searchForPhotos("cats")
            advanceTimeBy(searchDebounceTime)
            Assert.assertTrue(uiState.value is GalleryUiState.HasSearchResults)

            selectPhoto(0)
            unselectPhoto()

            Assert.assertTrue(uiState.value is GalleryUiState.HasSearchResults)
            Assert.assertEquals((uiState.value as GalleryUiState.HasSearchResults).selectedIndex, -1)
        }
    }
}