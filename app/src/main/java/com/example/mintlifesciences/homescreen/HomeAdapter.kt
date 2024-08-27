package com.example.mintlifesciences.homescreen

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mintlifesciences.R

class HomeAdapter(
    private var items: List<String>,
    private val itemClickListener: (String) -> Unit
) : RecyclerView.Adapter<HomeAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.itemTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_adapter, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.textView.text = item

        // Set click listener for the entire item view
//        holder.itemView.setOnClickListener {
//            itemClickListener(item)
//        }

        // If you want to specifically handle clicks on the TextView
        holder.textView.setOnClickListener {
            itemClickListener(item)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }


    fun updateItems(newItems: List<String>) {
        items = newItems
        notifyDataSetChanged()
    }
}