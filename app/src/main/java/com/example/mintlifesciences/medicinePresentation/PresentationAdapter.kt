package com.example.mintlifesciences.medicinePresentation

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.mintlifesciences.R
import com.example.mintlifesciences.model.Medicine

class PresentationAdapter(
    private val items: List<Medicine>,
    private val viewPager: ViewPager2,
    private val context: Context
) : RecyclerView.Adapter<PresentationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardImage: ImageView = view.findViewById(R.id.cardImage)
        val nextButton: ImageView = view.findViewById(R.id.next_button)
        val backButton: ImageView = view.findViewById(R.id.back_button)
        val playButton: ImageView = view.findViewById(R.id.play_button)
        val shareButton: ImageView = view.findViewById(R.id.share_button)
        val pdfTextView: TextView = view.findViewById(R.id.pdfTextView) // Assuming a TextView for PDF link
        val descriptionTextView: TextView = view.findViewById(R.id.descriptionTextView)
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
            .placeholder(R.drawable.baseline_image_24) // Placeholder image while the image loads
            .error(R.drawable.baseline_image_24) // Error image in case loading fails
            .into(holder.cardImage)

        holder.playButton.setOnClickListener {
            val intent = Intent(context, MediaPlayerActivity::class.java)
            intent.putExtra("MEDIA_URL", item.videoUrl ?: "")
            context.startActivity(intent)
        }

        holder.shareButton.setOnClickListener {
            // Create an intent for sharing
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain" // MIME type
                putExtra(Intent.EXTRA_TEXT, "Check this out: ${item.videoUrl}")
            }

            // Launch the intent
            val chooserIntent = Intent.createChooser(shareIntent, "Share via")
            context.startActivity(chooserIntent)
        }

        holder.nextButton.setOnClickListener {
            if (position < items.size - 1) {
                viewPager.setCurrentItem(position + 1, true)
            }
        }

        holder.backButton.setOnClickListener {
            if (position > 0) {
                viewPager.setCurrentItem(position - 1, true)
            }
        }

        // Set description and PDF link
        holder.descriptionTextView.text = item.description ?: "No description available"
        holder.pdfTextView.text = item.pdfUrl ?: "No PDF available"
    }

    override fun getItemCount(): Int {
        return items.size
    }
}
