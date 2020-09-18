package dev.l2j.tesla.autobots.utils

import java.util.*

data class CancellationToken(val cancelLambda: () -> Unit, val id: UUID = UUID.randomUUID())