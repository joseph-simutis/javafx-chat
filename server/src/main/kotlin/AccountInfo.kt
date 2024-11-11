package io.github.josephsimutis.server

import kotlinx.serialization.Serializable

@Serializable
data class AccountInfo(val passwordHash: String, val admin: Boolean, var banned: Boolean)