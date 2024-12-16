package com.example.mintlifesciences.medicinePresentation

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.mintlifesciences.R

class PresentationAdapter(private val items: List<PresentationData>, private val viewPager: ViewPager2, private val context: Context) :
    RecyclerView.Adapter<PresentationAdapter.ViewHolder>(){

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardImage : ImageView = view.findViewById(R.id.cardImage)
        val nextButton: ImageView = view.findViewById(R.id.next_button)
        val backButton: ImageView = view.findViewById(R.id.back_button)
        val playButton: ImageView = view.findViewById(R.id.play_button)
        val shareButton: ImageView = view.findViewById(R.id.share_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.medlistpresn, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.cardImage.setImageResource(item.imageResId)

        holder.playButton.setOnClickListener {
            val intent = Intent(context, MediaPlayerActivity::class.java)
            intent.putExtra("MEDIA_URL", item.mediaUrl)
            context.startActivity(intent)
        }

        holder.shareButton.setOnClickListener {
            // Create an intent for sharing
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain" // MIME type
                putExtra(Intent.EXTRA_TEXT, "Check this out: ${item.mediaUrl}")
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
    }

    override fun getItemCount(): Int {
        return items.size
    }
}
