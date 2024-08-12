package no.uio.ifi.in2000.prosjekt.ui.home

import android.annotation.SuppressLint
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.res.vectorResource
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import no.uio.ifi.in2000.prosjekt.R

data class BottomNavigationItem(
    val title : String,
    val selectedIcon : ImageVector,
    val unselectedIcon : ImageVector,
    val hasNews : Boolean,
    val badgeCount : Int? = null
)

/*
Made a method for loading vector drawables
 */
@Composable
fun loadImageVector(resourceId: Int): ImageVector {
    // Use vectorResource for vector drawables
    return ImageVector.vectorResource(resourceId)
}
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunBottomBar(navController: NavController){
    val items = listOf(
        BottomNavigationItem(
            title = "Hjem",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Filled.Home,
            hasNews = false
        ),
        BottomNavigationItem(
            title = "Lær",
            selectedIcon = loadImageVector(resourceId = R.drawable.learn),
            unselectedIcon = loadImageVector(resourceId = R.drawable.learn),
            hasNews = false
        ),
        BottomNavigationItem(
            title = "Kart",
            selectedIcon = Icons.Outlined.LocationOn,
            unselectedIcon = Icons.Outlined.LocationOn,
            hasNews = false
        ),
        BottomNavigationItem(
            title = "Manual",
            selectedIcon = loadImageVector(resourceId = R.drawable.book),
            unselectedIcon = loadImageVector(resourceId = R.drawable.book),
            hasNews = false
        )
    )
    var selectedItemIndex by rememberSaveable {
        mutableIntStateOf(0)
    }
    NavigationBar(containerColor = Color(0xFFCFE3F3)) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                colors = NavigationBarItemDefaults.colors(Color(0xFF1F3468)),
                selected = selectedItemIndex == index,
                onClick = {
                    selectedItemIndex = index
                    navController.navigate(item.title)
                },
                label = { Text(text = item.title) },
                icon = {
                    BadgedBox(
                        badge = {
                            if(item.badgeCount != null){
                                Badge{
                                    Text(text = item.badgeCount.toString())
                                }}
                            else if(item.hasNews){
                                Badge()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (index == selectedItemIndex){
                                item.selectedIcon
                            }
                            else{
                                item.unselectedIcon
                            },
                            contentDescription = item.title

                        )
                    }

                }
            )
        }
    }
}
