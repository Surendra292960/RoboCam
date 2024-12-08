package com.example.robocam
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel:ViewModel() {

    private var leftJoystickData = LeftJoyStickData(0.0f, 0.0f)

    private var rightJoystickData = RightJoyStickData(0.0f, 0.0f)

    val isDialogShowing = MutableLiveData(false)
    val takeScreenShot = MutableLiveData(false)

    private var job: Job? = null

    fun setJoystickData(left: LeftJoyStickData?, right: RightJoyStickData?) {
        if (left!=null){
            leftJoystickData = left
        }
        if (right!=null){
            rightJoystickData = right
        }
        getJoyStickCoordinates()
    }


    private fun getJoyStickCoordinates() {
    job = viewModelScope.launch(IO) {
        Log.d("TAG", "getJoyStickCoordinates start: ")
            while (job!!.isActive) {
                delay(10)
                Log.d("TAG", "setJoyStickCoordinates: Left => ${leftJoystickData.x} ${leftJoystickData.x}  Right => ${rightJoystickData.x} ${rightJoystickData.x}")
                if (nonActive()) {
                    job?.cancelAndJoin()
                    Log.d("TAG", "getJoyStickCoordinates cancel: ")
                }
            }
        }
    }

    private fun nonActive(): Boolean = leftJoystickData.x==0.0f && leftJoystickData.y==0.0f && rightJoystickData.x==0.0f && rightJoystickData.y==0.0f
}