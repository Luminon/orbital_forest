package space.byeolvit.of.util

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed class UiText {
    data class StringResource(@StringRes val resId: Int) : UiText()
    data class DynamicString(val value: String) : UiText()

    @Composable
    fun asString(): String = when (this) {
        is StringResource -> stringResource(resId)
        is DynamicString -> value
    }
}
