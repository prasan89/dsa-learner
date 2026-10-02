package com.langoa.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.langoa.app.auth.AuthEventBus
import com.langoa.app.navigation.LangoaNavGraph
import com.langoa.app.ui.theme.LangoaTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authEventBus: AuthEventBus

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LangoaTheme {
                LangoaNavGraph(authEventBus = authEventBus)
            }
        }
    }
}
