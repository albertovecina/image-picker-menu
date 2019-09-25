package com.zdev.library.view

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.zdev.library.utils.FileUtils
import com.zdev.rentspace.view.utils.extensions.getAbsolutePath


/**
 * Created by Alberto Vecina Sánchez on 2019-09-13.
 */
class ImageRequestActivity : AppCompatActivity() {

    companion object {

        private const val EXTRA_ACTION: String = "extra_action"

        private const val REQUEST_CODE_TAKE_PHOTO_FROM_CAMERA = 1
        private const val REQUEST_CODE_TAKE_PHOTO_FROM_GALLERY = 2
        private const val REQUEST_CODE_PERMISSION_CAMERA = 11
        private const val REQUEST_CODE_PERMISSION_EXTERNAL_STORAGE = 12

        private var onTakePictureListener: ((filePath: String) -> Unit)? = null

        fun requestFromCamera(context: Context, listener: ((filePath: String) -> Unit)) {
            onTakePictureListener = listener
            Intent(context, ImageRequestActivity::class.java).run {
                putExtra(EXTRA_ACTION, REQUEST_CODE_TAKE_PHOTO_FROM_CAMERA)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(this)
            }
        }


        fun requestFromGallery(context: Context, listener: ((filePath: String) -> Unit)) {
            onTakePictureListener = listener

            Intent(context, ImageRequestActivity::class.java).run {
                putExtra(EXTRA_ACTION, REQUEST_CODE_TAKE_PHOTO_FROM_GALLERY)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(this)
            }
        }

    }

    private val activityResultListenerMap: MutableMap<Int, OnActivityResultListener> = HashMap()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK)
            activityResultListenerMap[requestCode]?.onResult(data)
        finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE_PERMISSION_CAMERA -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED))
                    launchCameraIntent()
            }
            REQUEST_CODE_PERMISSION_EXTERNAL_STORAGE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED))
                    launchGalleryIntent()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        when (intent.getIntExtra(EXTRA_ACTION, 0)) {
            REQUEST_CODE_TAKE_PHOTO_FROM_CAMERA -> launchCameraIntent()
            REQUEST_CODE_TAKE_PHOTO_FROM_GALLERY -> launchGalleryIntent()
        }
    }

    private fun startActivityForResult(
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


    private fun launchCameraIntent() {
        if (checkPermission(Manifest.permission.CAMERA)) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val imageFile = FileUtils.createImageFile(this)
            val imageUri = FileProvider.getUriForFile(
                this,
                packageName,
                imageFile
            )
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            if (intent.resolveActivity(packageManager) != null)
                startActivityForResult(
                    intent,
                    REQUEST_CODE_TAKE_PHOTO_FROM_CAMERA,
                    object : OnActivityResultListener {
                        override fun onResult(data: Intent?) {
                            onTakePictureListener?.invoke(imageFile.absolutePath)
                        }
                    })
        }
    }

    private fun launchGalleryIntent() {
        if (checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            if (intent.resolveActivity(packageManager) != null)
                startActivityForResult(
                    intent,
                    REQUEST_CODE_TAKE_PHOTO_FROM_GALLERY,
                    object : OnActivityResultListener {
                        override fun onResult(data: Intent?) {
                            onTakePictureListener?.invoke(
                                data?.data?.getAbsolutePath(this@ImageRequestActivity) ?: ""
                            )
                        }
                    })
        }

    }

    private fun checkPermission(permission: String): Boolean {
        return if (ContextCompat.checkSelfPermission(
                this,
                permission
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(permission),
                REQUEST_CODE_PERMISSION_EXTERNAL_STORAGE
            )
            false
        } else {
            true
        }
    }

}