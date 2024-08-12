package no.uio.ifi.in2000.prosjekt.ui.map


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mapbox.geojson.Point
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.ViewAnnotationAnchor
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.ViewAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.CircleAnnotation
import com.mapbox.maps.viewannotation.annotationAnchors
import com.mapbox.maps.viewannotation.geometry
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import no.uio.ifi.in2000.prosjekt.R
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

/* MapScreen creates a screen with a map. It takes an activity as a parameter for fusedlocationprovider.
*  Navcontroller is used to navigate to DWSScreen, and MapViewModel is created and used to store center and zoom level of map
*  when changing states.
*  Creates a MapScreenViewModel to store and fetch data from havvarsel API
*  Creates a MetAlertsViewModel to store and fetch data from MetAlerts API
*  Creates a EnTurViewModel to store and fetch data from EnTur API for searchbar suggestions */
@OptIn(MapboxExperimental::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(activity: Activity, navController: NavController, mapScreenViewModel: MapScreenViewModel = viewModel(),
              mapViewModel: MapViewModel = MapViewModel(LocalContext.current),
              metAlertViewModel: MetAlertViewModel = viewModel(),
              enturViewModel: EnTurViewModel = viewModel()) {

    val mViewportState = rememberMapViewportState()
    var showViewAnnotation by remember {
        mutableStateOf(false)
    }
    val mapCameraState by mapViewModel.camera.collectAsState()
    var visible by remember {
        mutableStateOf(true)
    }
    var currentPoint by remember {mutableStateOf(Point.fromLngLat(10.0, 59.0))}
    val context = LocalContext.current.applicationContext
    var lat: String
    var lon: String

    Scaffold(
        topBar = {
            TopAppBar(modifier = Modifier,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF171729),
                    titleContentColor = Color.Black,
                ),
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = R.drawable.havblikktext),
                            contentDescription = "Logo",
                            alignment = Alignment.Center,
                            colorFilter = ColorFilter.tint(Color(0xFFCFE3F3)),
                            modifier = Modifier
                                .size(100.dp)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        mapViewModel.updateMap(mViewportState.cameraState.center, mViewportState.cameraState.zoom)
                        navController.popBackStack() }
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Return",
                            tint = Color(0xFFCFE3F3)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        enturViewModel.toggleVisibility() }) { /* Shows searchbar */
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFFCFE3F3)
                        )
                    }
                }
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier
                .fillMaxSize()){
                /* Creates a mapboxmap using mapbox function to show the map */
                MapboxMap(
                    modifier = Modifier
                        .fillMaxSize(),
                    mapViewportState = mViewportState.apply {
                        setCameraOptions {
                            zoom(mapCameraState.zoom)
                            center(mapCameraState.center)
                            pitch(20.0)
                            bearing(0.0)
                        }
                    },
                    /* Changes mapcenter to clicked currentPoint, updates current-currentPoint
                    *  Shows the weather-card om the map. */
                    onMapClickListener = { clickedPoint ->
                        lat = clickedPoint.latitude().toString()
                        lon = clickedPoint.longitude().toString()
                        mViewportState.setCameraOptions {
                            center(clickedPoint)
                            pitch(0.0)
                            bearing(0.0)
                        }
                        currentPoint = Point.fromLngLat(clickedPoint.longitude(),clickedPoint.latitude())
                        showViewAnnotation = true
                        visible = true
                        mapScreenViewModel.getNewData(lat,lon) /* Gets updated data from havvarsel API */
                        true
                    },
                ){
                    if (showViewAnnotation) {
                        if(visible){
                            CircleAnnotation(currentPoint, circleBlur = 1.0)
                        }
                        ViewAnnotation(
                            options = viewAnnotationOptions {
                                geometry(currentPoint)
                                annotationAnchors(
                                    {
                                        anchor(ViewAnnotationAnchor.BOTTOM_LEFT)
                                        offsetY(50.0)
                                    }
                                )
                                allowOverlap(false)
                                visible(visible)
                            }
                        ) {
                            Box(modifier = Modifier
                                .fillMaxHeight(0.2f)
                                .fillMaxWidth(0.5f)){
                                MapCard(mapScreenViewModel = mapScreenViewModel)
                                IconButton(
                                    onClick = {visible = false},
                                    modifier = Modifier
                                        .size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Close",
                                        tint = Color.DarkGray
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.BottomCenter),
                                    contentAlignment = Alignment.Center
                                ) {
                                    /* Clickable text which navigates to DWSScreen (detailed weather sea)
                                    *  Updates zoom-level and mapcenter to current values */
                                    Text(
                                        "Detaljert varsel",
                                        modifier = Modifier
                                            .clickable {
                                                //mapViewModel.updateZoomLevel(mViewportState.cameraState.zoom)
                                                mapViewModel.updateMap(mViewportState.cameraState.center, mViewportState.cameraState.zoom)
                                                navController.navigate("DetailedWeather/${currentPoint.latitude()},${currentPoint.longitude()}")
                                            }
                                            .padding(8.dp),
                                        fontSize = 10.sp,
                                        textDecoration = TextDecoration.Underline,
                                    )
                                }
                            }
                        }
                    }
                }
                Box(modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .fillMaxHeight(0.1f)
                    .align(Alignment.BottomCenter)
                ){
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .background(
                                Color(0xFF171729).copy(alpha = 0.95f),
                                shape = RoundedCornerShape(20.dp)
                            )
                    ) {
                        /* Toggles metalerts visibility */
                        IconButton(modifier = Modifier.weight(1f) ,onClick = {metAlertViewModel.togglePopupVisibility() }) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "MetAlerts",
                                tint = Color(0xFFF9E784)
                            )
                        }
                        VerticalDivider()
                        /* Sets mapcenter to current location if location permission is granted */
                        IconButton(modifier = Modifier.weight(1f) ,onClick = {
                            getLocationMap(context, activity) { location ->
                                mViewportState.setCameraOptions {
                                    center(Point.fromLngLat(
                                        location.longitude,
                                        location.latitude
                                    ))
                                }
                            }
                        }) {
                            Icon(painter = painterResource(id = R.drawable.mylocationgoogle2),
                                contentDescription = "GoToMyLocation" ,
                                tint = Color(0xFF4694E2)
                            )
                        }
                    }
                }
                SearchBar(enturViewModel = enturViewModel, mapViewModel = mapViewModel)
                MapMetAlerts(metAlertViewModel = metAlertViewModel)
            }
        }
    }
}

@Composable
fun VerticalDivider() {
    Divider(
        modifier = Modifier
            .fillMaxHeight()
            .width(2.dp),
        color = Color.Gray
    )
}

/* Creates a mapcard showing ocean-weather information for the map.
*  Takes mapScreenViewModel as a parameter to fetch data from havvarsel API */
@Composable
fun MapCard(mapScreenViewModel: MapScreenViewModel){
    val dataState by mapScreenViewModel.hVUIState.collectAsState()
    val isLoading by mapScreenViewModel.isLoading.collectAsState()
    Card(
        shape = RoundedCornerShape(15.dp),
        modifier = Modifier.fillMaxSize()
    )
    {
        Box {
            Column(
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .background(Color.White)
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                if(isLoading){
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier)
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val painter = painterResource(id = R.drawable.thermometer)
                            Image(painter = painter, contentDescription = "Temperature", modifier = Modifier.size(18.dp), Alignment.CenterEnd)
                            Column(horizontalAlignment = Alignment.Start) {
                                Text(text = "Sjø-temp", fontFamily = FontFamily.Serif, fontSize = 12.sp)
                                Text(text = "${dataState.dataProjectionMain?.data?.get(0)?.data?.find { it.key == "temperature" }?.value?.toDouble()?.roundToInt()} °C", fontSize = 14.sp)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val painter = painterResource(id = R.drawable.wind)
                            Image(painter = painter, contentDescription = "Wind", modifier = Modifier.size(18.dp), Alignment.CenterEnd)

                            Column(horizontalAlignment = Alignment.Start) {
                                Text(text = "Vind",fontFamily = FontFamily.Serif, fontSize = 12.sp)
                                Text(text = "${dataState.dataProjectionMain?.data?.get(0)?.data?.find { it.key == "wind_length" }?.value?.toDouble()?.roundToInt()} m/s", fontSize = 14.sp)
                            }
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            val painter = painterResource(id = R.drawable.waves)
                            Image(painter = painter, contentDescription = "Current", modifier = Modifier.size(18.dp), Alignment.CenterEnd)
                            Column(horizontalAlignment = Alignment.Start) {
                                Text(text = "Strøm",fontFamily = FontFamily.Serif, fontSize = 12.sp)
                                Text(text = "${String.format("%.1f", dataState.dataProjectionMain?.data?.get(0)?.data?.find { it.key == "current_length" }?.value?.toDouble())} m/s", fontSize = 14.sp)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)){
                            val painter = painterResource(id = R.drawable.salinity)
                            Image(painter = painter, contentDescription = "Salinity", modifier = Modifier.size(18.dp))
                            Column(horizontalAlignment = Alignment.Start) {
                                Text(text = "Salt",fontFamily = FontFamily.Serif, fontSize = 12.sp)
                                Text(text = "${dataState.dataProjectionMain?.data?.get(0)?.data?.find { it.key == "salinity" }?.value?.toDouble()?.roundToInt()} ‰", textAlign = TextAlign.Center, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

/* Function which returns current location if location permission is granted.
*  If not, it requests location permission. */
fun getLocationMap(
    context: Context,
    activity: Activity,
    locationCallback: (Location) -> Unit // Callback function to receive location
) {
    val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    if ((ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED)
    ) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            100
        )
    } else {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    locationCallback(location) // Call the callback with location
                }
            }
        return
    }
}

/* Function for showing current marine metAlerts for the entire Norwegian coast.
*  If internet is off or if the API-call fails, it displays a message.
*  Takes MetAlertViewModel as a parameter to fetch data from MetAlerts API */
@Composable
fun MapMetAlerts(metAlertViewModel: MetAlertViewModel){
    metAlertViewModel.getNewData()
    val metAlertState by metAlertViewModel.metAUiState.collectAsState()
    val isPopupVisible by metAlertViewModel.isPopupVisible.collectAsState()
    val nrOfAlerts = metAlertState.metAlerts?.features?.size
    var showInfo by remember {
        mutableStateOf(false)
    }
    AnimatedVisibility(
        visible = isPopupVisible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(durationMillis = 300)
        ) + fadeIn(animationSpec = tween(durationMillis = 300)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(durationMillis = 300)
        ) + fadeOut(animationSpec = tween(durationMillis = 300))
    ) {
        Box(modifier = Modifier
            .fillMaxSize(), contentAlignment = Alignment.Center)
        {
            Card(shape = RectangleShape,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxSize(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF171729)))
            {
                Row {
                    IconButton(
                        onClick = { metAlertViewModel.togglePopupVisibility()
                            showInfo = false},
                        modifier = Modifier
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Close",
                            tint = Color(0xFFCFE3F3)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = { showInfo = true },
                        modifier = Modifier
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Close",
                            tint = Color(0xFFCFE3F3)
                        )
                    }
                }
                if(showInfo){ /* Displays information about the alerts */
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { showInfo = false }
                    ){
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Farevarslene er hentet fra Metrologisk Institutt, og viser farevarsler langs hele norskekysten.\n \n" +
                                        "Gult nivå brukes om en moderat farlig situasjon, som kan forårsake skader lokalt.\n \n" +
                                        "Oransje nivå brukes om en alvorlig situasjon og været kan føre til alvorlige skader.\n \n" +
                                        "Rødt nivå brukes om en ekstrem situasjon. Dette farenivået forekommer svært sjelden, og kan føre til store skader.",
                                color = Color.White,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Black
                            )
                            Text(modifier = Modifier.padding(20.dp),
                                text = "Klikk på skjermen for å avvise",
                                fontSize = 16.sp,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Black
                            )
                            Text(modifier = Modifier.padding(20.dp),
                                text = "Farenivåer med informasjon er hentet fra Yr.no",
                                fontSize = 14.sp,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
                if(nrOfAlerts != null){
                    if(nrOfAlerts == 0){ /* If there are no alerts, show a message */
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ){
                            Text(
                                text = "Ingen farevarsler funnet for sjøen!",
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Black
                            )
                        }
                    } else { /* Call expandable cards for each alert */
                        LazyColumn(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxSize()
                                .clickable { metAlertViewModel.togglePopupVisibility() }
                        ) {
                            items(nrOfAlerts){ metalertNr ->
                                ExpandableCard(metAlertState, metalertNr)
                            }
                        }
                    }
                } else { /* If there is an error, display message */
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ){
                        Text(
                            text = "Vi har dessverre problemer med å hente data om farevarsler på sjøen",
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

/* Creates a card for one Met alert.
*  Takes the MetAlertUiState and nr as parameter.
*  metAlertState contains the data from the API call, and nr is the number of the alert in the list. */
@Composable
fun ExpandableCard(metAlertState: MetAlersUiState, nr : Int) {
    var expanded by remember { mutableStateOf (false) }
    val inputString = (metAlertState.metAlerts?.features?.get(nr)?.properties?.title).toString()
    val parts = inputString.split(",")
    val result = parts.take(3).joinToString(",").trim()

    val warning = painterResource(id = R.drawable.warning)
    val expandMoreIcon = painterResource(id = R.drawable.expand_more)
    val expandLessIcon = painterResource(id = R.drawable.expand_less)
    val toggleIconPainter = if (expanded) expandLessIcon else expandMoreIcon
    val imageUri = metAlertState.metAlertsCoordinate?.features?.get(nr)?.properties?.resources?.firstOrNull { it.mimeType?.startsWith("image/png") == true }?.uri

    /* Converts the timeinterval of alert start to alert end to Norwegian time */
    val alertStart = metAlertState.metAlerts?.features?.get(nr)?.timeinterval?.interval?.get(0)
        ?.let { formatToNorwegianTime(it) }
    val alertEnd = metAlertState.metAlerts?.features?.get(nr)?.timeinterval?.interval?.get(1)
        ?.let { formatToNorwegianTime(it) }

    val yellowGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFFF9C4),
            Color(0xFFFFEB3B),
            Color(0xFFFFD726)
        )
    )
    val redGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFFCDD2),
            Color(0xFFE57373),
            Color(0xFFD32F2F)
        )
    )
    val orangeGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFFE0B2),
            Color(0xFFFFB74D),
            Color(0xFFFB8C00)
        )
    )
    val greenGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFA5D6A7),
            Color(0xFF66BB6A),
            Color(0xFF2E7D32)
        )
    )
    /* Set correct matrixColor based on riskMatrixColor */
    val matrixColor = when("${metAlertState.metAlerts?.features?.get(nr)?.properties?.riskMatrixColor}") {
        "Red" -> redGradient
        "Orange" -> orangeGradient
        "Green" -> greenGradient
        else -> yellowGradient
    }
    /* Translate color to corresponding Norwegian name */
    val translatedColor = when (matrixColor) {
        redGradient -> "Rødt nivå"
        orangeGradient -> "Oransje nivå"
        greenGradient -> "Grønt nivå"
        else -> "Gult nivå"
    }
    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp,
            pressedElevation = 8.dp,
            focusedElevation = 8.dp,
            hoveredElevation = 8.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .clickable(
                onClick = { expanded = !expanded } /* Toggle expanded */
            ),
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(matrixColor)){
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()){
                    Image(painter = warning, contentDescription = "Warning", modifier = Modifier.size(40.dp), Alignment.Center)
                    Column(modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = result,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = translatedColor,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                    Image(painter = toggleIconPainter, contentDescription = "Expand", modifier = Modifier.size(40.dp), Alignment.CenterStart)
                }
                if (expanded) {
                    Text(
                        text = "Anbefalinger:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(8.dp),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${metAlertState.metAlerts?.features?.get(nr)?.properties?.instruction}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(8.dp)
                    )
                    Text(
                        text = "Beskrivelse:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(8.dp),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${metAlertState.metAlerts?.features?.get(nr)?.properties?.description}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(8.dp)
                    )
                    Text(
                        text = "Konsekvenser:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(8.dp),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${metAlertState.metAlerts?.features?.get(nr)?.properties?.consequences}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(8.dp)
                    )
                    Text(
                        text = "Område:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(8.dp),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${metAlertState.metAlerts?.features?.get(nr)?.properties?.area}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(8.dp)
                    )
                    Text(
                        text = "Tidsperiode:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(8.dp),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Fra $alertStart \nTil $alertEnd",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(8.dp)
                    )
                    if(imageUri != null){ /* Show image if available */
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Loaded Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { },
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

/* Transforms date-format to readable format */
fun formatToNorwegianTime(isoDateTime: String): String {
    val zonedDateTime = ZonedDateTime.parse(isoDateTime)
    val osloZone = zonedDateTime.withZoneSameInstant(ZoneId.of("Europe/Oslo"))
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy 'kl.' HH:mm")
    return osloZone.format(formatter)
}

/* Function creates a searchbar for the mapscreen. EnTurAPI is used for searchbar suggestions.
*  MapViewportState is used to change mapcenter to the searched place */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchBar(enturViewModel : EnTurViewModel, mapViewModel: MapViewModel){
    val isVisible by enturViewModel.isPopupVisible.collectAsState()
    val lUIState by enturViewModel.locationUIState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    var newLocationName by remember{ mutableStateOf("") }

    val scrollState = rememberScrollState()
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(durationMillis = 300)
        ) + fadeIn(animationSpec = tween(durationMillis = 300)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(durationMillis = 300)
        ) + fadeOut(animationSpec = tween(durationMillis = 300))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Card (
                shape = RectangleShape,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxSize(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF171729))
            ){
                Row {
                    IconButton(
                        onClick = {
                            enturViewModel.clearSuggestions()
                            enturViewModel.toggleVisibility()
                            newLocationName = ""
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = "Tilbake",
                            modifier = Modifier.size(40.dp), // Juster størrelsen etter behov
                            tint = Color.White // Juster fargen etter ønske
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.Center ,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxSize()
                ){
                    Column(
                        modifier = Modifier
                            .padding(top = 24.dp)
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        TextField(
                            value = newLocationName,
                            onValueChange = {newLocationName = it
                                enturViewModel.fetchSuggestions(it)
                                if (newLocationName.isNotEmpty()){
                                    enturViewModel.clearSuggestions()
                                }
                            },
                            shape = RoundedCornerShape(20.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                if (newLocationName.isNotEmpty()){
                                    newLocationName = ""
                                    keyboardController?.hide()
                                }
                            }),
                            modifier = Modifier
                                .fillMaxWidth(0.9f),
                            label = { Text("Søk")},
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            )
                        )
                        Column(
                            modifier = Modifier
                                .verticalScroll(scrollState)
                                .padding(3.dp)
                        ) {
                            lUIState.suggestion?.forEach{suggestion->
                                Text(
                                    text = suggestion.properties.label,
                                    modifier = Modifier
                                        .background(Color(0xFFCFE3F3))
                                        .fillMaxWidth(0.8f)
                                        .clickable { /* When clicked toggle searchbar and change mapcenter to selected location */
                                            newLocationName = suggestion.properties.label
                                            enturViewModel.clearSuggestions()
                                            val kordinatString =
                                                suggestion.geometry.coordinates[1] + "," + suggestion.geometry.coordinates[0]
                                            val kordinat = kordinatString.split(",")
                                            val currentPoint = Point.fromLngLat(
                                                kordinat[1].toDouble(),
                                                kordinat[0].toDouble()
                                            )
                                            enturViewModel.toggleVisibility()
                                            mapViewModel.updateMap(currentPoint, 12.0)
//                                            mapViewState.setCameraOptions {
//                                                zoom(12.0)
//                                                center(currentPoint)
//                                            }
                                        }
                                        .padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}