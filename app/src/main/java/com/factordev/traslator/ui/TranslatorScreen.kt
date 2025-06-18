package com.factordev.traslator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import com.factordev.traslator.TranslatorViewModel
import com.factordev.traslator.ui.components.LanguageSelector
import com.factordev.traslator.ui.components.TranslationCard
import com.factordev.traslator.ui.components.VoiceButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslatorScreen(
    viewModel: TranslatorViewModel = viewModel(),
    onRequestPermission: () -> Unit = {}
) {
    val uiState by viewModel.uiState
    val scrollState = rememberScrollState()

    // Mostrar errores con Snackbar
    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            // Aquí podrías mostrar un Snackbar si quisieras
            // Por ahora solo limpiaremos el error después de unos segundos
            kotlinx.coroutines.delay(3000)
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
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        // Selector de idiomas
        LanguageSelector(
            sourceLanguage = uiState.sourceLanguage,
            targetLanguage = uiState.targetLanguage,
            availableLanguages = viewModel.getAvailableLanguages(),
            onSourceLanguageChange = viewModel::updateSourceLanguage,
            onTargetLanguageChange = viewModel::updateTargetLanguage,
            onSwapLanguages = viewModel::swapLanguages
        )

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
} 