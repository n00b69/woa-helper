package com.woa.helper.util

import com.woa.helper.main.Download

object UpdateChecker {
    fun getRemoteVersion(debug: Boolean): String {
        val path = if (debug) "/debug" else ""
        return Download.text("https://raw.githubusercontent.com/n00b69/woa-helper-update/main$path/README.md")
    }

    fun getChangelog(debug: Boolean): String {
        val path = if (debug) "/debug" else ""
        return Download.text("https://raw.githubusercontent.com/n00b69/woa-helper-update/main$path/changelog.md")
    }
}
