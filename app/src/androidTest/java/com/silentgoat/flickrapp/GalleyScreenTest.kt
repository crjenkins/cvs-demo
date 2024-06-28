package com.silentgoat.flickrapp

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import cafe.adriel.voyager.navigator.Navigator
import com.silentgoat.flickrapp.gallery.GalleryScreen
import com.silentgoat.flickrapp.ui.theme.FlickrappTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import okhttp3.internal.wait
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection

@OptIn(ExperimentalTestApi::class)
@UninstallModules(FlickrModule::class)
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class GalleyScreenTest {

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<ActivityTest>()

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    private val mockWebServer by lazy { MockWebServer() }

    @Before
    fun setUp() {
        hiltRule.inject()
        mockWebServer.start(8080)
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun searchForPhotosSuccessfully() {
        val json = jsonFromFile("gallery_test.json")

        mockWebServer.enqueue(
            MockResponse().setBody(json)
            .setHeader("accept", "application/json")
            .setResponseCode(HttpURLConnection.HTTP_OK)
        )

        composeTestRule.run {
            setContent {
                FlickrappTheme {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        Navigator(
                            screen = GalleryScreen(padding = PaddingValues(16.dp))
                        )
                    }
                }
            }

            onNodeWithContentDescription("Search for photos.")
                .performTextInput("Cats")

            waitUntilDoesNotExist(hasContentDescription("There are no search results."))

            onNodeWithContentDescription("Photo of Orange Choppers")
                .assertExists()
        }
    }

    private fun jsonFromFile(filename:String):String {
        val stream = javaClass.classLoader?.getResourceAsStream(filename)
        val reader = BufferedReader(InputStreamReader(stream))

        val json = reader.readText()

        reader.close()
        stream?.close()

        return json
    }
}