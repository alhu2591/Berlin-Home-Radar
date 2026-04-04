package com.berlin.homeradar.presentation.screen.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.berlin.homeradar.R

@Composable
fun OnboardingRoute(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val completed by viewModel.completed.collectAsStateWithLifecycle()
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
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    stringResource(R.string.onboarding_title),
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(stringResource(R.string.onboarding_body_one))
                Text(stringResource(R.string.onboarding_body_two))
            }
        }
        item {
            OnboardingInfoCard(
                title = stringResource(R.string.onboarding_refresh_title),
                body = stringResource(R.string.onboarding_refresh_body),
            )
        }
        item {
            OnboardingInfoCard(
                title = stringResource(R.string.onboarding_sources_title),
                body = stringResource(R.string.onboarding_sources_body),
            )
        }
        item {
            OnboardingInfoCard(
                title = stringResource(R.string.onboarding_alerts_title),
                body = stringResource(R.string.onboarding_alerts_body),
            )
        }
        item {
            Button(
                onClick = onGetStarted,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.onboarding_get_started))
            }
        }
    }
}

@Composable
private fun OnboardingInfoCard(
    title: String,
    body: String,
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
