package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.data.NutriScanRepository
import com.example.data.local.NutriScanDatabase
import com.example.ui.NutriScanApp
import com.example.ui.NutriScanViewModel
import com.example.ui.theme.NutriScanTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = NutriScanDatabase.getInstance(applicationContext)
        val repository = NutriScanRepository(
            childDao = database.childDao(),
            screeningDao = database.screeningDao(),
            followUpDao = database.followUpDao()
        )

        val viewModel: NutriScanViewModel by viewModels {
            NutriScanViewModel.Factory(repository)
        }

        setContent {
            NutriScanTheme {
                NutriScanApp(viewModel = viewModel)
            }
        }
    }
}
