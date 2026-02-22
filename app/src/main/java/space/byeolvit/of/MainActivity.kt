package space.byeolvit.of

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import space.byeolvit.of.ui.navigation.OrbitalForestNavGraph
import space.byeolvit.of.ui.theme.OrbitalForestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OrbitalForestTheme {
                val navController = rememberNavController()
                OrbitalForestNavGraph(navController = navController)
            }
        }
    }
}
