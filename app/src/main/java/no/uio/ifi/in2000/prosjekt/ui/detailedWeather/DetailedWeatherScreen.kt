package no.uio.ifi.in2000.prosjekt.ui.detailedWeather

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import no.uio.ifi.in2000.prosjekt.R
import no.uio.ifi.in2000.prosjekt.ui.commonUIUtils.DecideWeatherIcon
import no.uio.ifi.in2000.prosjekt.ui.commonUIUtils.LocationForecastViewModel
import no.uio.ifi.in2000.prosjekt.ui.commonUIUtils.OceanForecastViewModel
import no.uio.ifi.in2000.prosjekt.ui.map.MetAlersUiState
import no.uio.ifi.in2000.prosjekt.ui.map.MetAlertViewModel
import no.uio.ifi.in2000.prosjekt.ui.map.formatToNorwegianTime
import java.util.Calendar
import java.util.Locale
import kotlin.math.ceil

/* Screen for detailed weather information about a location. Shows data for 3 days.
*  Parameters: Coordinate of the location, name of the location and nav-controller for popBackstack.
*  Creates three viewmodels, one for location forecast, one for ocean forecast and one for met alerts.*/
@OptIn(ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class
)
@Composable
fun InfoStederScreen(coordinate: String, place: String, navController: NavController,
                     locationForecastViewModel: LocationForecastViewModel = LocationForecastViewModel(coordinate),
                     metAlertViewModel: MetAlertViewModel = viewModel(),
                     oceanForecastViewModel: OceanForecastViewModel = OceanForecastViewModel(coordinate)
) {
    val liste = coordinate.split(",")
    val lat = liste[0].trim()
    val lon = liste[1].trim()
    val metAlert by metAlertViewModel.metAUiState.collectAsState()
    val nrOfAlerts = metAlert.metAlertsCoordinate?.features?.size
    /* Warning icon invisible if no alerts */
    val iconTint = when {
        nrOfAlerts == 0 || nrOfAlerts == null -> Color.Transparent
        else -> Color.Yellow
    }

    /* Fetches alerts for the location */
    metAlertViewModel.getNewDataCoords(lat,lon)
    /* selected option toggles the day which we fetch/display information about */
    var selectedOption by remember { mutableStateOf("I dag") }
    var graphOrTable by remember { mutableStateOf("Tabell") }

    /* Offset which determines which day we fetch information about */
    val offsett = when (selectedOption) {
        "I dag" -> 0
        "I morgen" -> 24
        else -> 48
    }
    val calendar = Calendar.getInstance()
    val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    val twoDaysAhead = Weekday.entries[(dayOfWeek+1)%7]

    Scaffold(
        containerColor = Color(0xFF171729),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF171729),
                    titleContentColor = Color.Black,
                ),
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = R.drawable.havblikktext),
                            contentDescription = "My Image",
                            alignment = Alignment.Center,
                            colorFilter = ColorFilter.tint(Color(0xFFCFE3F3)),
                            modifier = Modifier
                                .size(120.dp)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Return",
                            tint = Color(0xFFCFE3F3)
                        )
                    }
                },
                actions = {/* Toggles met alerts */
                    IconButton(onClick = { metAlertViewModel.togglePopupVisibility() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.warning),
                            contentDescription = "Custom Icon",
                            tint = iconTint,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Color(0xFF171729)
            ){
                Row (
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    /* Creates buttons to toggle between graph and table, and which day to display data about */
                    MultiToggleButton(currentSelection = graphOrTable, toggleStates = listOf("Tabell", "Graf")) {newSelecter ->
                        graphOrTable = newSelecter
                    }
                    MultiToggleButton(currentSelection = selectedOption, toggleStates = listOf("I dag", "I morgen",
                        twoDaysAhead.name
                    )) { newselectedOption ->
                        selectedOption = newselectedOption
                    }
                }
            }
        }
    ) { innerPadding ->
        Column {
            if(nrOfAlerts != null && nrOfAlerts > 0){
                MetAlerts(metAlertViewModel = metAlertViewModel, nrOfAlerts = nrOfAlerts)
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(8.dp)
            ) {
                stickyHeader{
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF171729))
                        .padding(bottom = 8.dp), contentAlignment = Alignment.Center) {
                        Text(place, color = Color(0xFFCFE3F3), fontSize = 22.sp, fontWeight = FontWeight.Medium)
                    }
                }
                item {
                    if(graphOrTable == "Graf"){
                        LandTempCard(
                            locationForecastViewModel = locationForecastViewModel,
                            offsett = offsett
                        )
                    } else {
                        WeatherIconRow(locationForecastViewModel = locationForecastViewModel,
                            offset = offsett,
                        )
                    }
                }
                item {
                    if(graphOrTable == "Graf") {
                        WindCard(
                            locationForecastViewModel = locationForecastViewModel,
                            offsett = offsett
                        )
                    } else {
                        ExpandableWeatherCardLand(locationForecastViewModel = locationForecastViewModel, nr = offsett, day = selectedOption)
                    }
                }
                item {
                    if(graphOrTable == "Graf") {
                        WaterTempCard(
                            oceanForecastViewModel = oceanForecastViewModel,
                            offsett = offsett)
                    } else {
                        ExpandableWeatherCardSea(oceanForecastViewModel, nr = offsett, selectedOption)
                    }
                }
            }
        }
    }
}

@Composable
fun MultiToggleButton(
    currentSelection: String,
    toggleStates: List<String>,
    onToggleChange: (String) -> Unit
) {
    val selectedTint = Color(0xFFCFE3F3)
    val unselectedTint = Color.White

    Row(modifier = Modifier
        .height(IntrinsicSize.Min)
        .border(BorderStroke(1.dp, Color(0xFFCFE3F3)), shape = RoundedCornerShape(10.dp))
        .clip(RoundedCornerShape(10.dp))
    ) {
        toggleStates.forEachIndexed { index, toggleState ->
            val isSelected = currentSelection.lowercase() == toggleState.lowercase()
            val backgroundTint = if (isSelected) selectedTint else unselectedTint
            val textColor = if (isSelected) Color(0xFF171729) else Color(0xFF171729)
            if (index != 0) {
                Divider(
                    color = Color.LightGray,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                )
            }
            Row(
                modifier = Modifier
                    .background(backgroundTint)
                    .padding(vertical = 4.dp, horizontal = 6.dp)
                    .toggleable(
                        value = isSelected,
                        enabled = true,
                        onValueChange = { selected ->
                            if (selected) {
                                onToggleChange(toggleState)
                            }
                        })
            ) {
                Text(toggleState.toCapital(), color = textColor, modifier = Modifier.padding(4.dp), fontSize = 16.sp
                )
            }
        }
    }
}

fun String.toCapital(): String {
    return this.lowercase().replaceFirstChar { it.titlecase(Locale.getDefault())}
}

/* Creates a table displaying the weather information for a location, on land.
*  Parameters: Locationforecastviewmodel, offset which determines which day we fetch information about, and the day. */
@Composable
fun ExpandableWeatherCardLand(locationForecastViewModel: LocationForecastViewModel, nr : Int, day : String) {
    val lfUiState by locationForecastViewModel.lFUiState.collectAsState()
    locationForecastViewModel.makeWeatherList(nr)
    val isLoading by locationForecastViewModel.isLoading.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    if(lfUiState.weatherList.isEmpty()){
        return
    }
    val listSize = lfUiState.weatherList.size
    val compactColums = ceil(listSize.toDouble() / 6).toInt()

    val t1 = when (expanded) {
        true -> 0
        else -> 0
    }
    val t2 = when (expanded) {
        true -> 1
        else -> 6
    }
    val t3 = when (expanded) {
        true -> 2
        else -> 12
    }
    val t4 = when (expanded) {
        true -> 3
        else -> 18
    }
    val indexArray = arrayOf(t1, t2, t3, t4)
    val column1Weight = .2f
    val column2Weight = .3f
    val column3Weight = .25f
    val column4Weight = .25f

    val expandMoreIcon = painterResource(id = R.drawable.expand_more)
    val expandLessIcon = painterResource(id = R.drawable.expand_less)
    val toggleIconPainter = if (expanded) expandLessIcon else expandMoreIcon


    Card(
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 20.dp,
            pressedElevation = 12.dp,
            focusedElevation = 12.dp,
            hoveredElevation = 12.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .clickable(
                onClick = {
                    expanded = !expanded
                }
            ),
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
        ){
            if(isLoading){
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier)
                }
            } else {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween){
                        Box(contentAlignment = Alignment.CenterStart){
                            Text(
                                text = day,
                                modifier = Modifier
                                    .padding(start = 10.dp)
                                    .padding(top = 10.dp),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Start)
                        }
                        Box(contentAlignment = Alignment.CenterEnd){
                            Text(
                                text = "På land",
                                modifier = Modifier
                                    .padding(end = 10.dp)
                                    .padding(top = 10.dp),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        /* Create the header of the table */
                        TableCell(
                            text = "Tid",
                            weight = column1Weight,
                            alignment = TextAlign.Left,
                            title = true
                        )
                        TableCell(text = "Temp", weight = column2Weight, title = true)
                        TableCell(text = "Vind", weight = column3Weight, title = true)
                        TableCell(
                            text = "Retning",
                            weight = column4Weight,
                            alignment = TextAlign.Right,
                            title = true
                        )
                    }
                    Divider(color = Color.LightGray, modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp))
                    var teller = 0
                    for (i in 4-compactColums..<4) { /* Create columns for !extended, 6 hour increments */
                        Row(
                            Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ){
                            TableCell(
                                text = when (i) {
                                    0 -> when (expanded) {
                                        true -> "%02d".format(teller+24-listSize)
                                        else -> "00-06"
                                    }
                                    1 -> when (expanded) {
                                        true -> "%02d".format(teller+24-listSize)
                                        else -> "06-12"
                                    }
                                    2 -> when (expanded) {
                                        true -> "%02d".format(teller+24-listSize)
                                        else -> "12-18"
                                    }
                                    else -> when (expanded) {
                                        true -> "%02d".format(teller+24-listSize)
                                        else -> "18-24"
                                    }
                                },
                                weight = column1Weight,
                                alignment = TextAlign.Left
                            )
                            /* Values are dynamic, weatherList[x] changes based on if the increments are 6 or 1 hour
                            *  When expanded shows the 0, 6, 12 and 18 elements of the list, and !expanded shows 0, 1, 2, 3 */
                            lfUiState.weatherList[indexArray[teller]]?.data?.instant?.details?.get("air_temperature").toString().let { TableCell(text = "$it°", weight = column2Weight) }
                            lfUiState.weatherList[indexArray[teller]]?.data?.instant?.details?.get("wind_speed").toString().let { StatusCell(text = it, weight = column3Weight, boolVind = 1) }
                            lfUiState.weatherList[indexArray[teller]]?.data?.instant?.details?.get("wind_from_direction").toString().let {
                                TableCellImage(text = it, weight = column4Weight, R.drawable.arrow_south)
                            }
                        }
                        Divider(
                            color = Color.LightGray,
                            modifier = Modifier
                                .height(1.dp)
                                .fillMaxHeight()
                                .fillMaxWidth()
                        )
                        teller ++
                    }
                    if (expanded) {
                        repeat(listSize - compactColums) { index -> /* Create the rest of the columns */
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ){
                                TableCell(
                                    text = "%02d".format(index+24+compactColums-listSize),
                                    weight = column1Weight,
                                    alignment = TextAlign.Left
                                )
                                lfUiState.weatherList[index+compactColums]?.data?.instant?.details?.get("air_temperature").toString().let { TableCell(text = "$it°", weight = column2Weight) }
                                lfUiState.weatherList[index+compactColums]?.data?.instant?.details?.get("wind_speed").toString().let { StatusCell(text = it, weight = column3Weight, boolVind = 1) }
                                lfUiState.weatherList[index+compactColums]?.data?.instant?.details?.get("wind_from_direction").toString().let {
                                    TableCellImage(text = it, weight = column4Weight, R.drawable.arrow_south)
                                }
                            }
                            Divider(
                                color = Color.LightGray,
                                modifier = Modifier
                                    .height(1.dp)
                                    .fillMaxHeight()
                                    .fillMaxWidth()
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Time for time")
                        Image(
                            painter = toggleIconPainter,
                            contentDescription = "Expand",
                            modifier = Modifier.size(30.dp),
                            Alignment.CenterStart
                        )
                    }
                }
            }
        }
    }
}
/* Creates a table displaying the weather information for a location, on the ocean.
*  Parameters: Locationforecastviewmodel, offset which determines which day we fetch information about, and the day.*/
@Composable
fun ExpandableWeatherCardSea(oceanForecastViewModel: OceanForecastViewModel, nr : Int, day : String) {
    val ofUiState by oceanForecastViewModel.oFUiState.collectAsState()
    oceanForecastViewModel.makeWeatherList(nr)
    val isLoading by oceanForecastViewModel.isLoading.collectAsState()
    var expanded by remember { mutableStateOf (false) }

    if(ofUiState.oceanWeatherList.isEmpty()){
        return
    }
    val listSize = ofUiState.oceanWeatherList.size
    val compactColumns = ceil(listSize.toDouble()/6).toInt()

    val t1 = when (expanded) {
        true -> 0
        else -> 0
    }
    val t2 = when (expanded) {
        true -> 1
        else -> 6
    }
    val t3 = when (expanded) {
        true -> 2
        else -> 12
    }
    val t4 = when (expanded) {
        true -> 3
        else -> 18
    }
    val indexArray = arrayOf(t1, t2, t3, t4)
    val column1Weight = .2f
    val column2Weight = .3f
    val column3Weight = .25f
    val column4Weight = .25f

    val expandMoreIcon = painterResource(id = R.drawable.expand_more)
    val expandLessIcon = painterResource(id = R.drawable.expand_less)
    val toggleIconPainter = if (expanded) expandLessIcon else expandMoreIcon

    Card(
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 20.dp,
            pressedElevation = 12.dp, // Optional: custom elevation when the card is pressed
            focusedElevation = 12.dp, // Optional: custom elevation when the card is focused
            hoveredElevation = 12.dp  // Optional: custom elevation when the card is hovered over
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .clickable(
                onClick = { expanded = !expanded }
            )
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
        ){
            if(isLoading){
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier)
                }
            } else {
                Column() {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween){
                        Box(contentAlignment = Alignment.CenterStart){
                            Text(
                                text = day,
                                modifier = Modifier
                                    .padding(start = 10.dp)
                                    .padding(top = 10.dp),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Start)
                        }
                        Box(contentAlignment = Alignment.CenterEnd){
                            Text(
                                text = "På havet",
                                modifier = Modifier
                                    .padding(end = 10.dp)
                                    .padding(top = 10.dp),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TableCell(
                            text = "Tid",
                            weight = column1Weight,
                            alignment = TextAlign.Left,
                            title = true
                        )
                        TableCell(text = "Temp", weight = column2Weight, title = true)
                        TableCell(text = "Strøm", weight = column3Weight, title = true)
                        TableCell(
                            text = "Retning",
                            weight = column4Weight,
                            alignment = TextAlign.Right,
                            title = true
                        )
                    }
                    Divider(color = Color.LightGray, modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp))
                    var teller = 0
                    for (i in 4-compactColumns..<4) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ){
                            TableCell(
                                text = when (i) {
                                    0 -> when (expanded) {
                                        true -> "%02d".format(teller+24-listSize)
                                        else -> "00-06"
                                    }
                                    1 -> when (expanded) {
                                        true -> "%02d".format(teller+24-listSize)
                                        else -> "06-12"
                                    }
                                    2 -> when (expanded) {
                                        true -> "%02d".format(teller+24-listSize)
                                        else -> "12-18"
                                    }
                                    else -> when (expanded) {
                                        true -> "%02d".format(teller+24-listSize)
                                        else -> "18-24"
                                    }
                                },
                                weight = column1Weight,
                                alignment = TextAlign.Left
                            )
                            /* Values are dynamic, weatherList[x] changes based on if the increments are 6 or 1 hour
                            *  When expanded shows the 0, 6, 12 and 18 elements of the list, and !expanded shows 0, 1, 2, 3 */
                            ofUiState.oceanWeatherList[indexArray[teller]]?.data?.instant?.details?.get("sea_water_temperature").toString().let { TableCell(text = "$it°", weight = column2Weight) }
                            ofUiState.oceanWeatherList[indexArray[teller]]?.data?.instant?.details?.get("sea_water_speed").toString().let { StatusCell(text = it, weight = column3Weight, boolVind = 0) }
                            ofUiState.oceanWeatherList[indexArray[teller]]?.data?.instant?.details?.get("sea_water_to_direction").toString().let {
                                TableCellImage(text = it, weight = column4Weight, R.drawable.arrow_north)
                            }
                        }
                        Divider(
                            color = Color.LightGray,
                            modifier = Modifier
                                .height(1.dp)
                                .fillMaxHeight()
                                .fillMaxWidth()
                        )
                        teller ++
                    }
                    if (expanded) {
                        repeat(listSize - compactColumns) { index ->
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ){
                                TableCell(
                                    text = "%02d".format(index+24+compactColumns-listSize),
                                    weight = column1Weight,
                                    alignment = TextAlign.Left
                                )
                                ofUiState.oceanWeatherList[index+compactColumns]?.data?.instant?.details?.get("sea_water_temperature").toString().let { TableCell(text = "$it°", weight = column2Weight) }
                                ofUiState.oceanWeatherList[index+compactColumns]?.data?.instant?.details?.get("sea_water_speed").toString().let { StatusCell(text = it, weight = column3Weight, boolVind = 0) }
                                ofUiState.oceanWeatherList[index+compactColumns]?.data?.instant?.details?.get("sea_water_to_direction").toString().let {
                                    TableCellImage(text = it, weight = column4Weight, R.drawable.arrow_north)
                                }
                            }
                            Divider(
                                color = Color.LightGray,
                                modifier = Modifier
                                    .height(1.dp)
                                    .fillMaxHeight()
                                    .fillMaxWidth()
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center, // This arranges the children to be centered horizontally
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Time for time")
                        Image(
                            painter = toggleIconPainter,
                            contentDescription = "Expand",
                            modifier = Modifier.size(30.dp),
                            Alignment.CenterStart
                        )
                    }
                }
            }
        }
    }
}

/* Creates a lazyrow of weather icons for a location. Function takes in the locationforecastviewmodel,
*  and the offset which determines which day. Calls WeatherIconCard for each item in the list. */
@Composable
fun WeatherIconRow(locationForecastViewModel: LocationForecastViewModel, offset : Int){
    val LFUiState by locationForecastViewModel.lFUiState.collectAsState()
    locationForecastViewModel.makeWeatherList(offset)
    val isLoading by locationForecastViewModel.isLoading.collectAsState()

    if(LFUiState.weatherList.isEmpty()){
        return
    }
    val listSize = LFUiState.weatherList.size

    Card(modifier = Modifier.padding(10.dp)){
        if(isLoading){
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier)
            }
        } else {
            LazyRow() {
                items(LFUiState.weatherList.size) { weatherItem ->
                    WeatherIconCard(
                        icon = LFUiState.weatherList[weatherItem]?.data?.next_1_hours?.summary?.get("symbol_code") ?: "cloudy",
                        uv = LFUiState.weatherList[weatherItem]?.data?.instant?.details?.get("ultraviolet_index_clear_sky") ?: 0.0,
                        size = 60, // adjust size according to your requirement
                        padding = 8, // adjust padding according to your requirement
                        temp = LFUiState.weatherList[weatherItem]?.data?.instant?.details?.get("air_temperature") ?: 0.0,
                        time = "%02d".format(weatherItem+24-listSize)
                    )
                }
            }
        }
    }
}
/* Creates a card displaying the hour, weather icon and temperature */
@Composable
fun WeatherIconCard(icon : String,
                    uv: Double,
                    size: Int, padding: Int,
                    temp: Double, time: String){
    Column(
        modifier = Modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DecideWeatherIcon(icon, size = size , padding = padding) /* Calls the function to decide which weather icon to use */
        Text(
            text = "UV: $uv",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
        )
        Text(
            text = "$temp°C",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
        )
        Text(
            text = "$time:00",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

/* Method to create cell for table */
@Composable
fun RowScope.TableCell(
    text: String,
    weight: Float,
    alignment: TextAlign = TextAlign.Center,
    title: Boolean = false
) {
    Text(
        text = text,
        Modifier
            .weight(weight)
            .padding(10.dp),
        fontWeight = if (title) FontWeight.Bold else FontWeight.Normal,
        textAlign = alignment,
    )
}
/* Method to create image for table cell */
@Composable
fun RowScope.TableCellImage(
    text: String,
    weight: Float,
    imageResId: Int
) {
    val floatValue: Float? = text.toFloatOrNull()
    val painter = painterResource(id = imageResId)
    Image(painter = painter, contentDescription = "Direction", Modifier
        .weight(weight)
        .padding(10.dp)
        .rotate(floatValue ?: 0f), alignment = Alignment.Center)
}

/* Method to create cell for table with colored background*/
@Composable
fun RowScope.StatusCell(
    text: String,
    weight: Float,
    alignment: TextAlign = TextAlign.Center,
    boolVind: Int?
) {
    val color1 = when (boolVind) {
        1 -> Color(0xFFF8F7F9)
        else -> Color(0xFF446097)
    }
    val textColor1 = when (boolVind) {
        1 -> Color(0xFF141414)
        else -> Color(0xFFE9E9E9)
    }
    Text(
        text = text,
        Modifier
            .weight(weight)
            .padding(12.dp)
            .background(color1, shape = RoundedCornerShape(50.dp)),
        textAlign = alignment,
        color = textColor1
    )
}

/* Displays the met alerts for a location, calls MetAlertsCard for each item in MetAlertViewModel */
@Composable
fun MetAlerts(metAlertViewModel: MetAlertViewModel, nrOfAlerts : Int){
    val metAlertState by metAlertViewModel.metAUiState.collectAsState()
    val isPopupVisible by metAlertViewModel.isPopupVisible.collectAsState()
    var showInfo by remember {
        mutableStateOf(false)
    }
    if(isPopupVisible){
        Popup(
            alignment = Alignment.TopCenter,
            onDismissRequest = { metAlertViewModel.togglePopupVisibility() }, // Close popup when dismissed
        ) {
            LazyColumn(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(16.dp)
                    .fillMaxHeight(1f)
                    .clickable { metAlertViewModel.togglePopupVisibility() }
            ) {
                items(nrOfAlerts){ metalertNr ->
                    MetAlertsCard(metAlertState, metalertNr)
                }
            }
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
                        .background(Color(0xFF171729))
                ){
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Farevarslene er hentet fra Metrologisk Institutt, og viser farevarsler for angitt posisjon.\n \n" +
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
        }
    }
}

/* Creates a card for one Met alert.
*  Takes the MetAlertUiState and nr as parameter.
*  metAlertState contains the data from the API call, and nr is the number of the alert in the list. */
@Composable
fun MetAlertsCard(metAlertState: MetAlersUiState, nr : Int) {
    var expanded by remember { mutableStateOf (false) }
    val inputString = (metAlertState.metAlertsCoordinate?.features?.get(nr)?.properties?.title).toString()
    val parts = inputString.split(",")
    val result = parts.take(3).joinToString(",").trim()

    val warning = painterResource(id = R.drawable.warning)
    val expandMoreIcon = painterResource(id = R.drawable.expand_more)
    val expandLessIcon = painterResource(id = R.drawable.expand_less)
    val toggleIconPainter = if (expanded) expandLessIcon else expandMoreIcon
    val imageUri = metAlertState.metAlertsCoordinate?.features?.get(nr)?.properties?.resources?.firstOrNull { it.mimeType?.startsWith("image/png") == true }?.uri

    /* Converts the timeinterval of alert start to alert end to Norwegian time */
    val fareStart = metAlertState.metAlertsCoordinate?.features?.get(nr)?.timeinterval?.interval?.get(0)
        ?.let { formatToNorwegianTime(it) }
    val fareSlutt = metAlertState.metAlertsCoordinate?.features?.get(nr)?.timeinterval?.interval?.get(1)
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
    val matrixColor = when("${metAlertState.metAlertsCoordinate?.features?.get(nr)?.properties?.riskMatrixColor}") {
        "Red" -> redGradient
        "Orange" -> orangeGradient
        "Yellow" -> yellowGradient
        else -> greenGradient
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
                onClick = { expanded = !expanded }
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
                        text = "${metAlertState.metAlertsCoordinate?.features?.get(nr)?.properties?.instruction}",
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
                        text = "${metAlertState.metAlertsCoordinate?.features?.get(nr)?.properties?.description}",
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
                        text = "${metAlertState.metAlertsCoordinate?.features?.get(nr)?.properties?.consequences}",
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
                        text = "${metAlertState.metAlertsCoordinate?.features?.get(nr)?.properties?.area}",
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
                        text = "Fra $fareStart \nTil $fareSlutt",
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
                                .fillMaxWidth(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}