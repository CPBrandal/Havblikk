package no.uio.ifi.in2000.prosjekt.ui.infoCards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column


import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color


import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import no.uio.ifi.in2000.prosjekt.model.InfoObjects
import no.uio.ifi.in2000.prosjekt.ui.commonUIUtils.InfoUIState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoCards(
    id : String?,
    navController: NavController,
    info : InfoObjects = InfoUIState().listOfInfo[id!!.toInt()],
) {
    Scaffold(
        modifier = Modifier.background(Color(0xFF171729)),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
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
                title = { Text(text = "") })
        },

        ) { innerPadding ->
        Box(modifier = Modifier
                .fillMaxSize() // This Box fills the entire content area of Scaffold
                .background(Color(0xFF171729))
                .padding(top = innerPadding.calculateTopPadding()), // Background color applied here as well
            contentAlignment = Alignment.TopStart
        ) {

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color(0xFF171729)
                )
                .padding(top = 70.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.Center,
            ) {
            item {
                Column(modifier = Modifier.fillMaxSize().background(Color(0xFF171729))) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(info.img)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp)
                    )
                }
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = info.name,
                        color = Color.White,
                        style = TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif
                        )
                    )
                }
                Spacer(modifier = Modifier.height(5.dp))
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    info.description.let {
                        Text(
                            color = Color.White,
                            text = it,
                            style = TextStyle(
                                fontSize = 11.sp,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Light,
                                lineHeight = 28.sp
                            ),
                            modifier = Modifier.fillMaxWidth().padding(start = 35.dp, end = 35.dp)
                        )
                    }
                }
            }
        }
    }
}}



