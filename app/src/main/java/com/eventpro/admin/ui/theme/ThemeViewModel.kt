package com.eventpro.admin.ui.theme

import androidx.lifecycle.ViewModel
import com.eventpro.admin.data.preferences.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    val prefs: AppPreferences
) : ViewModel()
