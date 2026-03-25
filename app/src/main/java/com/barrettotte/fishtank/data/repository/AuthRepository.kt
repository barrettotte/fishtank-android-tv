package com.barrettotte.fishtank.data.repository

import com.barrettotte.fishtank.data.api.FishtankApi

/** Repository for authentication operations (login, validate, logout). */
class AuthRepository(
    private val api: FishtankApi,
    private val preferencesRepository: PreferencesRepository,
) {
    // Not implemented yet
}
