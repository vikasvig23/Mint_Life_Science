package com.example.mintlifesciences.medicinePresentation

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
import com.example.mintlifesciences.R
import com.example.mintlifesciences.medicinePresentation.PresentationAdapter.ViewHolder
import com.example.mintlifesciences.model.Medicine

class PresentationAdapter(
    private val items: List<Medicine>,
    private val viewPager: ViewPager2,
    private val context: Context,
) : RecyclerView.Adapter<PresentationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardImage: ImageView = view.findViewById(R.id.cardImage)
        val nextButton: ImageView = view.findViewById(R.id.next_button)
        val backButton: ImageView = view.findViewById(R.id.back_button)
        val playButton: ImageView = view.findViewById(R.id.play_button)
        val shareButton: ImageView = view.findViewById(R.id.share_button)
        val pdfTextView: TextView = view.findViewById(R.id.pdfTextView)
        val descriptionTextView: TextView = view.findViewById(R.id.descriptionTextView)
        val medicineName: TextView = view.findViewById(R.id.medicineName)
        val medicineSaltDescription: TextView = view.findViewById(R.id.medicineSaltDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.medicine_presentation, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        // Load image using Glide
        Glide.with(context)
            .load(item.image)
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.baseline_image_24)
            .into(holder.cardImage)

        // Set text for medicine name and salt description
        holder.medicineName.text = item.name ?: "Unknown Medicine"
        holder.medicineSaltDescription.text = item.salt ?: "Unknown Salt"

        // Set description
        holder.descriptionTextView.text = item.description ?: "No description available"

        // Handle play button click
        holder.playButton.setOnClickListener {
            val videoUrl = item.videoUrl ?: ""
            if (videoUrl.contains("youtube", ignoreCase = true)) {
                // Open YouTube app or browser
                val youtubeIntent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
                youtubeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(youtubeIntent)
            } else {
                // Open MediaPlayerActivity
                val intent = Intent(context, MediaPlayerActivity::class.java).apply {
                    putExtra("MEDIA_URL", videoUrl)
                }
                context.startActivity(intent)
            }
        }

        // Handle share button click
        holder.shareButton.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "Check this out: ${item.videoUrl}")
            }
            val chooserIntent = Intent.createChooser(shareIntent, "Share via")
            context.startActivity(chooserIntent)
        }

        // Handle next button click
        holder.nextButton.setOnClickListener {
            if (position < items.size - 1) {
                viewPager.setCurrentItem(position + 1, true)
            }
        }

        // Handle back button click
        holder.backButton.setOnClickListener {
            if (position > 0) {
                viewPager.setCurrentItem(position - 1, true)
            }
        }

        // Handle PDF TextView click
        if (item.pdfUrl.isNullOrEmpty()) {
            holder.pdfTextView.visibility = View.GONE
        } else {
            holder.pdfTextView.visibility = View.VISIBLE
            holder.pdfTextView.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.pdfUrl))
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = items.size
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