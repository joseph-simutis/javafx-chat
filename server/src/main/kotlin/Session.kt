package io.github.josephsimutis.server

import io.github.josephsimutis.common.Connection

class Session(val connection: Connection, var account: Pair<String, AccountInfo>?)