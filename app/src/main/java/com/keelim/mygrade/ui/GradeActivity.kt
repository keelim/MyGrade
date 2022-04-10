package com.keelim.mygrade.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.keelim.data.model.Result
import com.keelim.mygrade.databinding.ActivityGradeBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream


@AndroidEntryPoint
class GradeActivity : AppCompatActivity() {
    private val data by lazy { intent.getParcelableExtra("data") ?: Result.emptyResult() }
    private val binding by lazy {
        ActivityGradeBinding.inflate(layoutInflater).apply {
            grade.text = data.grade
            level.text = data.point
            btnCopy.setOnClickListener {
                saveAndCopy()
            }
            title.setOnClickListener {
                onBackPressed()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

    private fun saveAndCopy() {
        val view = window.decorView.rootView
        val screenBitmap = getBitmapFromView(view)

        runCatching {
            val cachePath = File(applicationContext.cacheDir, "images").apply {
                mkdirs()
            }
            val stream = FileOutputStream("$cachePath/image.png")
            screenBitmap!!.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()
            FileProvider.getUriForFile(
                applicationContext,
                "com.keelim.fileprovider", File(cachePath, "image.png")
            )
        }.onSuccess {
            startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, it)
            }, "Share Capture Image"))
        }.onFailure { throwable -> throwable.printStackTrace() }
    }

    private fun getBitmapFromView(view: View): Bitmap? {
        return Bitmap
            .createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            .also { bitmap ->
                view.draw(Canvas(bitmap))
            }
    }
}