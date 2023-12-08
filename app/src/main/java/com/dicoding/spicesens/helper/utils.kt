package com.dicoding.spicesens.helper

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Environment
import com.dicoding.spicesens.R
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Locale

val timeStamp: String =
    SimpleDateFormat("dd-MMM-yyyy", Locale.US).format(System.currentTimeMillis())

fun createCustomTempFile(context: Context): File = File.createTempFile(
    timeStamp,
    ".jpg",
    context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
)

fun uriToFile(selectedImg: Uri, context: Context): File {
    val contentResolver: ContentResolver = context.contentResolver
    val myFile = createCustomTempFile(context)

    val inputStream = contentResolver.openInputStream(selectedImg) as InputStream
    val outputStream: OutputStream = FileOutputStream(myFile)
    val buf = ByteArray(1024)
    var len: Int

    while (inputStream.read(buf).also { len = it } > 0) outputStream.write(buf, 0, len)

    outputStream.close()
    inputStream.close()

    return myFile
}

fun createFile(application: Application): File {
    val mediaDir = application.externalMediaDirs.firstOrNull()?.let {
        File(it, application.resources.getString(R.string.app_name)).apply { mkdirs() }
    }

    val outputDirectory = if (
        mediaDir != null && mediaDir.exists()
    ) mediaDir else application.filesDir

    return File(outputDirectory, "$timeStamp.jpg")
}