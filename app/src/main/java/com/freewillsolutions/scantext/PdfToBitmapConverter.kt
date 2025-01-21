import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import java.io.IOException

object PdfToBitmapConverter {
    @Throws(IOException::class)
    fun convertPdfPageToBitmap(context: Context, pdfUri: Uri, pageNumber: Int): Bitmap {
        val parcelFileDescriptor = context.contentResolver.openFileDescriptor(pdfUri, "r")
            ?: throw IOException("Failed to open PDF file.")

        val pdfRenderer = PdfRenderer(parcelFileDescriptor)
        val page = pdfRenderer.openPage(pageNumber)

        val width = page.width
        val height = page.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

        page.close()
        pdfRenderer.close()
        parcelFileDescriptor.close()

        return bitmap
    }
}