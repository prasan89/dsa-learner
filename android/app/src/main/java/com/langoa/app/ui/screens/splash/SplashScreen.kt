package com.langoa.app.ui.screens.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.langoa.app.ui.theme.LangoaAmber
import com.langoa.app.ui.theme.LangoaAmberLight
import com.langoa.app.ui.theme.LangoaBackground
import com.langoa.app.ui.theme.LangoaOnBackground
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToHome: (languageCode: String) -> Unit,
    onNavigateToLanguagePicker: () -> Unit,
    onNavigateToLogin: () -> Unit,
    authRepository: com.langoa.app.domain.repository.AuthRepository? = null
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
        delay(2200)
        if (authRepository != null) {
            val savedLanguage = authRepository.getSelectedLanguage()
            when {
                authRepository.isLoggedIn() && savedLanguage != null ->
                    onNavigateToHome(savedLanguage)
                authRepository.isLoggedIn() ->
                    onNavigateToLanguagePicker()
                else ->
                    onNavigateToLogin()
            }
        } else {
            onNavigateToLogin()
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.6f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "logoScale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LangoaBackground),
        contentAlignment = Alignment.Center
    ) {
        // Subtle radial glow behind the logo
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            LangoaAmber.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        radius = 700f
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(32.dp)
                .scale(scale * pulse)
        ) {
            Text(
                text = "🏛",
                fontSize = 64.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            brush = Brush.linearGradient(
                                colors = listOf(LangoaAmberLight, LangoaAmber, Color(0xFFC8860A))
                            ),
                            fontWeight = FontWeight.Black,
                            fontSize = 64.sp,
                            letterSpacing = 8.sp
                        )
                    ) {
                        append("LANGOA")
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Build Your World\nThrough Language",
                style = MaterialTheme.typography.titleMedium,
                color = LangoaOnBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "🏙  🏠  🏫  🌾",
                fontSize = 24.sp,
                letterSpacing = 8.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SplashScreenPreview() {
    SplashScreen(
        onNavigateToHome = {},
        onNavigateToLanguagePicker = {},
        onNavigateToLogin = {}
    )
}
