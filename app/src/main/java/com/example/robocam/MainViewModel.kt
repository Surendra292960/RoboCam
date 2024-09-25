package com.example.robocam

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class Coordinates(val x: Float, val y: Float)

class MainViewModel:ViewModel() {

    private val coordinates_ = MutableStateFlow(Coordinates(0f, 0f))
    val coordinates = coordinates_.asStateFlow()

    fun setCoordinates(x: Float, y: Float) {
        Log.d("JoyStick setCoordinates", "$x, $y")
        coordinates_.value = Coordinates(x, y)
    }

}