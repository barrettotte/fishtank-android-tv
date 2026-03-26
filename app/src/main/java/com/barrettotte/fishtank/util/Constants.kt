package com.barrettotte.fishtank.util

/** App-wide constants. */
object Constants {

    /** Base URL for the Fishtank REST API. */
    const val API_BASE_URL = "https://api.fishtank.live/v1/"

    /** Default stream server when load balancer doesn't specify one. */
    const val DEFAULT_SERVER = "streams-f.fishtank.live"

    /** Default video quality (maxbps = high, 2mbps = medium, minbps = low). */
    const val DEFAULT_QUALITY = "maxbps"

    /** How often to refresh camera thumbnails in milliseconds. */
    const val THUMBNAIL_REFRESH_MS = 90_000L

    /** How often to update the header clock in milliseconds. */
    const val CLOCK_REFRESH_MS = 1_000L

    /** Number of columns in the camera grid. */
    const val GRID_COLUMNS = 5

}
