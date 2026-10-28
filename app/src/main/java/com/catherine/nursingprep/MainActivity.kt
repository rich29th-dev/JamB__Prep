package com.catherine.nursingprep

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import com.catherine.nursingprep.ui.tabs.DashboardTab
import com.catherine.nursingprep.ui.tabs.ErrorLogTab
import com.catherine.nursingprep.ui.tabs.RoadmapTab
import com.catherine.nursingprep.ui.tabs.RoutineTab
import com.catherine.nursingprep.ui.theme.CathCareTheme
import com.catherine.nursingprep.viewmodel.StudyViewModel

enum class Tab(val label: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Filled.Home),
    ROADMAP("Roadmap", Icons.Filled.Route),
    ROUTINE("Routine", Icons.Filled.Nightlight),
    ERRORS("Errors", Icons.Filled.EditNote),
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CathCareTheme {
                val vm: StudyViewModel = viewModel()
                CathCareApp(vm)
            }
        }
    }
}

@Composable
fun CathCareApp(vm: StudyViewModel) {
    var tab by remember { mutableStateOf(Tab.DASHBOARD) }
    val snackbarHostState = remember { SnackbarHostState() }

    /* toast-style feedback from the ViewModel (session complete, saved, etc.) */
    LaunchedEffect(Unit) {
        vm.messages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                Tab.entries.forEach { t ->
                    NavigationBarItem(
                        selected = tab == t,
                        onClick = { tab = t },
                        icon = { Icon(t.icon, contentDescription = null) },
                        label = { Text(t.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background,
        ) {
            when (tab) {
                Tab.DASHBOARD -> DashboardTab(vm, onNavigate = { tab = it })
                Tab.ROADMAP -> RoadmapTab(vm)
                Tab.ROUTINE -> RoutineTab(vm)
                Tab.ERRORS -> ErrorLogTab(vm)
            }
        }
    }
}
