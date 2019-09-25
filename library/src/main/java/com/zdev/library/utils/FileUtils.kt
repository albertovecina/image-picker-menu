package com.zdev.library.utils

import android.content.Context
import android.os.Environment
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Alberto Vecina Sánchez on 10/01/2019.
 */
object FileUtils {

    @JvmStatic
    fun createImageFile(context: Context): File {
        // Create an image file activityName
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File.createTempFile(
                "JPEG_${timeStamp}_", /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        )
        file.deleteOnExit()
        return file
    }

}