package com.coditria.footpos

import androidx.compose.runtime.Composable
import com.coditria.footpos.core.designsystem.PosAppTheme
import com.coditria.footpos.presentation.root.RootScreen

@Composable
fun App() {
    PosAppTheme {
        RootScreen()
    }
}
