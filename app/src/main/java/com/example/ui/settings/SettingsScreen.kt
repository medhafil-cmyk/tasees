package com.example.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.prefs.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(preferencesManager: PreferencesManager, modifier: Modifier = Modifier) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        modifier = modifier
    ) { padding ->
        SettingsSection(preferencesManager = preferencesManager, modifier = Modifier.padding(padding))
    }
}

@Composable
fun SettingsSection(preferencesManager: PreferencesManager, modifier: Modifier = Modifier) {
    val isFahrenheit by preferencesManager.isFahrenheitFlow.collectAsStateWithLifecycle(initialValue = false)
    val backgroundImagePath by preferencesManager.backgroundImagePathFlow.collectAsStateWithLifecycle(initialValue = null)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            scope.launch {
                val localPath = withContext(Dispatchers.IO) {
                    try {
                        val inputStream = context.contentResolver.openInputStream(it)
                        if (inputStream != null) {
                            backgroundImagePath?.let { oldPath ->
                                val oldFile = File(oldPath)
                                if (oldFile.exists()) oldFile.delete()
                            }

                            val file = File(context.filesDir, "bg_${UUID.randomUUID()}.jpg")
                            val outputStream = FileOutputStream(file)
                            inputStream.copyTo(outputStream)
                            inputStream.close()
                            outputStream.close()
                            file.absolutePath
                        } else null
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }
                if (localPath != null) {
                    preferencesManager.setBackgroundImagePath(localPath)
                }
            }
        }
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = stringResource(R.string.use_fahrenheit), style = MaterialTheme.typography.titleMedium)
                Switch(
                    checked = isFahrenheit,
                    onCheckedChange = { checked ->
                        scope.launch {
                            preferencesManager.setFahrenheit(checked)
                        }
                    }
                )
            }
            
            Divider(modifier = Modifier.padding(vertical = 16.dp))
            
            Text(text = stringResource(R.string.background_settings), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { pickImageLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.change_background))
            }
            if (backgroundImagePath != null) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                backgroundImagePath?.let { path ->
                                    val file = File(path)
                                    if (file.exists()) file.delete()
                                }
                            }
                            preferencesManager.setBackgroundImagePath(null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.reset_background))
                }
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))
            
            Text(
                text = stringResource(R.string.weather_data_attribution),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
}
