package com.example.robocam
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel:ViewModel() {

    private val _leftJoystickData = MutableStateFlow(Pair(0f, 0f))
    val leftJoystickData = _leftJoystickData.asStateFlow()

    private val _rightJoystickData = MutableStateFlow(Pair(0f, 0f))
    val rightJoystickData = _rightJoystickData.asStateFlow()

    fun setLeftJoystickData(x: Float, y: Float) {
        _leftJoystickData.value = Pair(x, y)
    }

    fun setRightJoystickData(x: Float, y: Float) {
        _rightJoystickData.value = Pair(x, y)
    }
}