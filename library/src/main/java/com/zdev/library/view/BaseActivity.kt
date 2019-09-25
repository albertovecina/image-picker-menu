package com.zdev.library.view

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity


/**
 * Created by Alberto Vecina Sánchez on 2019-09-13.
 */
abstract class BaseActivity : AppCompatActivity() {

    private val activityResultListenerMap: MutableMap<Int, OnActivityResultListener> = HashMap()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK)
            activityResultListenerMap[requestCode]?.onResult(data)
    }

    fun startActivityForResult(
        intent: Intent?,
        requestCode: Int,
        listener: OnActivityResultListener
    ) {
        activityResultListenerMap[requestCode] = listener
        startActivityForResult(intent, requestCode)
    }

    interface OnActivityResultListener {
        fun onResult(data: Intent?)
    }

}