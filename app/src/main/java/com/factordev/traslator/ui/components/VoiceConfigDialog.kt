package com.factordev.traslator.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factordev.traslator.VoiceInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceConfigDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    sourceLanguageName: String,
    targetLanguageName: String,
    sourceVoices: List<VoiceInfo>,
    targetVoices: List<VoiceInfo>,
    selectedSourceVoice: String?,
    selectedTargetVoice: String?,
    onSourceVoiceSelected: (String) -> Unit,
    onTargetVoiceSelected: (String) -> Unit,
    onTestVoice: (String, String) -> Unit // voiceName, sampleText
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Configurar Voces",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(500.dp)
                ) {
                    val scrollState = rememberScrollState()
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(scrollState)
                    ) {
                        // Sección de Interlocutor 1 (Idioma original)
                        Text(
                            text = "Interlocutor 1 - $sourceLanguageName",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        
                        if (sourceVoices.isEmpty()) {
                            Text(
                                text = "No hay voces disponibles para este idioma",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                            )
                        } else {
                            Column(
                                modifier = Modifier.selectableGroup()
                            ) {
                                sourceVoices.forEach { voice ->
                                    VoiceSelectionItem(
                                        voice = voice,
                                        isSelected = voice.name == selectedSourceVoice,
                                        onSelected = { onSourceVoiceSelected(voice.name) },
                                        onTest = { 
                                            onTestVoice(voice.name, "Hola, soy el interlocutor uno")
                                        }
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Sección de Interlocutor 2 (Idioma traducido)
                        Text(
                            text = "Interlocutor 2 - $targetLanguageName",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        
                        if (targetVoices.isEmpty()) {
                            Text(
                                text = "No hay voces disponibles para este idioma",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                            )
                        } else {
                            Column(
                                modifier = Modifier.selectableGroup()
                            ) {
                                targetVoices.forEach { voice ->
                                    VoiceSelectionItem(
                                        voice = voice,
                                        isSelected = voice.name == selectedTargetVoice,
                                        onSelected = { onTargetVoiceSelected(voice.name) },
                                        onTest = { 
                                            val sampleText = when (voice.locale.language) {
                                                "en" -> "Hello, I am the second speaker"
                                                "fr" -> "Bonjour, je suis le deuxième interlocuteur"
                                                "de" -> "Hallo, ich bin der zweite Sprecher"
                                                "it" -> "Ciao, sono il secondo interlocutore"
                                                "pt" -> "Olá, eu sou o segundo interlocutor"
                                                "zh" -> "你好，我是第二个发言者"
                                                "ja" -> "こんにちは、私は2番目の話者です"
                                                else -> "Hello, I am the second speaker"
                                            }
                                            onTestVoice(voice.name, sampleText)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cerrar")
                }
            }
        )
    }
}

@Composable
private fun VoiceSelectionItem(
    voice: VoiceInfo,
    isSelected: Boolean,
    onSelected: () -> Unit,
    onTest: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onSelected,
                role = Role.RadioButton
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = null
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = voice.displayName,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
            )
            
            if (voice.isNetworkConnectionRequired) {
                Text(
                    text = "Requiere conexión",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        IconButton(
            onClick = onTest,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Probar voz",
                modifier = Modifier.size(20.dp)
            )
        }
    }
} 