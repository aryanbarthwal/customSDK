package com.example.mysdk

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.lang.ref.WeakReference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

actual object PlatformSdkBridge {
    private val isSdkVisible = MutableStateFlow(false)
    private var currentActivityRef: WeakReference<SdkActivity>? = null

    actual fun openSdkScreen() {
        val appContext = AndroidDatabaseContextHolder.appContext
            ?: error("Android SDK context is unavailable. Ensure the library manifest is merged correctly.")
        appContext.startActivity(
            Intent(appContext, SdkActivity::class.java).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP,
                )
            },
        )
    }

    actual fun closeSdkScreen() {
        currentActivityRef?.get()?.let { activity ->
            activity.runOnUiThread {
                activity.finish()
            }
        } ?: run {
            isSdkVisible.value = false
        }
    }

    actual fun isSdkVisible(): Boolean = isSdkVisible.value

    internal fun onActivityCreated(activity: SdkActivity) {
        currentActivityRef = WeakReference(activity)
    }

    internal fun onActivityResumed(activity: SdkActivity) {
        currentActivityRef = WeakReference(activity)
        isSdkVisible.value = true
    }

    internal fun onActivityPaused(activity: SdkActivity) {
        if (currentActivityRef?.get() === activity) {
            isSdkVisible.value = false
        }
    }

    internal fun onActivityDestroyed(activity: SdkActivity) {
        if (currentActivityRef?.get() === activity) {
            currentActivityRef = null
            isSdkVisible.value = false
        }
    }
}

internal class SdkActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PlatformSdkBridge.onActivityCreated(this)
        setContent {
            MaterialTheme {
                SdkScreen(onClose = PlatformSdkBridge::closeSdkScreen)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        PlatformSdkBridge.onActivityResumed(this)
    }

    override fun onPause() {
        PlatformSdkBridge.onActivityPaused(this)
        super.onPause()
    }

    override fun onDestroy() {
        PlatformSdkBridge.onActivityDestroyed(this)
        super.onDestroy()
    }
}

@Composable
internal fun SdkScreen(
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val apiState by produceState<SdkApiUiState>(initialValue = SdkApiUiState.Loading) {
        value = try {
            SdkApiUiState.Content(MySdk.fetchDemoPost())
        } catch (exception: Exception) {
            SdkApiUiState.Error(exception.message ?: "Failed to load demo post")
        }
    }
    var inputText by remember { mutableStateOf("") }
    var savedNames by remember { mutableStateOf<List<NameEntity>>(emptyList()) }
    var databaseError by remember { mutableStateOf<String?>(null) }

    suspend fun refreshNames() {
        try {
            savedNames = MySdk.getSavedNames()
            databaseError = null
        } catch (exception: Exception) {
            databaseError = exception.message ?: "Failed to load saved names"
        }
    }

    LaunchedEffect(Unit) {
        refreshNames()
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = "MySdk",
                    style = MaterialTheme.typography.headlineSmall,
                )
                when (val state = apiState) {
                    SdkApiUiState.Loading -> {
                        Text("Loading demo post...")
                    }

                    is SdkApiUiState.Content -> {
                        Text(
                            text = state.post.title,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = state.post.body,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }

                    is SdkApiUiState.Error -> {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
                Text(
                    text = "Saved names",
                    style = MaterialTheme.typography.titleMedium,
                )
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = { Text("Enter name") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    SdkButton(
                        text = "Insert",
                        onClick = {
                            coroutineScope.launch {
                                MySdk.insertName(inputText)
                                inputText = ""
                                refreshNames()
                            }
                        },
                    )
                    SdkButton(
                        text = "Refresh",
                        onClick = {
                            coroutineScope.launch {
                                refreshNames()
                            }
                        },
                    )
                }
                if (databaseError != null) {
                    Text(
                        text = databaseError ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                if (savedNames.isEmpty()) {
                    Text("No saved names yet.")
                } else {
                    savedNames.forEach { entry ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = entry.value,
                                modifier = Modifier.weight(1f),
                            )
                            SdkButton(
                                text = "Delete",
                                onClick = {
                                    coroutineScope.launch {
                                        SdkStore.names.deleteName(entry.id)
                                        refreshNames()
                                    }
                                },
                            )
                        }
                    }
                }
                SdkButton(
                    text = "Close SDK",
                    onClick = onClose,
                )
            }
        }
    }
}

@Composable
internal fun SdkButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
    ) {
        Text(text)
    }
}

private sealed interface SdkApiUiState {
    data object Loading : SdkApiUiState
    data class Content(val post: DemoPost) : SdkApiUiState
    data class Error(val message: String) : SdkApiUiState
}
