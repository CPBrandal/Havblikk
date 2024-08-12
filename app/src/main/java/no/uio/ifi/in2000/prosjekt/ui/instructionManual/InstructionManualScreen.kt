package no.uio.ifi.in2000.prosjekt.ui.instructionManual

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import no.uio.ifi.in2000.prosjekt.R


data class InstructionIcons(
    val list : List<InstructionUI> = listOf(
        InstructionUI("0", "Hjemskjerm", Icons.Filled.Home),
        InstructionUI("1", "Kart", Icons.Filled.LocationOn)
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstructionManualScreen(navController : NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF191927)
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
                        Text(text = "Hjelp", color = Color(0xFFBEDDF5))
                    }
                },
                actions = { // For å få teksten sentrert
                    IconButton(
                        onClick = {}
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Transparent
                        )
                        }
                    }
                )
            }
        )
    { innerPadding ->
        Column(
            modifier = Modifier
                .background(
                    Color(0xFF171729)

                )
                .padding(top = 65.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            Spacer(modifier = Modifier.size(12.dp))

            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = "info",
                tint = Color(0xFFBEDDF5),
                modifier = Modifier.size(100.dp)
            )
            Spacer(modifier = Modifier.size(12.dp))
            Text(
                "Velkommen til Havblikk! Denne veiledningen vil hjelpe deg med å navigere gjennom appen og bruke dens funksjoner effektivt. Trykk på et av kortene under for å få mer informasjon om den skjermen.",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp),
                color = Color(0xFFFFFFFF),
                fontSize = 12.sp
            )
            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                columns = GridCells.Fixed(1), // Use 2 columns instead of 4
                horizontalArrangement = Arrangement.spacedBy(16.dp), // Space between columns
                verticalArrangement = Arrangement.spacedBy(16.dp), // Space between rows
                contentPadding = PaddingValues(16.dp), // Padding around the grid
                content = {
                    item {
                        InstructionCard(navController, "0")

                    }
                    item{
                        InstructionCard(navController, "1")
                    }
                    item{
                        Text(
                            "Takk for at du bruker Havblikk. Vi håper denne veiledningen gjør din opplevelse med appen enklere og mer givende. Vi vil takke Meterologisk institutt for data og ikoner, og Havvarsel for data.",
                            textAlign = TextAlign.Center,
                            //style = MaterialTheme.typography.body1,
                            modifier = Modifier.padding(top = 16.dp),
                            color = Color(0xFFFFFFFF),
                            fontSize = 12.sp
                        )
                    }

                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstructionCard(navController : NavController, id: String, instructionUI: InstructionUI = InstructionIcons().list[id.toInt()]) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp), // Add padding to create space around the card
        onClick = {
            navController.navigate("InstructionCard/$id")
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
                text = instructionUI.name,
                fontSize = 12.sp,
                color = Color.Black,

                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold
                ,
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.CenterHorizontally) // Padding inside the column for the text
            )
            Icon(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(50.dp), imageVector = instructionUI.icon, contentDescription = "Icon")

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MapUserManualScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFBEDDF5)
                ),
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(text = "Kartskjerm", color = Color(0xFF191927))
                    }
                },
                actions = { // For å få teksten sentrert
                    IconButton(
                        onClick = {}
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Transparent
                        )
                    }
                }
            )
        },) { innerPadding->
        LazyColumn(Modifier.padding(top = 65.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = {
                items(1){
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center){
                Text("Utforsk hvordan du bruker kartfunksjonene i appen.",textAlign = TextAlign.Center, fontSize = 12.sp)
            }
            ManualStep(
                title = "Søke etter steder",
                description = "Skriv inn i søkefeltet for å gå direkte til et sted på kartet.",
                imagePainter = painterResource(id = R.drawable.search),
                fontWeight = FontWeight.Bold
            )

            ManualStep(
                title = "Gå til din posisjon",
                description = "Trykk på posisjonsikonet for å sentrere kartet på din nåværende posisjon.",
                imagePainter = painterResource(id = R.drawable.mylocationgoogle),
                fontWeight = FontWeight.Bold
            )

            ManualStep(
                title = "Se varsler",
                description = "Trykk på varselikonet for å se alle aktive varsler langs kysten.",
                imagePainter = painterResource(id = R.drawable.warning),
                fontWeight = FontWeight.Bold
            )

            ManualStep(
                title = "Utforske punkter",
                description = "Trykk på et hvilket som helst punkt på kartet for å få mer informasjon om det. Detaljert informasjon inkluderer UV, værvarsel fremover i tid, og mer.",
                imagePainter = painterResource(id = R.drawable.marker),
                fontWeight = FontWeight.Bold
            )
        }
    }
})
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenManuals(navController: NavController){
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFBEDDF5)
                ),
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(text = "Hjemskjerm", color = Color(0xFF191927))
                    }
                },
                actions = { // For å få teksten sentrert
                    IconButton(
                        onClick = {}
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
        LazyColumn(Modifier.padding(top = 65.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = {
                items(1){

            Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Utforsk hvordan du bruker hjemskjermfunksjonene i appen.", textAlign = TextAlign.Center, fontSize = 12.sp)
                }

            ManualStep(
                title = "Legge til et nytt sted",
                description = "Trykk på pluss-tegnet (+) som du finner på hovedskjermen under teksten 'Legg til dine favorittsteder!'. Deretter skriver du inn navnet på stedet du ønsker å legge til.",
                imagePainter = painterResource(id = R.drawable.add),
                fontWeight = FontWeight.Bold
            )

            ManualStep(
                title = "Søke etter et sted",
                description = "Bruk søkefeltet på toppen av hovedskjermen, skriv inn navnet på stedet du ønsker å finne.",
                imagePainter = painterResource(id = R.drawable.search),
                fontWeight = FontWeight.Bold
            )

            ManualStep(
                title = "Administrere favorittsteder",
                description = "Sveip kortet fra høyre til venstre for å vise en søppelbøtte-ikon. Du kan fortsette å dra for å slette eller trykke på ikonet for å bekrefte slettingen.",
                imagePainter = painterResource(id = R.drawable.trashcan),
                fontWeight = FontWeight.Bold
            )

            ManualStep(
                title = "Posisjonsdeling",
                description = "Hvis du har aktivert posisjonsdeling, vil du automatisk se en boks på skjermen som viser din nåværende posisjon. Etter du har gitt tillatelse vil du måtte trykke en gang til på posisjonsikonet for å få opp boksen på skjermen.",
                imagePainter = painterResource(id = R.drawable.mylocationgoogle2),
                fontWeight = FontWeight.Bold
            )

            ManualStep(
                title = "Oppdater data",
                description = "Trykk på hjemknappen nede i hovedskjermen for å oppdatere dataene. Unngå å trykke på denne knappen for mange ganger på veldig kort tid, da appen kan ta lang tid for å laste inn all den nye dataen, noe som vil gjøre brukeropplevelsen dårligere.",
                imagePainter = rememberVectorPainter(Icons.Filled.Home),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.size(16.dp))

        }
    }})
    }
}


@Composable
fun ManualStep(title: String, description: String, imagePainter: Painter, fontWeight : FontWeight = FontWeight.Normal) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        // elevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = imagePainter,
                contentDescription = title,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = fontWeight, fontSize = 12.sp)
                Text(description, fontSize = 12.sp)
            }
        }
    }
}