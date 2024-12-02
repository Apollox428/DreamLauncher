package ui.page

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.konyaco.fluent.FluentTheme
import com.konyaco.fluent.component.*
import com.konyaco.fluent.icons.Icons
import com.konyaco.fluent.icons.filled.Send
import com.konyaco.fluent.icons.regular.*
import com.konyaco.fluent.surface.Card
import data.Version
import data.parseDisplayName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.skia.Point
import ui.component.BetterTextField
import viewmodel.AppViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun InstallationsPage(viewModel: AppViewModel) {
    val coroutineScope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize()) {
        val scrollState = rememberScrollState()
        var scrollEnabled by remember { mutableStateOf(true) }
        ScrollbarContainer(
            adapter = rememberScrollbarAdapter(scrollState),
            modifier = Modifier.weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState, scrollEnabled)
//                    .draggable(
//                        orientation = Orientation.Vertical,
//                        state = rememberDraggableState { delta ->
//                            coroutineScope.launch {
//                                scrollState.scrollBy(-delta)
//                            }
//                        },
//                    )
                    .padding(32.dp)
            ) {
                Row(Modifier.fillMaxWidth()) {
                    Text("Installations", style = FluentTheme.typography.title, color = Color(FluentTheme.colors.text.text.primary.value))
                    Spacer(Modifier.weight(1f))
                    AccentButton(onClick = {}) {
                        Icon(Icons.Default.Add, null)
                        Text("Add new installation")
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("Recently opened", style = FluentTheme.typography.subtitle, color = Color(FluentTheme.colors.text.text.primary.value))
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth()) {
                    Text("Your installations", style = FluentTheme.typography.subtitle, color = Color(FluentTheme.colors.text.text.primary.value))
                    Spacer(Modifier.weight(1f))
                    var buttonArrowVisible by remember { mutableStateOf(false) }
                    HyperlinkButton(onClick = {}, modifier = Modifier.animateContentSize(tween(if (buttonArrowVisible) 0 else 100)).onPointerEvent(PointerEventType.Enter) { buttonArrowVisible = true }.onPointerEvent(PointerEventType.Exit) { buttonArrowVisible = false }) {
                        Text("View all")
                        AnimatedVisibility(
                            visible = buttonArrowVisible,
                            enter = expandHorizontally() + fadeIn(),
                            exit = shrinkHorizontally(tween(75)) + fadeOut(tween(75))
                        ) {
                            Icon(Icons.Regular.ChevronRight, null)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Box(Modifier.fillMaxWidth()) {
                    val rowScrollState = rememberLazyListState()
                    val rowStartFadeColor = animateColorAsState(
                        targetValue = if (!rowScrollState.canScrollBackward) Color.Black else Color.Transparent,
                        animationSpec = if (!rowScrollState.canScrollBackward) tween(10) else tween(25)
                    )
                    val rowEndFadeColor = animateColorAsState(
                        targetValue = if (!rowScrollState.canScrollForward) Color.Black else Color.Transparent,
                        animationSpec = if (!rowScrollState.canScrollForward) tween(10) else tween(25)
                    )
                    var scrollToRow by remember { mutableStateOf(0) }
                    LaunchedEffect(scrollToRow) {
                        rowScrollState.animateScrollToItem(scrollToRow)
                    }
                    ScrollbarContainer(
                        adapter = rememberScrollbarAdapter(rowScrollState),
                        modifier = Modifier.fillMaxWidth(1f).height(96.dp),
                        isVertical = false
                    ) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .draggable(
                                    orientation = Orientation.Horizontal,
                                    state = rememberDraggableState { delta ->
                                        coroutineScope.launch {
                                            rowScrollState.scrollBy(-delta)
                                        }
                                    },
                                )
                                .onPointerEvent(PointerEventType.Scroll) {
                                    val scrollDeltaY = rowScrollState.firstVisibleItemIndex + -it.changes.first().scrollDelta.y.roundToInt()
                                    if (scrollDeltaY > 0) scrollToRow = if (scrollToRow == 1 && scrollDeltaY == 1 && it.changes.first().scrollDelta.y.roundToInt() == 1) 0 else scrollDeltaY
                                }
                                .onPointerEvent(PointerEventType.Enter) {
                                    scrollEnabled = false
                                }
                                .onPointerEvent(PointerEventType.Exit) {
                                    scrollEnabled = true
                                }
                                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                                .drawWithContent {
                                    val fadeColors = listOf(
                                        rowStartFadeColor.value,
                                        Color.Black,
                                        Color.Black,
                                        Color.Black,
                                        Color.Black,
                                        Color.Black,
                                        Color.Black,
                                        Color.Black,
                                        rowEndFadeColor.value
                                    )
                                    drawContent()
                                    drawRect(
                                        brush = Brush.horizontalGradient(fadeColors),
                                        blendMode = BlendMode.DstIn
                                    )
                                },
                            state = rowScrollState,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(viewModel.launcherInstance.installations) { installation ->
                                Card(
                                    onClick = {

                                    },
                                    modifier = Modifier
                                        .size(204.dp, 96.dp)

                                ) {
                                    Text(installation.displayName)
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth()) {
                    Text("Newly added", style = FluentTheme.typography.subtitle, color = Color(FluentTheme.colors.text.text.primary.value))
                    Spacer(Modifier.weight(1f))
                    var buttonArrowVisible by remember { mutableStateOf(false) }
                    HyperlinkButton(onClick = {}, modifier = Modifier.animateContentSize(tween(if (buttonArrowVisible) 0 else 100)).onPointerEvent(PointerEventType.Enter) { buttonArrowVisible = true }.onPointerEvent(PointerEventType.Exit) { buttonArrowVisible = false }) {
                        Text("View more")
                        AnimatedVisibility(
                            visible = buttonArrowVisible,
                            enter = fadeIn() + expandHorizontally(),
                            exit = fadeOut(tween(75)) + shrinkHorizontally(tween(75))
                        ) {
                            Icon(Icons.Regular.ChevronRight, null)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Box(Modifier.fillMaxWidth()) {
                    val rowScrollState = rememberLazyListState()
                    val rowStartFadeColor = animateColorAsState(
                        targetValue = if (!rowScrollState.canScrollBackward) Color.Black else Color.Transparent,
                        animationSpec = if (!rowScrollState.canScrollBackward) tween(10) else tween(25)
                    )
                    val rowEndFadeColor = animateColorAsState(
                        targetValue = if (!rowScrollState.canScrollForward) Color.Black else Color.Transparent,
                        animationSpec = if (!rowScrollState.canScrollForward) tween(10) else tween(25)
                    )
                    var scrollToRow by remember { mutableStateOf(0) }
                    LaunchedEffect(scrollToRow) {
                        rowScrollState.animateScrollToItem(scrollToRow)
                    }
                    ScrollbarContainer(
                        adapter = rememberScrollbarAdapter(rowScrollState),
                        modifier = Modifier.fillMaxWidth(1f).height(96.dp),
                        isVertical = false
                    ) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .draggable(
                                    orientation = Orientation.Horizontal,
                                    state = rememberDraggableState { delta ->
                                        coroutineScope.launch {
                                            rowScrollState.scrollBy(-delta)
                                        }
                                    },
                                )
                                .onPointerEvent(PointerEventType.Scroll) {
                                    val scrollDeltaY = rowScrollState.firstVisibleItemIndex + -it.changes.first().scrollDelta.y.roundToInt()
                                    if (scrollDeltaY > 0) scrollToRow = if (scrollToRow == 1 && scrollDeltaY == 1 && it.changes.first().scrollDelta.y.roundToInt() == 1) 0 else scrollDeltaY
                                }
                                .onPointerEvent(PointerEventType.Enter) {
                                    scrollEnabled = false
                                }
                                .onPointerEvent(PointerEventType.Exit) {
                                    scrollEnabled = true
                                }
                                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                                .drawWithContent {
                                    val fadeColors = listOf(
                                        rowStartFadeColor.value,
                                        Color.Black,
                                        Color.Black,
                                        Color.Black,
                                        Color.Black,
                                        Color.Black,
                                        Color.Black,
                                        Color.Black,
                                        rowEndFadeColor.value
                                    )
                                    drawContent()
                                    drawRect(
                                        brush = Brush.horizontalGradient(fadeColors),
                                        blendMode = BlendMode.DstIn
                                    )
                                },
                            state = rowScrollState,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(viewModel.launcherInstance.versions.take(10)) { version ->
                                Card(
                                    onClick = {

                                    },
                                    modifier = Modifier
                                        .size(204.dp, 96.dp)

                                ) {
                                    Text(parseDisplayName(version))
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth()) {
                    Text("Versions", style = FluentTheme.typography.subtitle, color = Color(FluentTheme.colors.text.text.primary.value))
                    Spacer(Modifier.weight(1f))
                    var value by remember { mutableStateOf(TextFieldValue()) }
                    BetterTextField(value, onValueChange = { value = it }, trailingIcon = { Icon(Icons.Default.Search, null) }, modifier = Modifier.width(256.dp), singleLine = true, placeholder = { Text("Search versions...") })
                    Spacer(Modifier.width(8.dp))
                    DropDownButton(
                        onClick = {  },
                        content = { Icon(Icons.Default.Filter, contentDescription = null, modifier = Modifier.size(24.dp)) }
                    )
                }
                Spacer(Modifier.height(16.dp))
                val displayedVersions = remember { mutableStateOf(viewModel.launcherInstance.versions.filter { !it.isInstalled }) }
                Column(Modifier.fillMaxWidth().height(1024.dp)) {
                    val gridScrollState = rememberLazyGridState()
                    val gridTopFadeColor = animateColorAsState(
                        targetValue = if (!gridScrollState.canScrollBackward) Color.Black else Color.Transparent,
                        animationSpec = if (!gridScrollState.canScrollBackward) tween(10) else tween(25)
                    )
                    val gridBottomFadeColor = animateColorAsState(
                        targetValue = if (!gridScrollState.canScrollForward) Color.Black else Color.Transparent,
                        animationSpec = if (!gridScrollState.canScrollForward) tween(10) else tween(25)
                    )
                    ScrollbarContainer(
                        adapter = rememberScrollbarAdapter(gridScrollState),
                        modifier = Modifier.weight(1f)
                    ) {
                        LazyVerticalGrid(
                            modifier = Modifier
                                .weight(1f)
//                                .draggable(
//                                    orientation = Orientation.Vertical,
//                                    state = rememberDraggableState { delta ->
//                                        coroutineScope.launch {
//                                            gridScrollState.scrollBy(-delta)
//                                        }
//                                    },
//                                )
                                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                                .drawWithContent {
                                    val fadeColors = listOf(
                                        gridTopFadeColor.value,
                                        Color.Black,
                                        Color.Black,
                                        Color.Black,
                                        Color.Black,
                                        Color.Black,
                                        Color.Black,
                                        Color.Black,
                                        gridBottomFadeColor.value
                                    )
                                    drawContent()
                                    drawRect(
                                        brush = Brush.verticalGradient(fadeColors),
                                        blendMode = BlendMode.DstIn
                                    )
                                },
                            columns = GridCells.Adaptive(minSize = 172.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            state = gridScrollState
                        ) {
                            items(displayedVersions.value) { version ->
                                Card(
                                    onClick = {

                                    },
                                    modifier = Modifier
                                        .size(172.dp, 172.dp)

                                ) {
                                    Text(parseDisplayName(version))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}