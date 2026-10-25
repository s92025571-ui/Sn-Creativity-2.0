package com.example.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object RouteSplash

@Serializable
object RouteDashboard

@Serializable
data class RouteStudioSection(val sectionId: String)

@Serializable
data class RouteCreatorTool(
    val toolId: String,
    val historyEntryId: Long = -1L
)

@Serializable
object RouteHistory

@Serializable
object RouteSettings
