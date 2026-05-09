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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.medicine_presentation, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]

        // --- SHIMMER + GLIDE (IMAGE NOW VISIBLE) ---
        holder.shimmer.visibility = View.VISIBLE
        holder.shimmer.startShimmer()
        holder.cardImage.visibility = View.INVISIBLE

        Glide.with(context)
            .load(item.image)
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.baseline_image_24)
            .centerCrop()
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirst: Boolean): Boolean {
                    holder.shimmer.stopShimmer()
                    holder.shimmer.visibility = View.GONE
                    holder.cardImage.visibility = View.VISIBLE
                    holder.cardImage.setImageResource(R.drawable.baseline_image_24)
                    return true
                }

                override fun onResourceReady(
                    resource: Drawable, model: Any, target: Target<Drawable>, dataSource: DataSource, isFirst: Boolean
                ): Boolean {
                    holder.shimmer.stopShimmer()
                    holder.shimmer.visibility = View.GONE
                    holder.cardImage.visibility = View.VISIBLE
                    return false
                }
            })
            .into(holder.cardImage)

        // --- TEXT ---
        holder.nameTv.text = item.name ?: "Unknown Medicine"
        holder.saltTv.text = item.salt ?: "Unknown Salt"
        holder.descTv.text = item.description ?: "No description"

        // --- PLAY ---
        holder.playBtn.setOnClickListener {
            val url = item.videoUrl.orEmpty()
            if (url.isNotBlank()) {
                if (url.contains("youtube", ignoreCase = true)) {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                } else {
                    context.startActivity(Intent(context, MediaPlayerActivity::class.java).apply {
                        putExtra("MEDIA_URL", url)
                    })
                }
            }
        }

        // --- SHARE ---
        holder.shareBtn.setOnClickListener {
            val shareText = item.videoUrl?.takeIf { it.isNotBlank() }
                ?.let { "Check this medicine: $it" }
                ?: "Check out this medicine!"
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            context.startActivity(Intent.createChooser(intent, "Share via"))
        }

        // --- NAVIGATION: Use holder.getAdapterPosition() ---
        holder.backBtn.setOnClickListener {
            val currentPos = holder.adapterPosition
            if (currentPos > 0 && currentPos != RecyclerView.NO_POSITION) {
                viewPager.setCurrentItem(currentPos - 1, true)
            }
        }

        holder.nextBtn.setOnClickListener {
            val currentPos = holder.adapterPosition
            if (currentPos < items.size - 1 && currentPos != RecyclerView.NO_POSITION) {
                viewPager.setCurrentItem(currentPos + 1, true)
            }
        }

        // Update arrow visibility safely
        val currentPos = holder.adapterPosition
        holder.backBtn.visibility = if (currentPos == 0 || currentPos == RecyclerView.NO_POSITION) View.GONE else View.VISIBLE
        holder.nextBtn.visibility = if (currentPos == items.size - 1 || currentPos == RecyclerView.NO_POSITION) View.GONE else View.VISIBLE

        // --- PDF ---
        holder.pdfTv.visibility = if (item.pdfUrl.isNullOrBlank()) View.GONE else View.VISIBLE
        holder.pdfTv.setOnClickListener {
            item.pdfUrl?.let { url ->
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        }
    }

    override fun getItemCount() = items.size

    override fun onViewRecycled(holder: VH) {
        holder.shimmer.stopShimmer()
        super.onViewRecycled(holder)
    }
}





/////--- Shimmer Effect is added
//
//override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//    val item = items[position]
//
//    // Show shimmer effect while image is loading
//    holder.shimmerFrameLayout.startShimmer()
//
//    // Load image using Glide with a RequestListener
//    Glide.with(context)
//        .load(item.image)
//        .placeholder(R.drawable.baseline_image_24) // Placeholder if image is not available
//        .error(R.drawable.baseline_image_24) // Error image if loading fails
//        .listener(object : RequestListener<Drawable?> {
//            override fun onLoadFailed(
//                e: GlideException?,
//                model: Any?,
//                target: Target<Drawable?>?,
//                isFirstResource: Boolean
//            ): Boolean {
//                // Stop shimmer effect and hide it when image loading fails
//                holder.shimmerFrameLayout.stopShimmer()
//                holder.shimmerFrameLayout.visibility = View.GONE
//                holder.cardImage.setImageResource(R.drawable.baseline_image_24) // Set error image
//                return false
//            }
//
//            override fun onResourceReady(
//                resource: Drawable?,
//                model: Any?,
//                target: Target<Drawable?>?,
//                dataSource: DataSource?,
//                isFirstResource: Boolean
//            ): Boolean {
//                // Stop shimmer effect and hide it once image is loaded successfully
//                holder.shimmerFrameLayout.stopShimmer()
//                holder.shimmerFrameLayout.visibility = View.GONE
//                return false
//            }
//        })
//        .into(holder.cardImage)
//
//    // Set text for medicine name and salt description
//    holder.medicineName.text = item.name ?: "Unknown Medicine"
//    holder.medicineSaltDescription.text = item.salt ?: "Unknown Salt"
//
//    // Set description
//    holder.descriptionTextView.text = item.description ?: "No description available"
//
//    // Handle play button click
//    holder.playButton.setOnClickListener {
//        val videoUrl = item.videoUrl ?: ""
//        if (videoUrl.contains("youtube", ignoreCase = true)) {
//            // Open YouTube app or browser
//            val youtubeIntent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
//            youtubeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            context.startActivity(youtubeIntent)
//        } else {
//            // Open MediaPlayerActivity
//            val intent = Intent(context, MediaPlayerActivity::class.java).apply {
//                putExtra("MEDIA_URL", videoUrl)
//            }
//            context.startActivity(intent)
//        }
//    }
//
//    // Handle share button click
//    holder.shareButton.setOnClickListener {
//        val shareIntent = Intent(Intent.ACTION_SEND).apply {
//            type = "text/plain"
//            putExtra(Intent.EXTRA_TEXT, "Check this out: ${item.videoUrl}")
//        }
//        val chooserIntent = Intent.createChooser(shareIntent, "Share via")
//        context.startActivity(chooserIntent)
//    }
//
//    // Handle next button click
//    holder.nextButton.setOnClickListener {
//        if (position < items.size - 1) {
//            viewPager.setCurrentItem(position + 1, true)
//        }
//    }
//
//    // Handle back button click
//    holder.backButton.setOnClickListener {
//        if (position > 0) {
//            viewPager.setCurrentItem(position - 1, true)
//        }
//    }
//
//    // Handle PDF TextView click
//    if (item.pdfUrl.isNullOrEmpty()) {
//        holder.pdfTextView.visibility = View.GONE
//    } else {
//        holder.pdfTextView.visibility = View.VISIBLE
//        holder.pdfTextView.setOnClickListener {
//            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.pdfUrl))
//            context.startActivity(intent)
//        }
//    }
//}
//
//override fun getItemCount(): Int = items.size
//}