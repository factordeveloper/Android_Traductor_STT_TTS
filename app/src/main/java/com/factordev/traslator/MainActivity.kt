package com.factordev.traslator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.factordev.traslator.ui.TranslatorScreen
import com.factordev.traslator.ui.theme.TraslatorTheme

class MainActivity : ComponentActivity() {
    
    private lateinit var viewModel: TranslatorViewModel
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.initializeSpeechRecognition(this)
        }
        viewModel.checkPermissions(this)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            TraslatorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    viewModel = viewModel()
                    
                    // Inicializar ViewModel cuando se cree por primera vez
                    LaunchedEffect(Unit) {
                        initializeViewModel()
                    }
                    
                    TranslatorScreen(
                        viewModel = viewModel,
                        onRequestPermission = {
                            requestPermissionLauncher.launch(PermissionManager.RECORD_AUDIO_PERMISSION)
                        }
                    )
                }
            }
        }
    }
    
    private fun initializeViewModel() {
        // Verificar permisos al inicializar
        viewModel.checkPermissions(this)
        
        // Inicializar servicios de speech (TTS siempre, STT solo con permisos)
        viewModel.initializeSpeechRecognition(this)
    }
}