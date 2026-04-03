package com.berlin.homeradar.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.berlin.homeradar.data.alerts.SavedSearchAlertEvaluator
import com.berlin.homeradar.domain.usecase.RefreshListingsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ListingsSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val refreshListingsUseCase: RefreshListingsUseCase,
    private val savedSearchAlertEvaluator: SavedSearchAlertEvaluator,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return refreshListingsUseCase("workmanager")
            .fold(
                onSuccess = {
                    savedSearchAlertEvaluator.evaluateAndNotify()
                    Result.success()
                },
                onFailure = { Result.retry() },
            )
    }

    companion object {
        const val WORK_NAME = "listings_sync_worker"
    }
}
