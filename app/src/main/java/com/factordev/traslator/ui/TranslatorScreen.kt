package com.factordev.traslator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import com.factordev.traslator.TranslatorViewModel
import com.factordev.traslator.ui.components.ApiConfigDialog
import com.factordev.traslator.ui.components.LanguageSelector
import com.factordev.traslator.ui.components.TranslationCard
import com.factordev.traslator.ui.components.TranslationStatusIndicator
import com.factordev.traslator.ui.components.VoiceButton
import com.factordev.traslator.ui.components.VoiceConfigDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslatorScreen(
    viewModel: TranslatorViewModel = viewModel(),
    onRequestPermission: () -> Unit = {}
) {
    val uiState by viewModel.uiState
    val scrollState = rememberScrollState()
    var showApiDialog by remember { mutableStateOf(false) }
    var showVoiceDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Mostrar errores con Snackbar
    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            // Aquí podrías mostrar un Snackbar si quisieras
            // Por ahora solo limpiaremos el error después de unos segundos
            delay(3000)
            viewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    )
                )
            )
            .verticalScroll(scrollState)
    ) {
        // Barra superior con título
        TopAppBar(
            title = {
                Text(
                    text = "Traductor",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            actions = {
                IconButton(
                    onClick = { showVoiceDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.RecordVoiceOver,
                        contentDescription = "Configurar Voces",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                IconButton(
                    onClick = { showApiDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Configuración",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        // Selector de idiomas
        LanguageSelector(
            sourceLanguage = uiState.sourceLanguage,
            targetLanguage = uiState.targetLanguage,
            availableLanguages = viewModel.getAvailableLanguages(),
            onSourceLanguageChange = { language -> viewModel.updateSourceLanguage(language, context) },
            onTargetLanguageChange = { language -> viewModel.updateTargetLanguage(language, context) },
            onSwapLanguages = viewModel::swapLanguages
        )
        
        // Indicador de estado de traducción
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            TranslationStatusIndicator(
                isTranslating = uiState.isTranslating
            )
        }

        // Espaciador flexible para centrar el botón de voz
        if (uiState.originalText.isEmpty() && uiState.translatedText.isEmpty() && !uiState.isTranslating) {
            Spacer(modifier = Modifier.weight(1f))
        }

                 // Botón de voz principal
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            VoiceButton(
                isListening = uiState.isListening,
                onStartListening = {
                    if (!uiState.hasAudioPermission) {
                        onRequestPermission()
                    } else {
                        viewModel.startListening()
                    }
                },
                onStopListening = viewModel::stopListening
            )
        }

        // Tarjeta de traducción
        TranslationCard(
            originalText = uiState.originalText,
            translatedText = uiState.translatedText,
            isTranslating = uiState.isTranslating,
            sourceLanguage = uiState.sourceLanguage.name,
            targetLanguage = uiState.targetLanguage.name,
            onPlayOriginal = viewModel::playOriginalText,
            onPlayTranslation = viewModel::playTranslation,
            onClearText = viewModel::clearText,
            isSpeakingOriginal = uiState.isSpeakingOriginal,
            isSpeakingTranslation = uiState.isSpeakingTranslation,
            onStopSpeaking = viewModel::stopSpeaking
        )

        // Espaciador para asegurar scroll al final
        Spacer(modifier = Modifier.height(32.dp))

        // Mostrar mensaje de error si existe
        uiState.errorMessage?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        }

        // Mensaje de ayuda cuando no hay texto
        if (uiState.originalText.isEmpty() && !uiState.isListening && !uiState.isTranslating && uiState.errorMessage == null) {
            val helpText = if (!uiState.hasAudioPermission) {
                "Necesitas dar permiso de micrófono para usar la traducción por voz"
            } else {
                "Toca el botón del micrófono y habla para traducir instantáneamente"
            }
            
            Text(
                text = helpText,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 16.dp)
            )
        }

        // Espaciador final para centrar mejor cuando no hay contenido
        if (uiState.originalText.isEmpty() && uiState.translatedText.isEmpty() && !uiState.isTranslating) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
    
    // Diálogo de configuración de API
    ApiConfigDialog(
        showDialog = showApiDialog,
        onDismiss = { showApiDialog = false },
        currentApiKey = "", // Por ahora vacío, se podría guardar en SharedPreferences
        onApiKeyChange = { _ -> },
        onSave = { }
    )
    
    // Diálogo de configuración de voces
    VoiceConfigDialog(
        showDialog = showVoiceDialog,
        onDismiss = { showVoiceDialog = false },
        sourceLanguageName = uiState.sourceLanguage.name,
        targetLanguageName = uiState.targetLanguage.name,
        sourceVoices = uiState.availableSourceVoices,
        targetVoices = uiState.availableTargetVoices,
        selectedSourceVoice = uiState.sourceVoice,
        selectedTargetVoice = uiState.targetVoice,
        onSourceVoiceSelected = viewModel::updateSourceVoice,
        onTargetVoiceSelected = viewModel::updateTargetVoice,
        onTestVoice = { voiceName, sampleText ->
            // Determinar el idioma basado en la voz seleccionada
            val voice = (uiState.availableSourceVoices + uiState.availableTargetVoices)
                .find { it.name == voiceName }
            val languageCode = when (voice?.locale?.language) {
                "es" -> "es"
                "en" -> "en"
                "fr" -> "fr"
                "de" -> "de"
                "it" -> "it"
                "pt" -> "pt"
                "zh" -> "zh"
                "ja" -> "ja"
                else -> "es"
            }
            viewModel.testVoice(voiceName, sampleText, languageCode)
        }
    )
} 