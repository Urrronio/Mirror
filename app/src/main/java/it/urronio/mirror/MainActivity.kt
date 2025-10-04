package it.urronio.mirror

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import it.urronio.mirror.presentation.navigation.Radio
import it.urronio.mirror.presentation.navigation.RadioList
import it.urronio.mirror.presentation.screen.RadioListScreen
import it.urronio.mirror.presentation.screen.RadioScreen
import it.urronio.mirror.ui.theme.MirrorTheme

class MainActivity : ComponentActivity() {
    override fun onStart() {
        super.onStart()
        // TODO: register receiver
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navcontroller = rememberNavController()
            MirrorTheme {
                NavHost(
                    navController = navcontroller,
                    startDestination = RadioList
                ) {
                    composable<RadioList> {
                        RadioListScreen(
                            onNavigateToRadio = { name ->
                                navcontroller.navigate(route = Radio(deviceName = name))
                            }
                        )
                    }
                    composable<Radio> { be ->
                        val radio: Radio = be.toRoute()
                        RadioScreen(
                            name = radio.deviceName
                        ) {
                            navcontroller.popBackStack()
                        }
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // TODO: unregister receiver
    }
}
