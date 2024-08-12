package no.uio.ifi.in2000.prosjekt.ui.detailedWeather

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import co.yml.charts.axis.AxisData
import co.yml.charts.ui.linechart.LineChart
import co.yml.charts.ui.linechart.model.GridLines
import co.yml.charts.ui.linechart.model.IntersectionPoint
import co.yml.charts.ui.linechart.model.Line
import co.yml.charts.ui.linechart.model.LineChartData
import co.yml.charts.ui.linechart.model.LinePlotData
import co.yml.charts.ui.linechart.model.LineStyle
import co.yml.charts.ui.linechart.model.LineType
import co.yml.charts.ui.linechart.model.SelectionHighlightPoint
import co.yml.charts.ui.linechart.model.SelectionHighlightPopUp
import co.yml.charts.ui.linechart.model.ShadowUnderLine
import no.uio.ifi.in2000.prosjekt.ui.commonUIUtils.LocationForecastViewModel
import no.uio.ifi.in2000.prosjekt.ui.commonUIUtils.OceanForecastViewModel

/* Collection of functions related to making charts.
   All the comments for the charts applies for all the charts.
*  For all the graphs: We make a Card which holds the graph and a text explaining the data shown.
*  The card calls the appropriate function to make the graph, sending the viewmodel as parameter.
*  The graph-function creates the graph based on the data from the viewmodel and displays it. */

@Composable
fun WaterTempCard(oceanForecastViewModel: OceanForecastViewModel, offsett: Int){
    Card(modifier = Modifier.padding(10.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp,
        )) {
        Column(
            modifier = Modifier.fillMaxWidth() .background(MaterialTheme.colorScheme.surface),
            horizontalAlignment = Alignment.CenterHorizontally  // Center content horizontally
        ) {
            Text(
                "Havtemperatur",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
            )
            WaterTempChart(oceanForecastViewModel, offsett, "sea_water_temperature")  // Your existing chart composable
        }
    }
}

@Composable
fun WaterTempChart(oceanForecastViewModel: OceanForecastViewModel, offsett: Int, variable: String){
    val oceanForecastUiState by oceanForecastViewModel.oFUiState.collectAsState()
    val isLoading by oceanForecastViewModel.isLoading.collectAsState()
    val pointData = oceanForecastViewModel.linechartMaker(offsett, variable) /* Fetches the point-data from the viewmodel */

    if(oceanForecastUiState.oceanForecastData?.properties?.timeseries?.isEmpty() == true){
        return
    }
    val dateTimeString = oceanForecastUiState.oceanForecastData?.properties?.timeseries?.get(0+offsett)?.time
    val hour = dateTimeString?.substring(11, 13)?.toIntOrNull() ?: 0

    val maxY = pointData.maxByOrNull { it.y }?.y ?: 1f
    val minY = pointData.minByOrNull { it.y }?.y ?: 0f
    val steps = determineSteps(maxY-minY)
    val stepsize = ((maxY - minY) / steps)

    val xAxisData = AxisData.Builder()
        .axisStepSize(40.dp)
        .backgroundColor(Color.Transparent)
        .steps(pointData.size - 1)
        .labelData { i ->
            when (offsett) { /* Have to create empty string to push the first hour to the right on the screen */
                0 -> when(oceanForecastViewModel.startHour) {
                    22 ->if (i == 0)  "    " + "%02d".format(i+ 1) else "%02d".format(i+hour)
                    23 -> if (i == 0)  "    " + "%02d".format(i) else "%02d".format(i+hour)
                    else -> if (i == 0)  "    " + "%02d".format(i+hour) else "%02d".format(i+hour)
                }
                else -> if (i == 0) "    " + "%02d".format(i) else "%02d".format(i)
            }
        }
        .labelAndAxisLinePadding(10.dp)
        .axisLineColor(MaterialTheme.colorScheme.tertiary)
        .axisLabelColor(MaterialTheme.colorScheme.tertiary)
        .build()

    val yAxisData = AxisData.Builder()
        .steps(steps)
        .backgroundColor(Color.Transparent)
        .labelAndAxisLinePadding(30.dp)
        .axisOffset(0.dp)
        .labelData {  i ->
            "%.1f°".format(minY  + (i * stepsize))
        }
        .axisLineColor(MaterialTheme.colorScheme.tertiary)
        .axisLabelColor(MaterialTheme.colorScheme.tertiary)
        .build()

    val lineChartData = LineChartData(
        linePlotData = LinePlotData(
            lines = listOf(
                Line(
                    dataPoints = pointData,
                    LineStyle(
                        alpha = 0.5f,
                        color = Color(0xFF171729),
                        lineType = LineType.Straight(isDotted = false)
                    ),
                    IntersectionPoint(
                        MaterialTheme.colorScheme.tertiary
                    ),
                    SelectionHighlightPoint(color = MaterialTheme.colorScheme.primary),
                    ShadowUnderLine(
                        alpha = 0.5f,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFC4EAFF),
                                Color(0xFF3B9AFF),
                                Color(0xFF2663FF)
                            )
                        )
                    ),
                    SelectionHighlightPopUp()
                )
            ),
        ),
        backgroundColor = MaterialTheme.colorScheme.surface,
        xAxisData = xAxisData,
        yAxisData = yAxisData,
        gridLines = GridLines(color = MaterialTheme.colorScheme.outlineVariant),
        isZoomAllowed = true
    )
    if(isLoading){
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(300.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier)
        }
    } else {
        LineChart(modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
            lineChartData = lineChartData
        )
    }
}

@Composable
fun LandTempCard(locationForecastViewModel: LocationForecastViewModel, offsett: Int){
    Card(modifier = Modifier.padding(10.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp,
        )) {
        Column(
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Lufttemperatur",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
            )
            LandTempChart(locationForecastViewModel, offsett, "air_temperature")
        }
    }
}

@Composable
fun LandTempChart(locationForecastViewModel: LocationForecastViewModel, offsett: Int, variable: String){
    val locationForecastUiState by locationForecastViewModel.lFUiState.collectAsState()
    val pointData = locationForecastViewModel.linechartMaker(offsett, variable)
    val isLoading by locationForecastViewModel.isLoading.collectAsState()

    if(locationForecastUiState.weatherData?.properties?.timeseries?.isEmpty() == true){
        return
    }
    val dateTimeString = locationForecastUiState.weatherData?.properties?.timeseries?.get(0+offsett)?.time
    val hour = dateTimeString?.substring(11, 13)?.toIntOrNull() ?: 0


    val maxY = pointData.maxByOrNull { it.y }?.y ?: 1f
    val minY = pointData.minByOrNull { it.y }?.y ?: 0f
    val steps = determineSteps(maxY-minY)
    val stepsize = ((maxY - minY) / steps).toInt()

    val xAxisData = AxisData.Builder()
        .axisStepSize(40.dp)
        .backgroundColor(Color.Transparent)
        .steps(pointData.size - 1)
        .labelData { i ->
            when (offsett) {
                0 -> when(locationForecastViewModel.startHour) {
                    22 ->if (i == 0)  "    " + "%02d".format(i+ 1) else "%02d".format(i+hour)
                    23 -> if (i == 0)  "    " + "%02d".format(i) else "%02d".format(i+hour)
                    else -> if (i == 0)  "    " + "%02d".format(i+hour) else "%02d".format(i+hour)
                }
                else -> if (i == 0) "    " + "%02d".format(i) else "%02d".format(i)
            }
        }
        .labelAndAxisLinePadding(10.dp)
        .axisLineColor(MaterialTheme.colorScheme.tertiary)
        .axisLabelColor(MaterialTheme.colorScheme.tertiary)
        .build()

    val yAxisData = AxisData.Builder()
        .steps(steps)
        .backgroundColor(Color.Transparent)
        .labelAndAxisLinePadding(30.dp)
        .axisOffset(0.dp)
        .labelData {  i ->
            "%.1f°".format(minY  + (i * stepsize))
        }
        .axisLineColor(MaterialTheme.colorScheme.tertiary)
        .axisLabelColor(MaterialTheme.colorScheme.tertiary)
        .build()

    val lineChartData = LineChartData(
        linePlotData = LinePlotData(
            lines = listOf(
                Line(
                    dataPoints = pointData,
                    LineStyle(
                        alpha = 0.5f,
                        color = Color(0xFF171729),
                        lineType = LineType.Straight(isDotted = false)
                    ),
                    IntersectionPoint(
                        MaterialTheme.colorScheme.tertiary
                    ),
                    SelectionHighlightPoint(color = MaterialTheme.colorScheme.primary),
                    ShadowUnderLine(
                        alpha = 0.5f,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFFF9C4),
                                Color(0xFFFFEB3B),
                                Color(0xFFFFD726)
                            )
                        )
                    ),
                    SelectionHighlightPopUp()
                )
            ),
        ),
        backgroundColor = MaterialTheme.colorScheme.surface,
        xAxisData = xAxisData,
        yAxisData = yAxisData,
        gridLines = GridLines(color = MaterialTheme.colorScheme.outlineVariant),
        isZoomAllowed = true
    )
    if(isLoading){
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(300.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier)
        }
    } else {
        LineChart(modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
            lineChartData = lineChartData
        )
    }
}

@Composable
fun WindCard(locationForecastViewModel: LocationForecastViewModel, offsett: Int){
    Card(modifier = Modifier.padding(10.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp,
        )) {
        Column(
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Vind",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
            )
            WindChart(locationForecastViewModel, offsett, "wind_speed")
        }
    }
}
@Composable
fun WindChart(locationForecastViewModel: LocationForecastViewModel, offsett: Int, variable: String){
    val locationForecastUiState by locationForecastViewModel.lFUiState.collectAsState()
    val pointData = locationForecastViewModel.linechartMaker(offsett, variable)
    val isLoaded by locationForecastViewModel.isLoading.collectAsState()

    if(locationForecastUiState.weatherData?.properties?.timeseries?.isEmpty() == true){
        return
    }
    val dateTimeString = locationForecastUiState.weatherData?.properties?.timeseries?.get(0+offsett)?.time
    val hour = dateTimeString?.substring(11, 13)?.toIntOrNull() ?: 0

    val maxY = pointData.maxByOrNull { it.y }?.y ?: 1f
    val minY = pointData.minByOrNull { it.y }?.y ?: 0f
    val steps = determineSteps(maxY-minY)
    val stepsize = ((maxY - minY) / steps)

    val xAxisData = AxisData.Builder()
        .axisStepSize(40.dp)
        .backgroundColor(Color.Transparent)
        .steps(pointData.size - 1)
        .labelData { i ->
            when (offsett) {
                0 -> when(locationForecastViewModel.startHour) {
                    22 ->if (i == 0)  "    " + "%02d".format(i+ 1) else "%02d".format(i+hour)
                    23 -> if (i == 0)  "    " + "%02d".format(i) else "%02d".format(i+hour)
                    else -> if (i == 0)  "    " + "%02d".format(i+hour) else "%02d".format(i+hour)
                }
                else -> if (i == 0) "    " + "%02d".format(i) else "%02d".format(i)
            }
        }
        .labelAndAxisLinePadding(10.dp)
        .axisLineColor(MaterialTheme.colorScheme.tertiary)
        .axisLabelColor(MaterialTheme.colorScheme.tertiary)
        .build()

    val yAxisData = AxisData.Builder()
        .steps(steps)
        .backgroundColor(Color.Transparent)
        .labelAndAxisLinePadding(30.dp)
        .axisOffset(0.dp)
        .labelData {  i ->
            "%.1f m/s".format(minY  + (i * stepsize))
        }
        .axisLineColor(MaterialTheme.colorScheme.tertiary)
        .axisLabelColor(MaterialTheme.colorScheme.tertiary)
        .build()

    val lineChartData = LineChartData(
        linePlotData = LinePlotData(
            lines = listOf(
                Line(
                    dataPoints = pointData,
                    LineStyle(
                        alpha = 0.5f,
                        color = Color(0xFF171729),
                        lineType = LineType.Straight(isDotted = false)
                    ),
                    IntersectionPoint(
                        MaterialTheme.colorScheme.tertiary
                    ),
                    SelectionHighlightPoint(color = MaterialTheme.colorScheme.primary),
                    ShadowUnderLine(
                        alpha = 0.5f,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFF5F5F5),
                                Color(0xFFE0E0E0),
                                Color(0xFFBDBDBD)
                            )
                        )
                    ),
                    SelectionHighlightPopUp()
                )
            ),
        ),
        backgroundColor = MaterialTheme.colorScheme.surface,
        xAxisData = xAxisData,
        yAxisData = yAxisData,
        gridLines = GridLines(color = MaterialTheme.colorScheme.outlineVariant),
        isZoomAllowed = true
    )
    if(isLoaded){
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(300.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier)
        }
    } else {
        LineChart(modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
            lineChartData = lineChartData
        )
    }
}

/* Function determines steps for the y-axis for the graph based on the difference between the maximum and minimum values. */
fun determineSteps(diff: Float): Int {
    return when {
        diff in 0.1..3.0 -> 10
        else -> diff.toInt()
    }
}