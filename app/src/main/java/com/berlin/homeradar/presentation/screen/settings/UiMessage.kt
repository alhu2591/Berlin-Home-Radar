package com.berlin.homeradar.presentation.screen.settings

import android.content.Context

data class UiMessage(
    val resId: Int,
    val args: List<Any> = emptyList(),
) {
    fun resolve(context: Context): String = context.getString(resId, *args.toTypedArray())
}
