package com.factordev.traslator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.factordev.traslator.api.ApiConfig

@Composable
fun TranslationStatusIndicator(
    isTranslating: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (ApiConfig.USE_LIBRE_TRANSLATE) 
                    MaterialTheme.colorScheme.surfaceVariant 
                else 
                    MaterialTheme.colorScheme.primaryContainer
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = when {
                isTranslating -> Icons.Default.Translate
                ApiConfig.USE_LIBRE_TRANSLATE -> Icons.Default.CloudOff
                else -> Icons.Default.CloudQueue
            },
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = if (ApiConfig.USE_LIBRE_TRANSLATE) 
                MaterialTheme.colorScheme.onSurfaceVariant 
            else 
                MaterialTheme.colorScheme.onPrimaryContainer
        )
        
        Text(
            text = when {
                isTranslating -> "Traduciendo..."
                ApiConfig.USE_LIBRE_TRANSLATE -> "LibreTranslate"
                else -> "Google Translate"
            },
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (ApiConfig.USE_LIBRE_TRANSLATE) 
                MaterialTheme.colorScheme.onSurfaceVariant 
            else 
                MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
} 