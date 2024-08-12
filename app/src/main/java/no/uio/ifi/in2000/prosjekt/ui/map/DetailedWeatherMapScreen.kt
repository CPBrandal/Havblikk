package no.uio.ifi.in2000.prosjekt.ui.map

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import no.uio.ifi.in2000.prosjekt.R
import no.uio.ifi.in2000.prosjekt.ui.detailedWeather.ExpandableWeatherCardSea
import no.uio.ifi.in2000.prosjekt.ui.commonUIUtils.LocationForecastViewModel
import no.uio.ifi.in2000.prosjekt.ui.commonUIUtils.OceanForecastViewModel
import no.uio.ifi.in2000.prosjekt.ui.commonUIUtils.getCurrentHour
import no.uio.ifi.in2000.prosjekt.ui.detailedWeather.StatusCell
import no.uio.ifi.in2000.prosjekt.ui.detailedWeather.TableCell
import no.uio.ifi.in2000.prosjekt.ui.detailedWeather.TableCellImage
import no.uio.ifi.in2000.prosjekt.ui.detailedWeather.WeatherIconRow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.ceil

/* Creates a screen which displays the weather information for the given location called from mapscreen.
*  Recieves data from the OceanForecastViewModel and LocationForecastViewModel */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailedWeatherMapScreen(
    navController: NavController,
    coordinate: String,
    oceanForecastViewModel: OceanForecastViewModel = OceanForecastViewModel(coordinate),
    locationForecastViewModel: LocationForecastViewModel = LocationForecastViewModel(coordinate)
) {
    Scaffold(
        containerColor = Color(0xFF171729),
        topBar = {
            TopAppBar(
                modifier = Modifier,
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
                                .size(100.dp)
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
                actions = {
                    Box(modifier = Modifier, contentAlignment = Alignment.Center) {
                        Text(getCurrentDateInNorwegianFormat(), color = Color(0xFFCFE3F3))
                    }
                }
            )
        },
        bottomBar = {
            val coordinateList = coordinate.split(",").map { it.trim() }
            val lat = coordinateList.getOrNull(0)?.toDoubleOrNull()
            val long = coordinateList.getOrNull(1)?.toDoubleOrNull()

            val formattedLat = lat?.let { String.format("%.6f", it) } ?: "N/A"
            val formattedLong = long?.let { String.format("%.6f", it) } ?: "N/A"
            Box(modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp), contentAlignment = Alignment.Center){
                Text(text = "$formattedLat, $formattedLong", color = Color.White, fontSize = 12.sp)

            }
        }
    ) { innerpadding ->
        LazyColumn(modifier = Modifier.padding(innerpadding)) {
            item {
                WeatherIconRow(locationForecastViewModel = locationForecastViewModel, offset = 0)
            }
            item {
                ExpandableWeatherCardSea(
                    oceanForecastViewModel = oceanForecastViewModel,
                    nr = 0,
                    day = getCurrentDateInNorwegianFormat()
                )
            }
            item {
                Box(modifier = Modifier.height(150.dp)) {
                    Row {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)
                        ){
                            WaveHeigth(oceanForecastViewModel)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)
                        ){
                            UvIndex(locationForecastViewModel)
                        }
                    }
                }
            }
            item {
                Wind(locationForecastViewModel, getCurrentDateInNorwegianFormat())
            }
        }
    }
}

/* Displays the current waveheight and the max wavehight with the corresponding hour.
*  Finds the current hour by using the LocalTime.now() function, to fetch correct data for
* for the current hour.
* Recieves data from the LocationForecastViewModel*/
@Composable
fun WaveHeigth(oceForViewModel: OceanForecastViewModel) {
    val map by oceForViewModel.waveMap.collectAsState()
    val isLoading by oceForViewModel.isLoading.collectAsState()

    val currentHour = getCurrentHour()
    val currentHourString = currentHour.toString()

    val painter = painterResource(id = R.drawable.waveheight)

    Card(
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .padding(6.dp)
            .fillMaxSize()
    ) {
        if(isLoading){ // Display a loading sign while we fetch data from the API.
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier)
            }
        } else {
            Row(modifier = Modifier.padding(20.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Bølgetopp",
                        style = TextStyle(
                            color = Color.Black,
                            fontSize = 16.sp
                        )
                    )
                    Text(
                        text = "Nå - ${map[currentHourString]} m",
                        style = TextStyle(
                            color = Color.Black,
                            fontSize = 14.sp
                        )
                    )
                    Text(
                        text = "Max - ${map["max"]} m",
                        style = TextStyle(
                            color = Color(0xFF446097),
                            fontSize = 10.sp
                        )
                    )
                    Text(
                        text = "(Kl. ${map["tid"]})",
                        style = TextStyle(
                            color = Color(0xFF446097),
                            fontSize = 10.sp
                        )
                    )
                }
                IconButton(
                    onClick = { },
                    enabled = false,
                    modifier = Modifier
                        .background(
                            color = Color(0xFF446097),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .fillMaxHeight()
                ) {
                    Image(painter = painter, contentDescription = "Waveheight", colorFilter = ColorFilter.tint(Color.White), modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

/* Displays the current ultraviolet index for the given location (from DSWScreen)
*  Recieves data from the LocationForecastViewModel */
@Composable
fun UvIndex(locForViewModel: LocationForecastViewModel) {
    val lfUiState by locForViewModel.lFUiState.collectAsState()
    val isLoading by locForViewModel.isLoading.collectAsState()
    val painter = painterResource(id = R.drawable.uv)

    Card(
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .padding(6.dp)
            .fillMaxSize()
    ) {
        if(isLoading){  // Display a loading sign while we fetch data from the API.
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier)
            }
        } else {
            Row(modifier = Modifier.padding(20.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "UV-Index",
                        style = TextStyle(
                            color = Color.Black,
                            fontSize = 16.sp
                        )
                    )
                    /* From what we can tell, the API usually gives data for 2 hours before now,
                    *  only when the current time passes midnight we sometime see an offset of 3 hours.
                    *  However at that time the UV is irrelevant, so we simplify by just adding 2 in get()*/
                    Text(
                        text = lfUiState.weatherData?.properties?.timeseries?.get(2)?.data?.instant?.details?.get("ultraviolet_index_clear_sky").toString(),
                        style = TextStyle(
                            color = Color.Black,
                            fontSize = 14.sp
                        )
                    )
                    Text(
                        text = uvToText(lfUiState.weatherData?.properties?.timeseries?.get(2)?.data?.instant?.details?.get("ultraviolet_index_clear_sky")),
                        // Call uvToText to get corresponding text for the UV index
                        style = TextStyle(
                            color = Color.Black,
                            fontSize = 12.sp
                        )
                    )
                }
                IconButton(
                    onClick = { },
                    enabled = false,
                    modifier = Modifier
                        .background(
                            color = Color(0xFFF9E784),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .fillMaxHeight()
                ) {
                    Image(painter = painter, contentDescription = "UV")
                }
            }
        }
    }
}

/* Function to convert the UV index to a text description. */
fun uvToText(uvIndex: Double?): String{
    if(uvIndex == null){
        return "UV-index ikke tilgjengelig"
    }
    return when (uvIndex) {
        in 0.0..2.9 -> "Lavt nivå"
        in 3.0..5.9 -> "Moderat nivå"
        in 6.0..7.9 -> "Høyt nivå"
        in 8.0..10.9 -> "Svært høyt nivå"
        else -> "Ekstremt nivå"
    }
}


/* Makes a table displaying wind and wind-gusts for
*  the given location (from DSWScreen) for the current day, hour for hour.
*  Gets the data from the LocationForecastViewModel
*  makeWeatherList makes a list of weather objects for the current day*/
@Composable
fun Wind(locForViewModel: LocationForecastViewModel, time: String){
    val locFor by locForViewModel.lFUiState.collectAsState()
    locForViewModel.makeWeatherList(0)
    val isLoading by locForViewModel.isLoading.collectAsState()
    var expanded by remember { mutableStateOf (false) }
    if(locFor.weatherList.isEmpty()){
        return
    }
    val a = locFor.weatherList.size
    val res = ceil(a.toDouble() / 6).toInt()

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
                onClick = { expanded = !expanded } /* Extend or collapse the card when clicked */
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (isLoading) { /* Display a loading sign while we fetch data from the API. */
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier)
                }
            } else {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(contentAlignment = Alignment.CenterStart) {
                            Text(
                                text = time, /* Displays the current time */
                                modifier = Modifier
                                    .padding(start = 10.dp)
                                    .padding(top = 10.dp),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Start
                            )
                        }
                        Box(contentAlignment = Alignment.CenterEnd) {
                            Text(
                                text = "Vind",
                                modifier = Modifier
                                    .padding(end = 10.dp)
                                    .padding(top = 10.dp),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    Row(Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        /* Table header */
                        TableCell(
                            text = "Tid",
                            weight = column1Weight,
                            alignment = TextAlign.Left,
                            title = true
                        )
                        TableCell(text = "Vind", weight = column2Weight, title = true)
                        TableCell(text = "Vindkast", weight = column3Weight, title = true)
                        TableCell(
                            text = "Retning",
                            weight = column4Weight,
                            alignment = TextAlign.Right,
                            title = true
                        )
                    }
                    Divider(
                        color = Color.LightGray, modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                    )
                    var counter = 0
                    for (i in 4-res..<4) { /* Display rows in 6 hour increments */
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ){
                            TableCell(
                                text = when (i) {
                                    0 -> when (expanded) {
                                        true -> "%02d".format(counter+24-a)
                                        else -> "00-06"
                                    }
                                    1 -> when (expanded) {
                                        true -> "%02d".format(counter+24-a)
                                        else -> "06-12"
                                    }
                                    2 -> when (expanded) {
                                        true -> "%02d".format(counter+24-a)
                                        else -> "12-18"
                                    }
                                    else -> when (expanded) {
                                        true -> "%02d".format(counter+24-a)
                                        else -> "18-24"
                                    }
                                },
                                weight = column1Weight,
                                alignment = TextAlign.Left
                            )
                            /* Values are dynamic, weatherList[x] changes based on if the increments are 6 or 1 hour
                            *  When expanded shows the 0, 6, 12 and 18 elements of the list, and !expanded shows 0, 1, 2, 3 */
                            locFor.weatherList[indexArray[counter]]?.data?.instant?.details?.get("wind_speed").toString().let { TableCell(text = it, weight = column2Weight) }
                            locFor.weatherList[indexArray[counter]]?.data?.instant?.details?.get("wind_speed_of_gust").toString().let { StatusCell(text = it, weight = column3Weight, boolVind = 1) }
                            locFor.weatherList[indexArray[counter]]?.data?.instant?.details?.get("wind_from_direction").toString().let {
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
                        counter ++
                    }
                    if (expanded) {
                        repeat(a - res) { index -> /* Makes a table for the remaining hours */
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ){
                                TableCell(
                                    text = "%02d".format(index+24+res-a),
                                    weight = column1Weight,
                                    alignment = TextAlign.Left
                                )
                                locFor.weatherList[index+res]?.data?.instant?.details?.get("wind_speed").toString().let { TableCell(text = it, weight = column2Weight) }
                                locFor.weatherList[index+res]?.data?.instant?.details?.get("wind_speed_of_gust").toString().let { StatusCell(text = it, weight = column3Weight, boolVind = 1) }
                                locFor.weatherList[index+res]?.data?.instant?.details?.get("wind_from_direction").toString().let {
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

/* Function to get the current time, in norwegian time format */
fun getCurrentDateInNorwegianFormat(): String {
    return try {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd. MMMM", Locale("no"))
        currentDate.format(formatter)
    } catch (e: Exception) {
        "" /* Return an empty string in case of failure */
    }
}