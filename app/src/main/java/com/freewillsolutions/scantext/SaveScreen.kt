package com.freewillsolutions.scantext

import PdfToBitmapConverter
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.freewillsolutions.scantext.MainScreen.Callback
import com.freewillsolutions.scantext.databinding.ScreenSaveBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.Date
import java.util.Objects


class SaveScreen : Fragment() {
    private var _binding: ScreenSaveBinding? = null
    private val binding get() = _binding!!

    private var data: ActivityResult? = null
    private var arrayBitmap = arrayListOf<Bitmap>()
    private var pdfPath : String? = null
    private var adapter : AdapterImage? = null

    private var callback: Callback? = null

    companion object {
        @JvmStatic
        fun newInstance(data: ActivityResult) = SaveScreen().apply {
            this.data = data
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = ScreenSaveBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListener()

        data?.let {
            recognize(it)
        }

    }

    private fun setListener(){


        binding.lineSaveImage.setOnClickListener {
            arrayBitmap.forEachIndexed { index, bitmap ->
                saveFileImage(bitmap, index)
            }
        }

        binding.lineSharePdf.setOnClickListener {
            savePdf()
        }

        binding.buttonDone.setOnClickListener {
            callback?.onClickDone()

        }

    }


     private fun recognize(activityResult: ActivityResult){

        val resultCode = activityResult.resultCode
        val result = GmsDocumentScanningResult.fromActivityResultIntent(activityResult.data)
        if (resultCode == Activity.RESULT_OK && result != null) {

            result.pdf?.let { pdf ->
                val page = pdf.pageCount
                this.pdfPath = pdf.uri.path
                var textOcr = ""


                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

                for (i in 0 until page){

                    val bitmap = PdfToBitmapConverter.convertPdfPageToBitmap(requireContext(), pdf.uri, i)
                    this.arrayBitmap.add(bitmap)

                    val image = InputImage.fromBitmap(bitmap, 0)
                    recognizer.process(image)
                        .addOnSuccessListener { result ->
                            textOcr += result.text+"\n"

                            binding.textOcr.text = textOcr
                            adapter?.addData(bitmap)
                        }
                        .addOnFailureListener { e ->

                        }
                }


                setImageAdapter()
            }
        }
   }

    private fun saveFileImage(bitmap: Bitmap, index :Int){
        val folderApp = "/scan_text/"
        val fileName = "${Date().time}"
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()+ folderApp + fileName)


        if (!file.exists()){
            val fos: OutputStream = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver: ContentResolver = requireContext().contentResolver
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
                        if (index == (arrayBitmap.size- 1)){
                            Toast.makeText(requireContext(),"save image success", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: java.lang.Exception) {
                    runOnUiThread {
                        Toast.makeText(requireContext(),"save image error : ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                    e.printStackTrace()
                }
            }

            thread.start()

        }

    }

    private fun savePdf(){
        pdfPath?.let { path ->
            val externalUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", File(path))
            val shareIntent =
                Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_STREAM, externalUri)
                    type = "application/pdf"
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            startActivity(Intent.createChooser(shareIntent, "share pdf"))
        }
    }

    private fun setImageAdapter(){
        val gridLayoutManager = GridLayoutManager(requireContext(), 1, LinearLayoutManager.HORIZONTAL, false)
        val linearSnapHelper: LinearSnapHelper = SnapHelperOneByOne()
        linearSnapHelper.attachToRecyclerView(binding.recyclerView)
        adapter = AdapterImage{
            callback?.onClickPreview(arrayBitmap)
        }
        binding.recyclerView.layoutManager = gridLayoutManager
        binding.recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }

    override fun onAttach(ac: Activity) {
        super.onAttach(ac)
        callback = ac as Callback
    }

    override fun onDetach() {
        callback = null
        super.onDetach()
    }



}