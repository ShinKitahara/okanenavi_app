package okanenavi.co.jp.ui.home

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.chrisbanes.photoview.PhotoView
import okanenavi.co.jp.R

class PhotoViewActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_IMAGE_URI = "extra_image_uri"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val photoView = PhotoView(this)
        setContentView(photoView)

        val imageUri = intent.getParcelableExtra<Uri>(EXTRA_IMAGE_URI)
        if (imageUri != null) {
            photoView.setImageURI(imageUri)
        } else {
            finish()
        }
    }
}
