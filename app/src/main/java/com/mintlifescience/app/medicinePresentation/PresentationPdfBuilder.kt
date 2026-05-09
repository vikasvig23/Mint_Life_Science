package com.mintlifescience.app.medicinePresentation

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.mintlifescience.app.model.Medicine
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream

object PresentationPdfBuilder {

    private const val PAGE_W = 595
    private const val PAGE_H = 842
    private const val MARGIN = 32f
    private const val IMAGE_H = 280f
    private const val HEADER_H = 48f
    private const val FOOTER_H = 40f

    fun buildAndShare(
        context: Context,
        medicines: List<Medicine>,
        doctorName: String,
        scope: CoroutineScope,
        onStatus: (String) -> Unit
    ) {
        scope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { onStatus("Generating PDF…") }
            try {
                val pdf = PdfDocument()

                for ((i, medicine) in medicines.withIndex()) {
                    withContext(Dispatchers.Main) {
                        onStatus("Building page ${i + 1} of ${medicines.size}…")
                    }
                    val pageInfo = PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, i + 1).create()
                    val page = pdf.startPage(pageInfo)
                    drawPage(context, page.canvas, medicine, i + 1, medicines.size, doctorName)
                    pdf.finishPage(page)
                }

                val dir = File(context.cacheDir, "presentation_pdfs").also { it.mkdirs() }
                val file = File(dir, "presentation_${doctorName.replace(" ", "_")}.pdf")
                FileOutputStream(file).use { pdf.writeTo(it) }
                pdf.close()

                val uri = FileProvider.getUriForFile(
                    context, "${context.packageName}.fileprovider", file
                )
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Medicine Presentation – $doctorName")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                withContext(Dispatchers.Main) {
                    onStatus("")
                    context.startActivity(Intent.createChooser(intent, "Share Presentation"))
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onStatus("Failed to create PDF: ${e.message}") }
            }
        }
    }

    private fun drawPage(
        context: Context,
        canvas: Canvas,
        medicine: Medicine,
        pageNum: Int,
        total: Int,
        doctorName: String
    ) {
        val contentW = (PAGE_W - 2 * MARGIN).toInt()

        // White background
        canvas.drawColor(Color.WHITE)

        // ── Header bar ────────────────────────────────────────────
        val headerBg = Paint().apply { color = Color.parseColor("#1B5E20") }
        canvas.drawRect(0f, 0f, PAGE_W.toFloat(), HEADER_H, headerBg)

        val headerPaint = TextPaint().apply { color = Color.WHITE; textSize = 13f; isFakeBoldText = true }
        canvas.drawText("Mint Life Sciences", MARGIN, 31f, headerPaint)

        val pageStr = "$pageNum / $total"
        val pagePaint = TextPaint().apply { color = Color.WHITE; textSize = 12f }
        canvas.drawText(pageStr, PAGE_W - MARGIN - pagePaint.measureText(pageStr), 31f, pagePaint)

        // ── Medicine image ────────────────────────────────────────
        val imgTop = HEADER_H
        val imgBot = imgTop + IMAGE_H
        try {
            val bmp = Glide.with(context).asBitmap()
                .load(medicine.image)
                .submit(PAGE_W, IMAGE_H.toInt())
                .get()
            canvas.drawBitmap(bmp, Rect(0, 0, bmp.width, bmp.height),
                RectF(0f, imgTop, PAGE_W.toFloat(), imgBot), null)
        } catch (e: Exception) {
            val ph = Paint().apply { color = Color.parseColor("#EEEEEE") }
            canvas.drawRect(0f, imgTop, PAGE_W.toFloat(), imgBot, ph)
            val phTxt = TextPaint().apply { color = Color.GRAY; textSize = 14f }
            canvas.drawText("No Image", PAGE_W / 2f - 36f, imgTop + IMAGE_H / 2f, phTxt)
        }

        // ── Content ───────────────────────────────────────────────
        var y = imgBot + 20f

        // Medicine name
        val namePaint = TextPaint().apply {
            color = Color.parseColor("#1B5E20"); textSize = 21f; isFakeBoldText = true
        }
        val nameLayout = staticLayout(medicine.name ?: "Unknown Medicine", namePaint, contentW)
        canvas.withTranslate(MARGIN, y) { nameLayout.draw(this) }
        y += nameLayout.height + 6f

        // Salt
        if (!medicine.salt.isNullOrBlank()) {
            val sp = TextPaint().apply { color = Color.parseColor("#616161"); textSize = 13f }
            canvas.drawText("Salt: ${medicine.salt}", MARGIN, y, sp)
            y += 20f
        }

        // MRP
        if (!medicine.mrp.isNullOrBlank()) {
            val mp = TextPaint().apply {
                color = Color.parseColor("#1565C0"); textSize = 14f; isFakeBoldText = true
            }
            canvas.drawText("MRP: ₹${medicine.mrp}", MARGIN, y, mp)
            y += 20f
        }

        // Divider
        val div = Paint().apply { color = Color.parseColor("#E0E0E0"); strokeWidth = 1f }
        canvas.drawLine(MARGIN, y + 6f, PAGE_W - MARGIN, y + 6f, div)
        y += 18f

        // Description
        val description = medicine.description
        val maxDescH = PAGE_H - FOOTER_H - y - 8f
        if (!description.isNullOrBlank() && maxDescH > 0) {
            val dp = TextPaint().apply { color = Color.parseColor("#333333"); textSize = 12f }
            val dl = staticLayout(description, dp, contentW)
            if (dl.height <= maxDescH) {
                canvas.withTranslate(MARGIN, y) { dl.draw(this) }
            }
        }

        // ── Footer ────────────────────────────────────────────────
        val footerTop = PAGE_H - FOOTER_H
        val footerBg = Paint().apply { color = Color.parseColor("#F1F8E9") }
        canvas.drawRect(0f, footerTop, PAGE_W.toFloat(), PAGE_H.toFloat(), footerBg)

        val footerTxt = TextPaint().apply { color = Color.parseColor("#388E3C"); textSize = 10f }
        canvas.drawText("Mint Life Sciences Pvt. Ltd.  ·  Dr. $doctorName", MARGIN, footerTop + 24f, footerTxt)
    }

    private fun staticLayout(text: String, paint: TextPaint, width: Int): StaticLayout =
        StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(3f, 1f)
            .setIncludePad(false)
            .build()

    private inline fun Canvas.withTranslate(dx: Float, dy: Float, block: Canvas.() -> Unit) {
        save(); translate(dx, dy); block(); restore()
    }
}
