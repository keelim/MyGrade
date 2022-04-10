package com.keelim.mygrade.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
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
        val bitmap = createBitmapFromLayout(binding.root)
        runCatching {
            val cachePath = File(applicationContext.cacheDir, "images").apply {
                mkdirs()
            }
            val stream = FileOutputStream("$cachePath/image.png")
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
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

    private fun createBitmapFromLayout(tv: View): Bitmap {
        val spec: Int = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        tv.measure(spec, spec)
        tv.layout(0, 0, tv.measuredWidth, tv.measuredHeight)
        val b = Bitmap.createBitmap(
            tv.width, tv.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        val c = Canvas(b)
        c.translate((-tv.scrollX).toFloat(), (-tv.scrollY).toFloat())
        tv.draw(c)
        return b
    }

    fun View.convertToBitmap(): Bitmap {
        val measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        measure(measureSpec, measureSpec)
        val r = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(r)
        draw(canvas)
        return r
    }
}