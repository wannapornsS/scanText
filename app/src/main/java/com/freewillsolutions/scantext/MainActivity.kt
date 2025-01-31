package com.freewillsolutions.scantext

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
import android.content.IntentSender
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.freewillsolutions.scantext.databinding.ActivityMainBinding
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning


class MainActivity : AppCompatActivity(), MainScreen.Callback {

    private lateinit var scannerLauncher: ActivityResultLauncher<IntentSenderRequest>
    private var enableGalleryImport = true
    private val FULL_MODE = "FULL"
    private val BASE_MODE = "BASE"
    private val BASE_MODE_WITH_FILTER = "BASE_WITH_FILTER"
    private var selectedMode = FULL_MODE
    private lateinit var binding: ActivityMainBinding

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestPermission()
        scannerLauncher = registerForActivityResult(StartIntentSenderForResult()) { result ->

            if (result.data != null) {
                replaceFragment(SaveScreen.newInstance(result))
            }
        }

        replaceFragment(MainScreen())
    }

    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results -> }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            requestPermissions.launch(arrayOf(READ_MEDIA_IMAGES, READ_MEDIA_VISUAL_USER_SELECTED))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions.launch(arrayOf(READ_MEDIA_IMAGES))
        } else {
            requestPermissions.launch(arrayOf(READ_EXTERNAL_STORAGE))
        }
    }

    private fun replaceFragment(fragment: Fragment?) {

        if (fragment != null) {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frameLayout, fragment)
            transaction.commit()
        }
    }

    private fun addFragment(fragment: Fragment?) {

        if (fragment != null) {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.add(R.id.frameLayout, fragment)
            transaction.addToBackStack("${fragment.tag}")
            transaction.commit()
        }
    }

    private fun openCamera() {
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

        GmsDocumentScanning.getClient(options.build())
            .getStartScanIntent(this)
            .addOnSuccessListener { intentSender: IntentSender ->
                scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
            }
            .addOnFailureListener { e: Exception ->
            }
    }

    override fun onClickCamera() {
        openCamera()
    }

    override fun onClickDone() {
        replaceFragment(MainScreen())
    }

    override fun onClickPreview(bitmap: ArrayList<Bitmap>) {
        addFragment(ImagePreviewScreen.newInstance(bitmap))
    }


}
