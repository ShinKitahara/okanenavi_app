package okanenavi.co.jp.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storageMetadata
import okanenavi.co.jp.R
import okanenavi.co.jp.databinding.ActivityCreateBinding
import okanenavi.co.jp.model.Record
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*


class CreateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateBinding

    private val datePicker: MaterialDatePicker<Long> by lazy {
        Locale.setDefault(Locale.JAPAN)
        MaterialDatePicker.Builder.datePicker()
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()
    }

    private val timePicker: MaterialTimePicker by lazy {
        Locale.setDefault(Locale.JAPAN)
        MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_24H).build()
    }

    private var date: Long? = null
    private var hour: Int? = null
    private var minute: Int? = null
    private var photo: Bitmap? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        datePicker.addOnPositiveButtonClickListener(dateSelection)
        timePicker.addOnPositiveButtonClickListener(timeSelection)
        binding.dateInput.setOnClickListener {
            datePicker.show(supportFragmentManager, datePicker.toString())
        }
        binding.timeInput.setOnClickListener {
            timePicker.show(supportFragmentManager, timePicker.toString())
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.subjects,
            android.R.layout.simple_spinner_item,
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.debitInput.setAdapter(adapter)
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.subjects,
            android.R.layout.simple_spinner_item,
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.creditInput.setAdapter(adapter)
        }

        binding.photoButton.setOnClickListener { onPickPhoto() }

        binding.cancelButton.setOnClickListener { finish() }
        binding.saveButton.setOnClickListener { onSave() }
    }

    private val dateSelection = MaterialPickerOnPositiveButtonClickListener<Long> {
        date = it
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = it
        val format = SimpleDateFormat.getDateInstance()
        binding.dateInput.setText(format.format(calendar.time))
    }

    private val timeSelection = View.OnClickListener {
        hour = timePicker.hour
        minute = timePicker.minute
        binding.timeInput.setText(getString(R.string.time_format, hour, minute))
    }

    private fun onPickPhoto() {
        MaterialAlertDialogBuilder(this).apply {
            setMessage("写真を選択してください")
            setNeutralButton("キャンセル") { _, _ -> }
            if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                setNegativeButton("写真を撮る") { _, _ ->
                    requestCameraPermission.launch(Manifest.permission.CAMERA)
                }
            }
            setPositiveButton("写真をギャラリーから選択") { _, _ ->
                requestGalleryPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            show()
        }
    }

    private val requestGalleryPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Intent(Intent.ACTION_PICK).also { galleryIntent ->
                    galleryIntent.type = "image/*"
                    resultGallery.launch(galleryIntent)
                }
            }
        }

    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                    resultCamera.launch(takePictureIntent)
                }
            }
        }

    private val resultGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { imageUri ->
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                    if (bitmap != null) {
                        didSelectBitmap(bitmap)
                    }
                }
            }
        }

    private val resultCamera =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val bitmap = result.data?.extras?.get("data") as? Bitmap
                if (bitmap != null) {
                    didSelectBitmap(bitmap)
                }
            }
        }

    private fun didSelectBitmap(bitmap: Bitmap) {
        photo = bitmap
        binding.imageView.setImageBitmap(bitmap)
    }

    private fun validation(): Boolean {
        var validate = true

        if (date != null) {
            binding.dateContainer.helperText = null
        } else {
            binding.dateContainer.helperText = "日付を選択してください"
            validate = false
        }

        if (hour != null && minute != null) {
            binding.hourContainer.helperText = null
        } else {
            binding.hourContainer.helperText = "時刻を選択してください"
            validate = false
        }

        val place = binding.placeInput.text.toString()
        if (place.isNotEmpty()) {
            binding.placeContainer.helperText = null
        } else {
            binding.placeContainer.helperText = "場所を入力してください"
            validate = false
        }

        val debit = binding.debitInput.text.toString()
        if (debit.isNotEmpty()) {
            binding.debitContainer.helperText = null
        } else {
            binding.debitContainer.helperText = "借方を選択してください"
            validate = false
        }

        val credit = binding.creditInput.text.toString()
        if (credit.isNotEmpty()) {
            binding.creditContainer.helperText = null
        } else {
            binding.creditContainer.helperText = "貸方を選択してください"
            validate = false
        }

        val debitDetail = binding.debitDetailInput.text.toString()
        if (debitDetail.isNotEmpty()) {
            binding.debitDetailContainer.helperText = null
        } else {
            binding.debitDetailContainer.helperText = "借方を入力してください"
            validate = false
        }

        val price = binding.priceInput.text.toString()
        if (price.isNotEmpty()) {
            binding.priceContainer.helperText = null
        } else {
            binding.priceContainer.helperText = "価格を入力してください"
            validate = false
        }

        val creditDetail = binding.creditDetailInput.text.toString()
        if (creditDetail.isNotEmpty()) {
            binding.creditDetailContainer.helperText = null
        } else {
            binding.creditDetailContainer.helperText = "貸方を入力してください"
            validate = false
        }

        val memo = binding.memoInput.text.toString()
        if (memo.isNotEmpty()) {
            binding.memoContainer.helperText = null
        } else {
            binding.memoContainer.helperText = "メモを入力してください"
            validate = false
        }

        if (photo != null) {
            binding.photoContainer.helperText = null
        } else {
            binding.photoContainer.helperText = "写真を選択してください"
            validate = false
        }

        return validate
    }

    private fun onSave() {
        if (!validation()) return

        val currentUser = Firebase.auth.currentUser ?: return

        binding.progressCircular.visibility = View.VISIBLE

        val record = Record(
            currentUser.uid,
            Date().time,
            date!!,
            hour!!,
            minute!!,
            binding.placeInput.text.toString(),
            binding.debitInput.text.toString(),
            binding.debitDetailInput.text.toString(),
            binding.creditInput.text.toString(),
            binding.creditDetailInput.text.toString(),
            binding.priceInput.text.toString().toInt(),
            binding.memoInput.text.toString(),
            ""
        )
        Record.collectionRef.add(record).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val recordId = task.result.id
                uploadPhoto(recordId) { path ->
                    if (path != null) {
                        Record.collectionRef.document(recordId).update(
                            mapOf("photoLink" to path)
                        ).addOnCompleteListener {
                            finish()
                        }
                    } else {
                        finish()
                    }
                }
            } else {
                binding.progressCircular.visibility = View.INVISIBLE
            }
        }
    }

    private fun uploadPhoto(recordId: String, completion: (String?) -> Unit) {
        val bitmap = photo
        if (bitmap == null) {
            completion(null)
            return
        }

        val uuid = UUID.randomUUID().toString()
        val storageRef = Record.storageRef.child(recordId).child("$uuid.jpg")

        val byteStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteStream)
        val data = byteStream.toByteArray()
        val metadata = storageMetadata {
            contentType = "image/jpg"
        }

        val uploadTask = storageRef.putBytes(data, metadata)
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            storageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                completion(downloadUri.path)
            } else {
                completion(null)
            }
        }
    }
}