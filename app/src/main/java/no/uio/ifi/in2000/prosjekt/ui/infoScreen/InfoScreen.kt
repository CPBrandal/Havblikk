package no.uio.ifi.in2000.prosjekt.ui.infoScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import no.uio.ifi.in2000.prosjekt.model.InfoObjects
import no.uio.ifi.in2000.prosjekt.R
import no.uio.ifi.in2000.prosjekt.ui.commonUIUtils.InfoUIState

/*
This method calls the InfoCard composable for each item in the list.
The information is directly from Havvarsel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreen(
    navController: NavController,
    infoList : InfoUIState = InfoUIState()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = Color(0xFF171729)
                ),
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFFBEDDF5)
                        )
                    }
                },
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(text = "Lær om havet", color = Color(0xFFBEDDF5))
                        }},
                actions = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Transparent
                        )
                    }
                }
            )
        },

        ) { innerPadding ->

        Column(
            modifier = Modifier
                .background(
                    Color(0xFF171729)

                )
                .padding(top = innerPadding.calculateTopPadding()),

            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            Spacer(modifier = Modifier.height(16.dp))
            val img = painterResource(id = R.drawable.learn)
            Image(
                painter = img,
                contentDescription = "learningHat",
                colorFilter = ColorFilter.tint((Color(0xFFBEDDF5)
                        )
                ),
                modifier = Modifier.size(100.dp)
            )
            /*
            Could have used LazyColumn instead of LazyVerticalGrid
             */
            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                columns = GridCells.Fixed(1), // Use 2 columns instead of 4
                horizontalArrangement = Arrangement.spacedBy(16.dp), // Space between columns
                verticalArrangement = Arrangement.spacedBy(16.dp), // Space between rows
                contentPadding = PaddingValues(16.dp), // Padding around the grid
                content = {
                    items(infoList.listOfInfo) { infodata ->
                        InfoCard(infodata, infodata.id, navController)
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoCard(info: InfoObjects, id: String, navController: NavController) {
    Card(
        modifier = Modifier
            //.fillMaxWidth()
            .padding(10.dp), // Add padding to create space around the card
        onClick = {
            navController.navigate("InfoCard/$id")
        }
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .size(75.dp)
                .background(Color(0xFFBEDDF5))


        )
        {
            Text(
                text = info.name,
                fontSize = 12.sp,
                color = Color.Black,

                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold
                ,
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.CenterHorizontally) // Padding inside the column for the text
            )
            val painter = painterResource(id = info.icon)
            Image(modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(50.dp), painter = painter, contentDescription = "Temperature")

        }
    }
}
