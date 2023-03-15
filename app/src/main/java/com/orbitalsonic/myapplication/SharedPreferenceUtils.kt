package com.orbitalsonic.myapplication

import android.content.SharedPreferences

private const val firstTimeAskingPermission = "firstTimeAskingPermission"

class SharedPreferenceUtils(private val sharedPreferences: SharedPreferences) {

    /* ---------- Permission ---------- */

    var isFirstTimeAskingPermission: Boolean
        get() = sharedPreferences.getBoolean(firstTimeAskingPermission, true)
        set(value) {
            sharedPreferences.edit().apply {
                putBoolean(firstTimeAskingPermission, value)
                apply()
            }
        }
}