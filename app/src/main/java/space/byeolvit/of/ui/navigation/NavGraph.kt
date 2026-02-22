package space.byeolvit.of.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import space.byeolvit.of.OrbitalForestApp
import space.byeolvit.of.data.repository.DocumentRepositoryImpl
import space.byeolvit.of.data.repository.SettingsRepositoryImpl
import space.byeolvit.of.data.source.DocumentDataSource
import space.byeolvit.of.ui.screen.home.HomeScreen
import space.byeolvit.of.ui.screen.home.HomeViewModel
import space.byeolvit.of.ui.screen.launch.LaunchScreen
import space.byeolvit.of.ui.screen.launch.LaunchViewModel
import space.byeolvit.of.ui.screen.select.SelectDocumentScreen
import space.byeolvit.of.ui.screen.select.SelectDocumentViewModel
import space.byeolvit.of.ui.screen.settings.SettingsScreen
import space.byeolvit.of.ui.screen.settings.SettingsViewModel
import space.byeolvit.of.util.isUriPermissionValid

@Composable
fun OrbitalForestNavGraph(navController: NavHostController) {
    val context = LocalContext.current
    val app = context.applicationContext as OrbitalForestApp

    val settingsRepo = remember { SettingsRepositoryImpl(app.appDataStore) }
    val documentRepo = remember { DocumentRepositoryImpl(DocumentDataSource(context)) }

    // null = 아직 DataStore 로딩 중, true = 유효한 URI 있음, false = URI 없거나 무효
    var isUriValid by remember { mutableStateOf<Boolean?>(null) }

    // DataStore 첫 번째 emit을 기다렸다가 판단 (initial = null이면 아직 로딩 중)
    // 영구 권한은 루트 URI에 부여되므로 safRootUri로 검사
    LaunchedEffect(Unit) {
        settingsRepo.safRootUri.collect { uri ->
            isUriValid = if (uri != null) {
                isUriPermissionValid(context, uri)
            } else {
                false
            }
            // 첫 값을 받았으면 더 이상 collect 불필요 — NavHost가 이미 결정됨
            return@collect
        }
    }

    if (isUriValid == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val startDestination = if (isUriValid == true) Screen.Home.route else Screen.Launch.route

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Launch.route) {
            val vm: LaunchViewModel = viewModel(
                factory = LaunchViewModel.Factory(settingsRepo, documentRepo, context)
            )
            LaunchScreen(
                viewModel = vm,
                onSetupComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Launch.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            val vm: HomeViewModel = viewModel(
                factory = HomeViewModel.Factory(settingsRepo, documentRepo)
            )
            HomeScreen(
                viewModel = vm,
                onNavigateToSelect = { navController.navigate(Screen.SelectDocument.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.SelectDocument.route) {
            val vm: SelectDocumentViewModel = viewModel(
                factory = SelectDocumentViewModel.Factory(settingsRepo, documentRepo)
            )
            SelectDocumentScreen(
                viewModel = vm,
                onDocumentSelected = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            val vm: SettingsViewModel = viewModel(
                factory = SettingsViewModel.Factory(settingsRepo, documentRepo, context)
            )
            SettingsScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onFolderReselected = {
                    navController.navigate(Screen.Launch.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
