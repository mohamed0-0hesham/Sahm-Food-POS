package com.coditria.footpos.presentation.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.coditria.footpos.core.designsystem.PosTheme
import com.coditria.footpos.core.designsystem.typography

@Composable
fun SplashScreen() {
    val colors = PosTheme.colors
    Column(
        Modifier.fillMaxSize().background(colors.backgroundPrimary),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Sahm Food", style = PosTheme.typography.largeTitle, color = colors.labelPrimary)
        Text("POS", style = PosTheme.typography.title2, color = colors.labelSecondary)
        Spacer(Modifier.size(40.dp))
        CircularProgressIndicator(color = colors.accent)
    }
}
