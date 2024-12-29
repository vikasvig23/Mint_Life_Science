package com.example.mintlifesciences.homescreen

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mintlifesciences.R
import com.example.mintlifesciences.model.BrandItem

class HomeAdapter(
    private var items: List<BrandItem>,
    private val itemClickListener: (String) -> Unit
) : RecyclerView.Adapter<HomeAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.itemTextView)
        val imageView: ImageView = view.findViewById(R.id.itemImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.home_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.textView.text = item.name
        holder.imageView.setImageResource(item.imageResId) // Set the drawable image

        holder.itemView.setOnClickListener {
            itemClickListener(item.name)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<BrandItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
