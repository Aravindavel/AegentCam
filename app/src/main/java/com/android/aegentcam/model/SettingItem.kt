package com.android.aegentcam.model

sealed class SettingItem {
    data class SegmentedControl(
        var title: String,
        val options: List<String>,
        var selectedOptionPosition: Int,
    ) : SettingItem()

    data class SwitchSetting(
        val title: String,
        var isEnabled: Boolean
    ) : SettingItem()

    data class NavigationSetting(
        val title: String,
        val value: String
    ) : SettingItem()
}
