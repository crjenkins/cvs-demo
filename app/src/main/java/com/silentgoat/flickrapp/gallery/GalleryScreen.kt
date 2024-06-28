@file:OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)

package com.silentgoat.flickrapp.gallery

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.hilt.getViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.silentgoat.flickrapp.R
import com.silentgoat.flickrapp.gallery.GalleryViewModel.Companion.TAG
import com.silentgoat.flickrapp.ui.theme.Typography
import kotlinx.coroutines.FlowPreview

class GalleryScreen(private val padding: PaddingValues) : Screen {

    @Composable
    override fun Content() {
        val viewModel: GalleryViewModel = getViewModel()
        val state = viewModel.uiState.collectAsStateWithLifecycle()
        val searchState = viewModel.searchState.collectAsStateWithLifecycle()

        viewModel.initialize()

        Surface(modifier = Modifier.padding(padding)) {
            Column(modifier = Modifier.background(Color.White)) {
                SearchView(
                    search = searchState.value,
                    onSearchChanged = { search -> viewModel.searchForPhotos(search) }
                )

                if (state.value.isLoading) {
                    LoadingView()
                } else if (state.value.error != GalleryError.NONE) {
                    ErrorView()
                } else if (state.value is GalleryUiState.HasSearchResults) {
                    val results = state.value as GalleryUiState.HasSearchResults

                    GalleryView(photos = results.photos) {
                        selectedIndex -> viewModel.selectPhoto(selectedIndex)
                    }

                    if (results.selectedIndex >= 0) {
                        ImageDetailsView(photo = results.photos[results.selectedIndex]) {
                            viewModel.unselectPhoto()
                        }
                    }
                } else {
                    EmptySearchView()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageDetailsView(modifier: Modifier = Modifier,
                     sheetState: SheetState = rememberModalBottomSheetState(),
                     photo:GalleryPhotoUI,
                     onDismiss: () -> Unit) {
    val imageDetailsContentDescription = stringResource(
        id = R.string.image_details_content_description)

    ModalBottomSheet(
        onDismissRequest = {
            onDismiss()
        },
        sheetState = sheetState,
        modifier = modifier.semantics { contentDescription = imageDetailsContentDescription }
    ) {
        val descriptionLabelContentDescription = stringResource(id = R.string.photo_description_content_description_label)
        val authorLabelContentDescription = stringResource(id = R.string.photo_author_content_description_label)
        val publishedLabelContentDescription = stringResource(id = R.string.photo_published_content_description_label)

        val titleContentDescription = stringResource(id = R.string.photo_title_content_description)
        val descriptionContentDescription = stringResource(id = R.string.photo_description_content_description)
        val authorContentDescription = stringResource(id = R.string.photo_author_content_description)
        val publishedContentDescription = stringResource(id = R.string.photo_published_content_description)

        Column(modifier = modifier.fillMaxWidth()) {
            PhotoImageView(
                modifier = modifier.align(alignment = Alignment.CenterHorizontally),
                photo = photo,
                contentScale = ContentScale.Fit
            )
            Text(
                text = photo.title, style = Typography.titleLarge, modifier = Modifier
                    .padding(16.dp)
                    .align(alignment = Alignment.CenterHorizontally)
                    .semantics {
                        contentDescription = titleContentDescription
                    }
            )
            Text(
                text = stringResource(id = R.string.photo_description),
                style = Typography.titleLarge,
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    .semantics {
                        contentDescription = descriptionLabelContentDescription
                    }
            )
            Text(
                text = AnnotatedString.fromHtml(photo.description),
                style = Typography.bodyLarge,
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    .semantics {
                        contentDescription = descriptionContentDescription
                    }
            )
            Text(
                text = stringResource(id = R.string.photo_author),
                style = Typography.titleLarge,
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    .semantics {
                        contentDescription = authorLabelContentDescription
                    }
            )
            Text(
                text = photo.author,
                style = Typography.bodyLarge,
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    .semantics {
                        contentDescription = authorContentDescription
                    }
            )
            Text(
                text = stringResource(id = R.string.photo_published),
                style = Typography.titleLarge,
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    .semantics {
                        contentDescription = publishedLabelContentDescription
                    }
            )
            Text(
                text = photo.published,
                style = Typography.bodyLarge,
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, bottom = 32.dp)
                    .semantics {
                        contentDescription = publishedContentDescription
                    }
            )
        }
    }
}

@Composable
fun ErrorView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.connection_failed_error),
            modifier = modifier
        )
    }
}

@Composable
fun SearchView(search:String, onSearchChanged: (search:String) -> Unit, modifier: Modifier = Modifier) {
    val searchContentDescription = stringResource(id = R.string.search_content_description)

    TextField(value = search, onValueChange = onSearchChanged, modifier = modifier
        .fillMaxWidth()
        .semantics {
            contentDescription = searchContentDescription
        })
}

@Composable
fun GalleryView(photos: List<GalleryPhotoUI>, modifier: Modifier = Modifier, onClickPhoto: (Int) -> Unit) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start) {

        items(photos.size) { index ->
            PhotoImageView(photo = photos[index]) {
                onClickPhoto(index)
            }
        }
    }
}

@Composable
fun EmptySearchView(modifier: Modifier = Modifier) {
    val searchResultsContentDescription = stringResource(
        id = R.string.no_search_results_content_description)
    
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(id = R.string.no_search_results), modifier = modifier.semantics { 
            contentDescription = searchResultsContentDescription
        })
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun PhotoImageView(photo: GalleryPhotoUI,
                  modifier: Modifier = Modifier,
                  contentScale: ContentScale = ContentScale.FillHeight,
                  onClick: () -> Unit = {}) {
    val contentDescription = stringResource(id = R.string.photo_content_description)+" "+photo.title

    GlideImage(
        model = photo.url,
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier.clickable(onClick = onClick)
    )
}

@Composable
fun LoadingView(modifier: Modifier = Modifier) {
    val loadingContentDescription = stringResource(
        id = R.string.loading_bar_content_description)

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {

        CircularProgressIndicator(modifier = modifier.semantics {
            contentDescription = loadingContentDescription
        })
    }
}