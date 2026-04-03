package com.berlin.homeradar.presentation.screen.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun OnboardingRoute(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val completed by viewModel.completed.collectAsState()
    if (completed) {
        onFinished()
        return
    }
    OnboardingScreen(
        onGetStarted = {
            viewModel.complete()
            onFinished()
        }
    )
}

@Composable
fun OnboardingScreen(
    onGetStarted: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Welcome to Berlin Home Radar", style = MaterialTheme.typography.headlineMedium)
        Text("Track housing listings from multiple Berlin sources, save searches, and receive local alerts when new matches appear.")
        Text("Use filters, source management, language selection, and theme settings to tailor the experience to your needs.")
        Button(onClick = onGetStarted) {
            Text("Get started")
        }
    }
}
