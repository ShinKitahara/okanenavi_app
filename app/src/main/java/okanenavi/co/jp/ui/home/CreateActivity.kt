package okanenavi.co.jp.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storageMetadata
import okanenavi.co.jp.R
import okanenavi.co.jp.databinding.ActivityCreateBinding
import okanenavi.co.jp.model.Record
import okanenavi.co.jp.model.Record.Companion.toRecord
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class CreateActivity : AppCompatActivity() {
    companion object {
        const val ARG_RECORD_ID = "record_id"
    }

    private lateinit var binding: ActivityCreateBinding

    private var recordId: String? = null

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

    // カメラで撮影した写真のファイルURIを保持する変数
    private var photoURI: Uri? = null

    // 追加
    private var currentBitmap: Bitmap? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 本日の日付を設定
        val today = Calendar.getInstance().time
        val format = SimpleDateFormat.getDateInstance()
        binding.dateInput.setText(format.format(today))
        date = today.time

        recordId = intent.getStringExtra(ARG_RECORD_ID)

        configBinding()

        loadRecord()
    }

    private fun configBinding() {
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

        binding.imageView.setOnClickListener {
            onImageClick()
        }

        binding.photoButton.setOnClickListener { onPickPhoto() }

        binding.cancelButton.setOnClickListener { finish() }
        binding.saveButton.setOnClickListener { onSave() }

        if (recordId.isNullOrEmpty()) {
            binding.deleteButton.visibility = View.GONE
        } else {
            binding.deleteButton.visibility = View.VISIBLE
            binding.deleteButton.setOnClickListener { onDelete() }
        }
    }

    private fun onImageClick() {
        val bitmap = currentBitmap
        if (bitmap != null) {
            try {
                // 一時ファイルを作成
                val file = File.createTempFile("temp_image", ".jpg", cacheDir)
                val outputStream = file.outputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.close()

                val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)

                val intent = Intent(this, PhotoViewActivity::class.java)
                intent.putExtra(PhotoViewActivity.EXTRA_IMAGE_URI, uri)
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "画像の表示に失敗しました", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun loadRecord() {
        if (recordId.isNullOrEmpty()) return

        binding.progressCircular.visibility = View.VISIBLE

        Record.collectionRef.document(recordId!!).get().addOnCompleteListener { task ->
            val record = task.result.toRecord()
            if (record != null) {
                showRecord(record)
            } else {
                binding.progressCircular.visibility = View.INVISIBLE
            }
        }
    }

    private fun showRecord(record: Record) {
        date = record.date
        val format = SimpleDateFormat.getDateInstance()
        binding.dateInput.setText(format.format(record.date))

        hour = record.hour
        minute = record.minute

        if (hour == null && minute == null) {
            binding.timeInput.setText("")
        } else {
            val formattedTime = String.format("%02d:%02d", hour, minute)
            binding.timeInput.setText(formattedTime)
        }

        binding.placeInput.setText(record.place)
        binding.debitInput.setText(record.debit)
        binding.creditInput.setText(record.credit)
        binding.debitDetailInput.setText(record.debitDetail)
        binding.creditDetailInput.setText(record.creditDetail)
        binding.priceInput.setText(record.price.toString())
        binding.memoInput.setText(record.memo)

        if (recordId.isNullOrEmpty() || record.photoLink.isNullOrEmpty()) {
            binding.progressCircular.visibility = View.INVISIBLE
        } else {
            val storageRef = Record.storageRef.child(recordId!!).child(record.photoLink!!)
            val oneMegaByte: Long = 1024 * 1024 * 5 // 5MB
            storageRef.getBytes(oneMegaByte)
                .addOnSuccessListener { byteArray ->
                    val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                    binding.imageView.setImageBitmap(bitmap)
                    currentBitmap = bitmap // 追加
                    binding.progressCircular.visibility = View.INVISIBLE
                }
                .addOnFailureListener { exception ->
                    Log.e("ShowRecord", "Failed to download image", exception)
                    binding.progressCircular.visibility = View.INVISIBLE
                }
        }
    }

    private val dateSelection = MaterialPickerOnPositiveButtonClickListener<Long> {
        date = it
        val format = SimpleDateFormat.getDateInstance()
        binding.dateInput.setText(format.format(it))
    }

    private val timeSelection = View.OnClickListener {
        hour = timePicker.hour
        minute = timePicker.minute
        val formattedTime = String.format("%02d:%02d", hour, minute)
        binding.timeInput.setText(formattedTime)
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

    // カメラ起動用のインテントを作成するメソッド
    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // カメラアプリが存在するか確認
        val activities = packageManager.queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY)
        if (activities.isNotEmpty()) {
            // 撮影した写真を保存するファイルを作成
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                ex.printStackTrace()
                null
            }
            // ファイルが作成できた場合
            photoFile?.also {
                photoURI = FileProvider.getUriForFile(
                    this,
                    "${packageName}.fileprovider",
                    it
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                // URI に一時的な書き込み権限を付与
                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                resultCamera.launch(takePictureIntent)
            } ?: run {
                Toast.makeText(this, "写真ファイルの作成に失敗しました", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "カメラアプリが見つかりません", Toast.LENGTH_SHORT).show()
        }
    }

    // 一時的な画像ファイルを作成するメソッド
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (storageDir != null && !storageDir.exists()) {
            storageDir.mkdirs()
        }
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )
    }

    private val requestGalleryPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Intent(Intent.ACTION_PICK).also { galleryIntent ->
                    galleryIntent.type = "image/*"
                    resultGallery.launch(galleryIntent)
                }
            } else {
                Toast.makeText(this, "ギャラリーへのアクセスが許可されていません", Toast.LENGTH_SHORT).show()
            }
        }

    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                dispatchTakePictureIntent()
            } else {
                Toast.makeText(this, "カメラのパーミッションが必要です", Toast.LENGTH_SHORT).show()
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
                try {
                    val inputStream = contentResolver.openInputStream(photoURI!!)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()
                    if (bitmap != null) {
                        didSelectBitmap(bitmap)
                    } else {
                        Toast.makeText(this, "写真の取得に失敗しました", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "写真の取得に失敗しました", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "写真の撮影がキャンセルされました", Toast.LENGTH_SHORT).show()
            }
        }

    private fun didSelectBitmap(bitmap: Bitmap) {
        photo = bitmap
        currentBitmap = bitmap // 追加
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

        // 時刻がなくてもエラーを表示しないようにする
        if (hour != null && minute != null) {
            binding.hourContainer.helperText = null
        } else {
            binding.hourContainer.helperText = null
        }

        // 場所の入力は空欄でもエラーを表示しないようにする
        val place = binding.placeInput.text.toString()
        if (place.isNotEmpty()) {
            binding.placeContainer.helperText = null
        } else {
            binding.placeContainer.helperText = null
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

        // メモの入力は空欄でもエラーを表示しないようにする
        val memo = binding.memoInput.text.toString()
        if (memo.isNotEmpty()) {
            binding.memoContainer.helperText = null
        } else {
            binding.memoContainer.helperText = null
        }

        // 写真がなくてもエラーを表示しないようにする
        if (recordId.isNullOrEmpty() && photo == null) {
            binding.photoContainer.helperText = null
        } else {
            binding.photoContainer.helperText = null
        }

        return validate
    }

    private fun onSave() {
        if (!validation()) return

        val currentUser = Firebase.auth.currentUser ?: return

        binding.progressCircular.visibility = View.VISIBLE

        val record = Record(
            recordId ?: "",
            currentUser.uid,
            Date().time,
            date!!,
            hour,
            minute,
            binding.placeInput.text.toString(),
            binding.debitInput.text.toString(),
            binding.debitDetailInput.text.toString(),
            binding.creditInput.text.toString(),
            binding.creditDetailInput.text.toString(),
            binding.priceInput.text.toString().toInt(),
            binding.memoInput.text.toString()
        )

        if (recordId.isNullOrEmpty()) {
            // 新しいレコードの追加
            Record.collectionRef.add(record).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val rid = task.result.id
                    uploadPhoto(rid) { path ->
                        if (path != null) {
                            // 画像のパスを更新
                            Record.collectionRef.document(rid).update(
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
        } else {
            // 既存のレコードの更新
            Record.collectionRef.document(recordId!!).set(record, SetOptions.merge())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        uploadPhoto(recordId!!) { path ->
                            if (path != null) {
                                // 画像のパスを更新
                                Record.collectionRef.document(recordId!!).update(
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
    }

    private fun uploadPhoto(recordId: String, completion: (String?) -> Unit) {
        val bitmap = photo
        if (bitmap == null) {
            completion(null)
            return
        }

        val uuid = UUID.randomUUID().toString()
        val fileName = "$uuid.jpg"
        val storageRef = Record.storageRef.child(recordId).child(fileName)

        val byteStream = ByteArrayOutputStream()
        // 画像の圧縮品質を最高に設定
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteStream)
        val data = byteStream.toByteArray()
        val metadata = storageMetadata {
            contentType = "image/jpg"
        }

        storageRef.putBytes(data, metadata)
            .addOnSuccessListener {
                // アップロード成功
                completion(fileName)
            }
            .addOnFailureListener { exception ->
                // アップロード失敗
                Log.e("UploadPhoto", "Failed to upload photo", exception)
                completion(null)
            }
    }

    private fun onDelete() {
        val recordId = recordId ?: return

        MaterialAlertDialogBuilder(this).apply {
            setMessage("記録を削除しますか？")
            setNegativeButton("キャンセル") { _, _ -> }
            setPositiveButton("削除") { _, _ ->
                binding.progressCircular.visibility = View.VISIBLE
                Record.collectionRef.document(recordId).delete().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        finish()
                    } else {
                        binding.progressCircular.visibility = View.INVISIBLE
                    }
                }
            }
            show()
        }
    }
}
