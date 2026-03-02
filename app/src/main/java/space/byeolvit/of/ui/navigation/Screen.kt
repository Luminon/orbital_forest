package space.byeolvit.of.ui.navigation

sealed class Screen(val route: String) {
    object Launch : Screen("launch")
    object Home : Screen("home")
    object SelectDocument : Screen("select_document")
    object Settings : Screen("settings")
    object Licenses : Screen("licenses")
}
