package com.berlin.homeradar.domain.usecase

import com.berlin.homeradar.domain.repository.HousingRepository
import javax.inject.Inject

/**
 * Use case لمراقبة معلومات حالة المزامنة وإعداداتها.
 *
 * يُرجع Flow يتحدث عند كل تغيير في حالة المزامنة أو إعدادات المستخدم.
 */
class ObserveSyncInfoUseCase @Inject constructor(
    private val repository: HousingRepository,
) {
    operator fun invoke() = repository.observeSyncInfo()
}
