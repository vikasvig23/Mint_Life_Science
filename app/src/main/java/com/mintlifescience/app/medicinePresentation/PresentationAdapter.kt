package com.mintlifescience.app.medicinePresentation

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.mintlifescience.app.R
import com.mintlifescience.app.model.Medicine

class PresentationAdapter(
    private val items: List<Medicine>,
    private val viewPager: ViewPager2,
    private val context: Context
) : RecyclerView.Adapter<PresentationAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val shimmer = view.findViewById<com.facebook.shimmer.ShimmerFrameLayout>(R.id.shimmer_layout)
        val cardImage = view.findViewById<ImageView>(R.id.cardImage)
        val playBtn = view.findViewById<ImageView>(R.id.play_button)
        val shareBtn = view.findViewById<ImageView>(R.id.share_button)
        val backBtn = view.findViewById<ImageView>(R.id.back_button)
        val nextBtn = view.findViewById<ImageView>(R.id.next_button)
        val pdfTv = view.findViewById<TextView>(R.id.pdfTextView)
        val nameTv = view.findViewById<TextView>(R.id.medicineName)
        val saltTv = view.findViewById<TextView>(R.id.medicineSaltDescription)
        val descTv = view.findViewById<TextView>(R.id.descriptionTextView)
        val mrpChip = view.findViewById<TextView>(R.id.mrpChip)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.medicine_presentation, parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]

        // Image with shimmer
        holder.shimmer.visibility = View.VISIBLE
        holder.shimmer.startShimmer()
        holder.cardImage.visibility = View.INVISIBLE

        Glide.with(context)
            .load(item.image)
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.baseline_image_24)
            .centerCrop()
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(e: GlideException?, model: Any?,
                    target: Target<Drawable>?, isFirst: Boolean): Boolean {
                    holder.shimmer.stopShimmer()
                    holder.shimmer.visibility = View.GONE
                    holder.cardImage.visibility = View.VISIBLE
                    return false
                }
                override fun onResourceReady(resource: Drawable, model: Any,
                    target: Target<Drawable>, dataSource: DataSource, isFirst: Boolean): Boolean {
                    holder.shimmer.stopShimmer()
                    holder.shimmer.visibility = View.GONE
                    holder.cardImage.visibility = View.VISIBLE
                    return false
                }
            })
            .into(holder.cardImage)

        // Text
        holder.nameTv.text = item.name ?: "Unknown Medicine"
        holder.saltTv.text = item.salt ?: ""
        holder.descTv.text = item.description ?: ""

        // MRP chip — show only when a price is available
        if (!item.mrp.isNullOrBlank()) {
            holder.mrpChip.text = "MRP  ₹${item.mrp}"
            holder.mrpChip.visibility = View.VISIBLE
        } else {
            holder.mrpChip.visibility = View.GONE
        }

        // Play button — hide entirely when no video URL so there is nothing misleading to tap
        val hasVideo = !item.videoUrl.isNullOrBlank()
        holder.playBtn.visibility = if (hasVideo) View.VISIBLE else View.GONE
        holder.playBtn.setOnClickListener {
            val url = item.videoUrl?.takeIf { it.isNotBlank() }
            if (url == null) {
                // Defensive: button is GONE when no URL, but guard just in case
                android.widget.Toast.makeText(
                    context, "No video available for this medicine", android.widget.Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            try {
                if (url.contains("youtube", ignoreCase = true)) {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                } else {
                    context.startActivity(
                        Intent(context, MediaPlayerActivity::class.java)
                            .putExtra("MEDIA_URL", url)
                    )
                }
            } catch (e: android.content.ActivityNotFoundException) {
                android.widget.Toast.makeText(
                    context, "No app found to play this video", android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Share button — shares this medicine's details as text
        holder.shareBtn.setOnClickListener {
            val sb = StringBuilder()
            sb.appendLine("*${item.name ?: "Medicine"}*")
            if (!item.salt.isNullOrBlank()) sb.appendLine("Salt: ${item.salt}")
            if (!item.mrp.isNullOrBlank()) sb.appendLine("MRP: ₹${item.mrp}")
            if (!item.description.isNullOrBlank()) sb.appendLine("\n${item.description}")
            if (!item.videoUrl.isNullOrBlank()) sb.appendLine("\nVideo: ${item.videoUrl}")
            if (!item.pdfUrl.isNullOrBlank()) sb.appendLine("PDF: ${item.pdfUrl}")
            sb.appendLine("\n– Mint Life Sciences")

            try {
                context.startActivity(
                    Intent.createChooser(
                        Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, sb.toString())
                        },
                        "Share Medicine"
                    )
                )
            } catch (e: android.content.ActivityNotFoundException) {
                android.widget.Toast.makeText(
                    context, "No app found to share", android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Navigation arrows
        val pos = holder.adapterPosition
        holder.backBtn.visibility = if (pos <= 0 || pos == RecyclerView.NO_POSITION) View.GONE else View.VISIBLE
        holder.nextBtn.visibility = if (pos >= items.size - 1 || pos == RecyclerView.NO_POSITION) View.GONE else View.VISIBLE

        holder.backBtn.setOnClickListener {
            val cur = holder.adapterPosition
            if (cur > 0 && cur != RecyclerView.NO_POSITION) viewPager.setCurrentItem(cur - 1, true)
        }
        holder.nextBtn.setOnClickListener {
            val cur = holder.adapterPosition
            if (cur < items.size - 1 && cur != RecyclerView.NO_POSITION) viewPager.setCurrentItem(cur + 1, true)
        }

        // PDF button — hide when no URL; guard against missing PDF-viewer app
        holder.pdfTv.visibility = if (item.pdfUrl.isNullOrBlank()) View.GONE else View.VISIBLE
        holder.pdfTv.setOnClickListener {
            val url = item.pdfUrl?.takeIf { it.isNotBlank() }
            if (url == null) {
                android.widget.Toast.makeText(
                    context, "No PDF available for this medicine", android.widget.Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } catch (e: android.content.ActivityNotFoundException) {
                android.widget.Toast.makeText(
                    context,
                    "No PDF viewer found. Please install a PDF viewer app to open this file.",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onViewRecycled(holder: VH) {
        holder.shimmer.stopShimmer()
        super.onViewRecycled(holder)
    }
}
