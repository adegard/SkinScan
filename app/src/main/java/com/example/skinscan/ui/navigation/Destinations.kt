package com.example.skinscan.ui.navigation

/** Central place for every route, so screens never hardcode strings. */
object Destinations {
    const val BODY_MAP = "bodymap"
    const val SESSIONS = "sessions"

    const val SESSION_DETAIL = "session/{sessionId}"
    const val CAPTURE = "capture/{sessionId}/{regionId}"
    const val COMPARE = "compare/{sessionA}"

    const val ARG_SESSION_ID = "sessionId"
    const val ARG_REGION_ID = "regionId"
    const val ARG_SESSION_A = "sessionA"

    fun sessionDetail(sessionId: Long) = "session/$sessionId"
    fun capture(sessionId: Long, regionId: String) = "capture/$sessionId/$regionId"
    fun compare(sessionA: Long) = "compare/$sessionA"
}
