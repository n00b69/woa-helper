package com.woa.helper.util

import com.woa.helper.main.Download

sealed class UpdateResult {
    data class Available(val version: String, val changelog: String) : UpdateResult()
    data object UpToDate : UpdateResult()
    data object Skipped : UpdateResult()
}

object UpdateChecker {
    const val UPDATE_URL = "https://github.com/n00b69/woa-helper/releases/tag/APK"

    fun check(
        currentVersion: String,
        debug: Boolean,
        rootGranted: Boolean,
        networkAvailable: Boolean,
        autoUpdateDisabled: Boolean,
        manual: Boolean
    ): UpdateResult {
        if (!manual) {
            if (!rootGranted || autoUpdateDisabled) return UpdateResult.Skipped
        }
        if (!networkAvailable) return UpdateResult.Skipped

        val remoteVersion = getRemoteVersion(debug)
        if (remoteVersion.isEmpty()) return UpdateResult.Skipped

        if (currentVersion == remoteVersion) return UpdateResult.UpToDate

        val changelog = getChangelog(debug)
        return UpdateResult.Available(remoteVersion, changelog)
    }

    private fun getRemoteVersion(debug: Boolean): String {
        val path = if (debug) "/debug" else ""
        return Download.text("https://raw.githubusercontent.com/n00b69/woa-helper-update/main$path/README.md").trim()
    }

    private fun getChangelog(debug: Boolean): String {
        val path = if (debug) "/debug" else ""
        return Download.text("https://raw.githubusercontent.com/n00b69/woa-helper-update/main$path/changelog.md")
    }
}
