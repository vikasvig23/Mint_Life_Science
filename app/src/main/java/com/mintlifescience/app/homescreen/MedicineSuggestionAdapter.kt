package com.mintlifescience.app.homescreen

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mintlifescience.app.R
import com.mintlifescience.app.model.Medicine

class MedicineSuggestionAdapter(
    private var medicineList: List<Pair<Medicine, String>>, // Medicine + Brand Name
    private val onItemClick: (Medicine, String) -> Unit
) : RecyclerView.Adapter<MedicineSuggestionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val medicineName: TextView = view.findViewById(R.id.medicineName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medicine_suggestion, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (medicine, brandName) = medicineList[position]
        holder.medicineName.text = "${medicine.name} ($brandName)"

        holder.itemView.setOnClickListener {
            onItemClick(medicine, brandName)
        }
    }

    override fun getItemCount(): Int = medicineList.size

    fun updateList(newList: List<Pair<Medicine, String>>) {
        medicineList = newList
        notifyDataSetChanged()
    }
}
