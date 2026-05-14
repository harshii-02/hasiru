package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.TreeViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.compose.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AddLocation
import com.google.maps.android.compose.clustering.Clustering
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.android.gms.maps.model.UrlTileProvider
import com.google.maps.android.compose.TileOverlay
import com.google.maps.android.heatmaps.HeatmapTileProvider
import java.net.URL

data class TreeItem(
    val itemPosition: LatLng,
    val itemTitle: String,
    val itemSnippet: String,
    val type: String,
    val isEmptyPit: Boolean = false
) : ClusterItem {
    override fun getPosition(): LatLng = itemPosition
    override fun getTitle(): String = itemTitle
    override fun getSnippet(): String = itemSnippet
    override fun getZIndex(): Float? = if (isEmptyPit) 1f else 0f
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    onSignOut: () -> Unit = {},
    onOpenChat: () -> Unit = {},
    onOpenCamera: () -> Unit = {},
    onOpenGuide: () -> Unit = {},
    viewModel: TreeViewModel = viewModel()
) {
    val context = LocalContext.current
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasLocationPermission = isGranted }
    )

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val bangalore = LatLng(12.9716, 77.5946)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(bangalore, 12f)
    }

    var mapProperties by remember {
        mutableStateOf(MapProperties(isMyLocationEnabled = hasLocationPermission))
    }

    LaunchedEffect(hasLocationPermission) {
        mapProperties = mapProperties.copy(isMyLocationEnabled = hasLocationPermission)
    }

    // Observe trees from ViewModel
    val trees by viewModel.trees.collectAsState()
    val clusterItems = trees.map { tree ->
        TreeItem(
            itemPosition = LatLng(tree.latitude, tree.longitude),
            itemTitle = tree.title,
            itemSnippet = tree.snippet,
            type = tree.type,
            isEmptyPit = tree.isEmptyPit
        )
    }

    val totalOxygenScore = remember(trees) { viewModel.calculateOxygenScore(trees) }

    // State for Add Tree Dialog
    var showDialog by remember { mutableStateOf(false) }
    var selectedLatLng by remember { mutableStateOf<LatLng?>(null) }
    
    var treeTitle by remember { mutableStateOf("") }
    var treeSnippet by remember { mutableStateOf("") }
    var treeSpecies by remember { mutableStateOf("") }
    var treeGirth by remember { mutableStateOf("") }
    var treeHealth by remember { mutableStateOf("Healthy") }
    var isEmptyPit by remember { mutableStateOf(false) }

    // Overlay toggles
    var showHeatmap by remember { mutableStateOf(false) }
    var showNdvi by remember { mutableStateOf(false) }

    // Heatmap provider — built from tree positions
    val heatmapProvider = remember(clusterItems) {
        if (clusterItems.isNotEmpty()) {
            HeatmapTileProvider.Builder()
                .data(clusterItems.map { it.getPosition() })
                .radius(50)
                .build()
        } else null
    }

    // NDVI overlay — NASA GIBS MODIS NDVI (Web Mercator compatible, free)
    val ndviTileProvider = remember {
        object : UrlTileProvider(256, 256) {
            override fun getTileUrl(x: Int, y: Int, z: Int): URL? {
                return try {
                    URL("https://gibs.earthdata.nasa.gov/wmts/epsg3857/best/" +
                        "MODIS_Terra_L3_NDVI_Monthly/default/2023-06/" +
                        "GoogleMapsCompatible_Level7/$z/$y/$x.png")
                } catch (e: Exception) { null }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = MapUiSettings(zoomControlsEnabled = false),
            onMapClick = { latLng ->
                selectedLatLng = latLng
                showDialog = true
            }
        ) {
            Clustering(
                items = clusterItems,
                onClusterClick = { false },
                onClusterItemClick = { false },
                clusterItemContent = { item ->
                    if (item.isEmptyPit) {
                        Text("🔴", fontSize = 24.sp)
                    } else {
                        Text("🌳", fontSize = 24.sp)
                    }
                }
            )

            // Heatmap overlay
            if (showHeatmap && heatmapProvider != null) {
                TileOverlay(
                    tileProvider = heatmapProvider,
                    transparency = 0.3f
                )
            }

            // NDVI satellite overlay
            if (showNdvi) {
                TileOverlay(
                    tileProvider = ndviTileProvider,
                    transparency = 0.4f
                )
            }
        }

        // Overlay toggle buttons — top left
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = showHeatmap,
                onClick = { showHeatmap = !showHeatmap },
                label = { Text("🔥 Heatmap", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color(0xFF1B2D1F).copy(alpha = 0.9f),
                    labelColor = Color.White,
                    selectedContainerColor = Color(0xFFBF360C).copy(alpha = 0.85f),
                    selectedLabelColor = Color.White
                )
            )
            FilterChip(
                selected = showNdvi,
                onClick = { showNdvi = !showNdvi },
                label = { Text("🌿 NDVI", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color(0xFF1B2D1F).copy(alpha = 0.9f),
                    labelColor = Color.White,
                    selectedContainerColor = Color(0xFF2E7D32).copy(alpha = 0.85f),
                    selectedLabelColor = Color.White
                )
            )
        }

        // Sign-out button top-right
        IconButton(
            onClick = onSignOut,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = "Sign Out",
                tint = androidx.compose.ui.graphics.Color.White
            )
        }

        // FABs column bottom-right
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.End
        ) {
            FloatingActionButton(
                onClick = onOpenGuide,
                containerColor = Color(0xFFFBC02D),
                contentColor = Color.Black
            ) {
                Text("📖", fontSize = 20.sp)
            }
            FloatingActionButton(
                onClick = onOpenCamera,
                containerColor = Color(0xFF1565C0),
                contentColor = Color.White
            ) {
                Text("📷", fontSize = 20.sp)
            }
            FloatingActionButton(
                onClick = onOpenChat,
                containerColor = Color(0xFF2E7D32),
                contentColor = Color.White
            ) {
                Text("🤖", fontSize = 20.sp)
            }
        }

        // Premium Oxygen Score Card
        Card(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp)
                .width(200.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2D1F).copy(alpha = 0.95f)),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(modifier = Modifier.background(
                Brush.horizontalGradient(listOf(Color(0xFF2E7D32).copy(alpha = 0.1f), Color.Transparent))
            )) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF4CAF50).copy(alpha = 0.2f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("✨", fontSize = 16.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "OXYGEN SCORE",
                            color = Color(0xFF4CAF50),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = String.format("%.1f", totalOxygenScore),
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        "KG CO2 OFFSET / YR",
                        color = Color(0xFF9E9E9E),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { (totalOxygenScore / 1000.0).coerceIn(0.0, 1.0).toFloat() },
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                        color = Color(0xFF4CAF50),
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )
                }
            }
        }

        if (showDialog && selectedLatLng != null) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Add New Tree") },
                text = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isEmptyPit, onCheckedChange = { isEmptyPit = it })
                            Text("Empty Pit Marker")
                        }
                        
                        if (!isEmptyPit) {
                            OutlinedTextField(
                                value = treeTitle,
                                onValueChange = { treeTitle = it },
                                label = { Text("Tree Name") },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = treeSpecies,
                                onValueChange = { treeSpecies = it },
                                label = { Text("Species (Neem, Honge, etc.)") },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = treeGirth,
                                onValueChange = { treeGirth = it },
                                label = { Text("Girth (in cm)") },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = treeHealth,
                                onValueChange = { treeHealth = it },
                                label = { Text("Health Status") },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            )
                        } else {
                            Text("Marking an empty pit for future planting.", fontSize = 12.sp, color = Color.Gray)
                        }
                        
                        OutlinedTextField(
                            value = treeSnippet,
                            onValueChange = { treeSnippet = it },
                            label = { Text("Additional Notes") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            selectedLatLng?.let { latLng ->
                                viewModel.addTree(
                                    latitude = latLng.latitude,
                                    longitude = latLng.longitude,
                                    title = if (isEmptyPit) "Empty Pit" else treeTitle,
                                    snippet = treeSnippet,
                                    type = if (isEmptyPit) "Pit" else "Tree",
                                    species = treeSpecies,
                                    girth = treeGirth.toDoubleOrNull() ?: 0.0,
                                    health = treeHealth,
                                    isEmptyPit = isEmptyPit
                                )
                            }
                            showDialog = false
                            treeTitle = ""
                            treeSnippet = ""
                            treeSpecies = ""
                            treeGirth = ""
                            isEmptyPit = false
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
