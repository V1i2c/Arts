package com.artspath.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.artspath.app.AppGraph
import com.artspath.app.ui.errors.ChaptersScreen
import com.artspath.app.ui.errors.ErrorFormScreen
import com.artspath.app.ui.errors.ErrorPagerScreen
import com.artspath.app.ui.errors.ErrorsHomeScreen
import com.artspath.app.ui.onboarding.OnboardingScreen
import com.artspath.app.ui.plan.PlanScreen
import com.artspath.app.ui.theme.LocalPalette
import com.artspath.app.ui.todo.TaskFormScreen
import com.artspath.app.ui.todo.TodoScreen

object Routes {
    const val ONBOARDING = "onboarding"
    const val DASHBOARD = "dashboard"
    const val TODO = "todo"
    const val PLAN = "plan"
    const val ERRORS = "errors"
    const val CHAPTERS = "errors/subject/{subjectId}"
    const val PAGER = "errors/subject/{subjectId}/chapter/{chapterId}"
    const val ERROR_FORM = "errorForm?errorId={errorId}&subjectId={subjectId}&chapterId={chapterId}"
    const val TASK_FORM = "taskForm?taskId={taskId}"
    const val PLAN_FORM = "planForm?planId={planId}&day={day}"
}

@Composable
fun AppRoot() {
    val nav = rememberNavController()
    val backStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val topLevel = setOf(Routes.DASHBOARD, Routes.TODO, Routes.PLAN, Routes.ERRORS)
    val p = LocalPalette.current

    Scaffold(
        containerColor = p.paper,
        bottomBar = {
            if (currentRoute in topLevel) {
                NavigationBar(containerColor = p.paper, tonalElevation = 0.dp) {
                    bottomItems().forEach { (route, icon, label) ->
                        NavigationBarItem(
                            selected = currentRoute == route,
                            onClick = {
                                nav.navigate(route) {
                                    popUpTo(Routes.DASHBOARD) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = p.accentDeep,
                                selectedTextColor = p.accentDeep,
                                unselectedIconColor = p.inkFaint,
                                unselectedTextColor = p.inkFaint,
                                indicatorColor = p.accentSoft
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = if (AppGraph.onboarded) Routes.DASHBOARD else Routes.ONBOARDING,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    onDone = {
                        AppGraph.onboarded = true
                        nav.navigate(Routes.DASHBOARD) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    }
                )
            }
            composable(Routes.DASHBOARD) { com.artspath.app.ui.dashboard.DashboardScreen() }
            composable(Routes.TODO) {
                TodoScreen(
                    onOpenTask = { taskId -> nav.navigate("taskForm?taskId=$taskId") }
                )
            }
            composable(Routes.PLAN) {
                PlanScreen(
                    onOpenPlan = { planId, day -> nav.navigate("planForm?planId=$planId&day=$day") }
                )
            }
            composable(Routes.ERRORS) {
                ErrorsHomeScreen(
                    onOpenSubject = { subjectId -> nav.navigate("errors/subject/$subjectId") },
                    onAddError = { subjectId, chapterId ->
                        nav.navigate("errorForm?errorId=-1&subjectId=$subjectId&chapterId=$chapterId")
                    }
                )
            }
            composable(
                Routes.CHAPTERS,
                arguments = listOf(navArgument("subjectId") { type = NavType.LongType })
            ) { entry ->
                val subjectId = entry.arguments?.getLong("subjectId") ?: 0L
                ChaptersScreen(
                    subjectId = subjectId,
                    onBack = { nav.popBackStack() },
                    onOpenChapter = { chapterId ->
                        nav.navigate("errors/subject/$subjectId/chapter/$chapterId")
                    },
                    onAddError = { sid, cid ->
                        nav.navigate("errorForm?errorId=-1&subjectId=$sid&chapterId=$cid")
                    }
                )
            }
            composable(
                Routes.PAGER,
                arguments = listOf(
                    navArgument("subjectId") { type = NavType.LongType },
                    navArgument("chapterId") { type = NavType.LongType }
                )
            ) { entry ->
                ErrorPagerScreen(
                    subjectId = entry.arguments?.getLong("subjectId") ?: 0L,
                    chapterId = entry.arguments?.getLong("chapterId") ?: 0L,
                    onBack = { nav.popBackStack() },
                    onEditError = { errorId ->
                        nav.navigate("errorForm?errorId=$errorId&subjectId=-1&chapterId=-1")
                    }
                )
            }
            composable(
                Routes.ERROR_FORM,
                arguments = listOf(
                    navArgument("errorId") { type = NavType.LongType; defaultValue = -1L },
                    navArgument("subjectId") { type = NavType.LongType; defaultValue = -1L },
                    navArgument("chapterId") { type = NavType.LongType; defaultValue = -1L }
                )
            ) { entry ->
                ErrorFormScreen(
                    errorId = entry.arguments?.getLong("errorId") ?: -1L,
                    presetSubjectId = entry.arguments?.getLong("subjectId") ?: -1L,
                    presetChapterId = entry.arguments?.getLong("chapterId") ?: -1L,
                    onDone = { nav.popBackStack() }
                )
            }
            composable(
                Routes.TASK_FORM,
                arguments = listOf(navArgument("taskId") { type = NavType.LongType; defaultValue = -1L })
            ) { entry ->
                TaskFormScreen(
                    taskId = entry.arguments?.getLong("taskId") ?: -1L,
                    onDone = { nav.popBackStack() }
                )
            }
            composable(
                Routes.PLAN_FORM,
                arguments = listOf(
                    navArgument("planId") { type = NavType.LongType; defaultValue = -1L },
                    navArgument("day") { type = NavType.LongType; defaultValue = -1L }
                )
            ) { entry ->
                com.artspath.app.ui.plan.PlanFormScreen(
                    planId = entry.arguments?.getLong("planId") ?: -1L,
                    presetDay = entry.arguments?.getLong("day") ?: -1L,
                    onDone = { nav.popBackStack() }
                )
            }
        }
    }
}

private data class BottomItem(val route: String, val icon: ImageVector, val label: String)

private fun bottomItems() = listOf(
    BottomItem(Routes.DASHBOARD, Icons.Filled.Home, "Dashboard"),
    BottomItem(Routes.TODO, Icons.Filled.CheckCircle, "To Do"),
    BottomItem(Routes.PLAN, Icons.Filled.DateRange, "Plan"),
    BottomItem(Routes.ERRORS, Icons.Filled.MenuBook, "Errors")
)
