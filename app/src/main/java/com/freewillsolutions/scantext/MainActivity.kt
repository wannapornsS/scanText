/*
 * Copyright 2024 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.freewillsolutions.scantext

import PdfToBitmapConverter
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.content.IntentSender
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.freewillsolutions.scantext.databinding.ActivityMainBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

//import com.google.mlkit.vision.text.TextRecognizerOptions
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Objects


/** Demonstrates the document scanner powered by Google Play services. */
class MainActivity : AppCompatActivity() {

    private lateinit var resultInfo: TextView
    private lateinit var firstPageView: ImageView
    private lateinit var pageLimitInputView: EditText
    private lateinit var scannerLauncher: ActivityResultLauncher<IntentSenderRequest>
    private var enableGalleryImport = true
    private val FULL_MODE = "FULL"
    private val BASE_MODE = "BASE"
    private val BASE_MODE_WITH_FILTER = "BASE_WITH_FILTER"
    private var selectedMode = FULL_MODE
    private lateinit var currentPhotoPath: String

    private lateinit var binding : ActivityMainBinding
    private var bitmap : Bitmap? = null
    private var pdfPath : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        resultInfo = findViewById<TextView>(R.id.result_info)!!
        firstPageView = findViewById<ImageView>(R.id.first_page_view)!!
        pageLimitInputView = findViewById(R.id.page_limit_input)

        requestPermission()
        scannerLauncher =
            registerForActivityResult(StartIntentSenderForResult()) { result ->
//                handleActivityResult(result)
                recognize(result)
            }
        populateModeSelector()
    }

    fun onEnableGalleryImportCheckboxClicked(view: View) {
        enableGalleryImport = (view as CheckBox).isChecked
    }

    @Suppress("UNUSED_PARAMETER")
    fun onScanButtonClicked(unused: View) {
        resultInfo.text = null
        binding.sharePdfButton.visibility = View.GONE
        binding.saveImageButton.visibility = View.GONE
        bitmap = null
        pdfPath = null

        Glide.with(this).clear(firstPageView)

        val options =
            GmsDocumentScannerOptions.Builder()
                .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_BASE)
                .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_PDF)
                .setGalleryImportAllowed(enableGalleryImport)

        when (selectedMode) {
            FULL_MODE -> options.setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            BASE_MODE -> options.setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_BASE)
            BASE_MODE_WITH_FILTER ->
                options.setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_BASE_WITH_FILTER)
            else -> Log.e(TAG, "Unknown selectedMode: $selectedMode")
        }

        val pageLimitInputText = pageLimitInputView.text.toString()
        if (pageLimitInputText.isNotEmpty()) {
            try {
                val pageLimit = pageLimitInputText.toInt()
                options.setPageLimit(pageLimit)
            } catch (e: Throwable) {
                resultInfo.text = e.message
                return
            }
        }

        GmsDocumentScanning.getClient(options.build())
            .getStartScanIntent(this)
            .addOnSuccessListener { intentSender: IntentSender ->
                scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
            }
            .addOnFailureListener { e: Exception ->
                resultInfo.text = getString(R.string.error_default_message, e.message)
            }
    }

    @Suppress("UNUSED_PARAMETER")
    fun onSaveBitmap(unused: View) {

        bitmap?.let {
            saveFileImage(it)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun onSharePdf(unused: View){
        pdfPath?.let { path ->
            val externalUri = FileProvider.getUriForFile(this, "$packageName.provider", File(path))
            val shareIntent =
                Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_STREAM, externalUri)
                    type = "application/pdf"
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            startActivity(Intent.createChooser(shareIntent, "share pdf"))
        }
    }

    private fun populateModeSelector() {
        val featureSpinner = findViewById<Spinner>(R.id.mode_selector)
        val options: MutableList<String> = ArrayList()
        options.add(FULL_MODE)
        options.add(BASE_MODE)
        options.add(BASE_MODE_WITH_FILTER)

        // Creating adapter for featureSpinner
        val dataAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Attaching data adapter to spinner
        featureSpinner.adapter = dataAdapter
        featureSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parentView: AdapterView<*>,
                    selectedItemView: View,
                    pos: Int,
                    id: Long
                ) {
                    selectedMode = parentView.getItemAtPosition(pos).toString()
                }

                override fun onNothingSelected(arg0: AdapterView<*>?) {}
            }
    }

    private fun recognize(activityResult: ActivityResult){

         val resultCode = activityResult.resultCode
         val result = GmsDocumentScanningResult.fromActivityResultIntent(activityResult.data)
         if (resultCode == Activity.RESULT_OK && result != null) {
//             resultInfo.text = getString(R.string.scan_result, result)

             val pages = result.pages

             Log.d("recognize", "${activityResult.data}")
             if (!pages.isNullOrEmpty()) {
                 Glide.with(this).load(pages[0].imageUri).into(firstPageView)
             }

             result.pdf?.uri?.let { pdf ->
                 val bitmap = PdfToBitmapConverter.convertPdfPageToBitmap(this, pdf, 0)

                 val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                 val image = InputImage.fromBitmap(bitmap, 0)
                 val text = recognizer.process(image)
                     .addOnSuccessListener { result ->
                         binding.resultInfo.text = result.text
                         binding.saveImageButton.visibility = View.VISIBLE
                         binding.sharePdfButton.visibility = View.VISIBLE

                         this.bitmap = bitmap
                         this.pdfPath = pdf.path

                     }
                     .addOnFailureListener { e ->
                         binding.resultInfo.text = e.message
                         Log.d("text_recognize_2", "${e.message}")
                     }

                 Log.d("text_recognize_result", "$text")
             }
         }
    }

    private fun saveFileImage(bitmap: Bitmap){
        val folderApp = "/scan_text/"
        val fileName = "${Date().time}.jpg"
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()+ folderApp + fileName)


        if (!file.exists()){
            val fos: OutputStream = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver: ContentResolver = contentResolver
                val contentValues = ContentValues()
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM + folderApp)
                val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                Objects.requireNonNull(imageUri)?.let { resolver.openOutputStream(it) }!!
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)

                val dir = File(imagesDir, "/scan_text/")

                if (!dir.exists()){
                    dir.mkdirs()
                }

                val image = File(dir, fileName)
                FileOutputStream(image)

            }

            val thread = Thread {
                try {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)

                    Objects.requireNonNull(fos).close()
                    runOnUiThread {
                        Toast.makeText(this,"save image success", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: java.lang.Exception) {
                    runOnUiThread {
                        Toast.makeText(this,"save image error : ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                    e.printStackTrace()
                }
            }

            thread.start()

        }

    }


    private fun handleActivityResult(activityResult: ActivityResult) {
        Log.d("handleActivityResult","")
        val resultCode = activityResult.resultCode
        val result = GmsDocumentScanningResult.fromActivityResultIntent(activityResult.data)
        if (resultCode == Activity.RESULT_OK && result != null) {
            resultInfo.text = getString(R.string.scan_result, result)

            val pages = result.pages
            if (!pages.isNullOrEmpty()) {
                Glide.with(this).load(pages[0].imageUri).into(firstPageView)
            }

            result.pdf?.uri?.path?.let { path ->
                val externalUri = FileProvider.getUriForFile(this, "$packageName.provider", File(path))
                val shareIntent =
                    Intent(Intent.ACTION_SEND).apply {
                        putExtra(Intent.EXTRA_STREAM, externalUri)
                        type = "application/pdf"
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                startActivity(Intent.createChooser(shareIntent, "share pdf"))
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            resultInfo.text = getString(R.string.error_scanner_cancelled)
        } else {
            resultInfo.text = getString(R.string.error_default_message)
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }

    private val requestPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results -> }

    private fun requestPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            requestPermissions.launch(arrayOf(READ_MEDIA_IMAGES, READ_MEDIA_VISUAL_USER_SELECTED))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions.launch(arrayOf(READ_MEDIA_IMAGES))
        } else {
            requestPermissions.launch(arrayOf(READ_EXTERNAL_STORAGE))
        }
    }

}
