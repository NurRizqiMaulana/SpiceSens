package com.dicoding.spicesens.ui.scan.camera

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.spicesens.R
import com.yalantis.ucrop.UCrop
import java.io.File
import java.util.UUID

class CropImageActivity : AppCompatActivity() {
    private lateinit var sourceUri: String
    private lateinit var destinationUri: String
    private lateinit var uri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crop_image)

        if (intent.extras != null) {
            sourceUri = intent.getStringExtra("image") as String
            uri = Uri.parse(sourceUri)
        }

        destinationUri = StringBuilder(UUID.randomUUID().toString()).append(".jpg").toString()

        UCrop.of(uri, Uri.fromFile(File(cacheDir, destinationUri)))
            .withOptions(UCrop.Options())
            .withAspectRatio(1F, 1F)
            .withMaxResultSize(384, 384)
            .start(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val intent = Intent()
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            intent.putExtra("crop", "${UCrop.getOutput(data!!)!!}")
            setResult(101, intent)
        } else if (resultCode == UCrop.RESULT_ERROR) {
            setResult(102, intent)
        }
        finish()
    }
}