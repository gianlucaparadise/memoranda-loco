package com.gianlucaparadise.memorandaloco.ui.main

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.gianlucaparadise.memorandaloco.activityrecognizer.ActivityRecognizer

class MainViewModel @ViewModelInject constructor(private val activityRecognizer: ActivityRecognizer) : ViewModel() {

    fun startActivityRecognizer() {
        activityRecognizer.start()
    }
}