package com.example.skinscan.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.skinscan.ui.bodymap.BodyMapScreen
import com.example.skinscan.ui.capture.CaptureScreen
import com.example.skinscan.ui.compare.CompareScreen
import com.example.skinscan.ui.session.SessionDetailScreen
import com.example.skinscan.ui.sessions.SessionListScreen

@Composable
fun SkinScanNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Destinations.BODY_MAP,
    ) {
        composable(Destinations.BODY_MAP) {
            BodyMapScreen(
                onOpenRegion = { sessionId, regionId ->
                    navController.navigate(Destinations.capture(sessionId, regionId))
                },
                onOpenSessions = { navController.navigate(Destinations.SESSIONS) },
                onOpenSession = { sessionId ->
                    navController.navigate(Destinations.sessionDetail(sessionId))
                },
            )
        }

        composable(Destinations.SESSIONS) {
            SessionListScreen(
                onBack = { navController.popBackStack() },
                onOpenSession = { sessionId ->
                    navController.navigate(Destinations.sessionDetail(sessionId))
                },
            )
        }

        composable(
            route = Destinations.SESSION_DETAIL,
            arguments = listOf(navArgument(Destinations.ARG_SESSION_ID) { type = NavType.LongType }),
        ) { entry ->
            val sessionId = entry.arguments?.getLong(Destinations.ARG_SESSION_ID) ?: return@composable
            SessionDetailScreen(
                sessionId = sessionId,
                onBack = { navController.popBackStack() },
                onOpenRegion = { regionId ->
                    navController.navigate(Destinations.capture(sessionId, regionId))
                },
                onCompare = { navController.navigate(Destinations.compare(sessionId)) },
            )
        }

        composable(
            route = Destinations.CAPTURE,
            arguments = listOf(
                navArgument(Destinations.ARG_SESSION_ID) { type = NavType.LongType },
                navArgument(Destinations.ARG_REGION_ID) { type = NavType.StringType },
            ),
        ) { entry ->
            val sessionId = entry.arguments?.getLong(Destinations.ARG_SESSION_ID) ?: return@composable
            val regionId = entry.arguments?.getString(Destinations.ARG_REGION_ID) ?: return@composable
            CaptureScreen(
                sessionId = sessionId,
                regionId = regionId,
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Destinations.COMPARE,
            arguments = listOf(navArgument(Destinations.ARG_SESSION_A) { type = NavType.LongType }),
        ) { entry ->
            val sessionA = entry.arguments?.getLong(Destinations.ARG_SESSION_A) ?: return@composable
            CompareScreen(
                sessionA = sessionA,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
