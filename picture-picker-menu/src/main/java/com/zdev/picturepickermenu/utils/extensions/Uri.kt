package com.zdev.picturepickermenu.utils.extensions

import android.content.Context
import android.net.Uri
import android.provider.MediaStore

/**
 * Created by Alberto Vecina Sánchez on 29/01/2019.
 */

/** Returns the absolute path for the file referenced by the [Uri] **/
fun Uri.getAbsolutePath(context: Context?): String {
    if (context == null)
        return path ?: ""

    val cursor =
        context.contentResolver.query(this, arrayOf(MediaStore.Images.Media.DATA), null, null, null)

    return if (cursor != null) {
        cursor.moveToFirst()
        val columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
        val filePath = cursor.getString(columnIndex)
        cursor.close()
        filePath
    } else {
        path ?: ""
    }
}