package com.zdev.library.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.zdev.library.utils.FileUtils
import com.zdev.rentspace.view.utils.extensions.getAbsolutePath


/**
 * Created by Alberto Vecina Sánchez on 2019-09-13.
 */
class PicturePickerActivity : AppCompatActivity() {

    companion object {

        private const val EXTRA_ACTION: String = "extra_action"

        private const val REQUEST_CODE_TAKE_PHOTO_FROM_CAMERA = 1
        private const val REQUEST_CODE_TAKE_PHOTO_FROM_GALLERY = 2

        private var onTakePictureListener: ((filePath: String) -> Unit)? = null

        fun requestFromCamera(context: Context, listener: ((filePath: String) -> Unit)) {
            onTakePictureListener = listener
            Intent(context, PicturePickerActivity::class.java).run {
                putExtra(EXTRA_ACTION, REQUEST_CODE_TAKE_PHOTO_FROM_CAMERA)
                context.startActivity(this)
            }
        }


        fun requestFromGallery(context: Context, listener: ((filePath: String) -> Unit)) {
            onTakePictureListener = listener

            Intent(context, PicturePickerActivity::class.java).run {
                putExtra(EXTRA_ACTION, REQUEST_CODE_TAKE_PHOTO_FROM_GALLERY)
                context.startActivity(this)
            }
        }

    }

    private val activityResultListenerMap: MutableMap<Int, OnActivityResultListener> = HashMap()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK)
            activityResultListenerMap[requestCode]?.onResult(data)
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
                        finish()
                    }
                })
    }

    private fun launchGalleryIntent() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        if (intent.resolveActivity(packageManager) != null)
            startActivityForResult(
                intent,
                REQUEST_CODE_TAKE_PHOTO_FROM_GALLERY,
                object : OnActivityResultListener {
                    override fun onResult(data: Intent?) {
                        onTakePictureListener?.invoke(
                            data?.data?.getAbsolutePath(this@PicturePickerActivity) ?: ""
                        )
                        finish()
                    }
                })
    }

}