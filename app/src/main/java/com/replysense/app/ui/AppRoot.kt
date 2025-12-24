package com.replysense.app.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.replysense.app.vm.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot(vm: AppViewModel) {
    val s = vm.state.collectAsState().value

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = s.tab == 0,
                    onClick = { vm.setTab(0) },
                    label = { Text("Compose") },
                    icon = {}
                )
                NavigationBarItem(
                    selected = s.tab == 1,
                    onClick = { vm.setTab(1) },
                    label = { Text("History") },
                    icon = {}
                )
            }
        }
    ) { pad ->
        if (s.tab == 0) {
            ComposeScreen(vm = vm, paddingValues = pad)
        } else {
            HistoryScreen(vm = vm, paddingValues = pad)
        }
    }
}
