package okanenavi.co.jp.ui.home

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.github.chrisbanes.photoview.PhotoView
import okanenavi.co.jp.R

class PhotoViewActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_IMAGE_URI = "extra_image_uri"
    }

    private var currentBitmap: Bitmap? = null
    private var rotationDegrees = 0f

    private lateinit var photoView: PhotoView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_view)

        photoView = findViewById(R.id.photo_view)
        val rotateButton = findViewById<ImageButton>(R.id.rotate_button)

        val imageUri = intent.getParcelableExtra<Uri>(EXTRA_IMAGE_URI)
        if (imageUri != null) {
            try {
                // 画像をBitmapとして読み込む
                val inputStream = contentResolver.openInputStream(imageUri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                currentBitmap = bitmap
                photoView.setImageBitmap(currentBitmap)
            } catch (e: Exception) {
                e.printStackTrace()
                finish()
            }
        } else {
            finish()
        }

        rotateButton.setOnClickListener {
            rotateImage()
        }
    }

    private fun rotateImage() {
        if (currentBitmap != null) {
            rotationDegrees = (rotationDegrees + 90f) % 360f
            val matrix = Matrix()
            matrix.postRotate(90f)
            currentBitmap = Bitmap.createBitmap(
                currentBitmap!!,
                0,
                0,
                currentBitmap!!.width,
                currentBitmap!!.height,
                matrix,
                true
            )
            photoView.setImageBitmap(currentBitmap)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Bitmapのメモリを解放
        currentBitmap?.recycle()
        currentBitmap = null
    }
}
