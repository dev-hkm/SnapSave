package com.snapsave.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.snapsave.app.ui.create.CreateSnippetScreen
import com.snapsave.app.ui.detail.DetailScreen
import com.snapsave.app.ui.home.HomeScreen
import com.snapsave.app.ui.settings.SettingsScreen

object Routes {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val DETAIL = "detail/{id}"
    const val CREATE = "create"
    fun detail(id: Long) = "detail/$id"
}

/** Transition chuẩn Material: trượt-ngang bằng spring + fade nhẹ; hỗ trợ Predictive Back trên Android 13+. */
@Composable
fun AppNavHost() {
    val nav = rememberNavController()
    val spec = spring<IntOffset>(
        stiffness = Spring.StiffnessMediumLow,
        dampingRatio = Spring.DampingRatioNoBouncy
    )

    NavHost(
        navController = nav,
        startDestination = Routes.HOME,
        enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, spec) +
                fadeIn(tween(150))
        },
        exitTransition = { fadeOut(tween(150)) },
        popEnterTransition = { fadeIn(tween(150)) },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, spec) +
                fadeOut(tween(150))
        }
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onOpenDetail = { id -> nav.navigate(Routes.detail(id)) },
                onOpenSettings = { nav.navigate(Routes.SETTINGS) },
                onOpenCreate = { nav.navigate(Routes.CREATE) }
            )
        }
        composable(Routes.CREATE) {
            CreateSnippetScreen(
                onBack = { nav.popBackStack() },
                onCreated = { id ->
                    nav.popBackStack()
                    nav.navigate(Routes.detail(id))
                }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { nav.popBackStack() })
        }
        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { entry ->
            DetailScreen(
                id = entry.arguments?.getLong("id") ?: -1L,
                onBack = { nav.popBackStack() }
            )
        }
    }
}
