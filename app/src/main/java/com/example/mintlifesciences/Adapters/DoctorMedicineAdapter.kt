package com.example.mintlifesciences.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mintlifesciences.Model.Medicine
import com.example.mintlifesciences.R

class DoctorMedicineAdapter(
    private val context: Context,
    private var medicines: List<Medicine>
) : RecyclerView.Adapter<DoctorMedicineAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.doctor_medicine_recycler_item, parent, false)
        return MyViewHolder(view)
    }

    // Bind data to ViewHolder
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val medicine = medicines[position] // Use the provided list

        holder.medicineTitle.text = medicine.name

        // Handle item click if needed ( To handle expandable recylerview Item)
        holder.itemView.setOnClickListener {

        }
    }

    override fun getItemCount(): Int {
        return medicines.size // Use the provided list
    }

    // Update the list and notify changes
    fun updateMedicineList(newList: List<Medicine>) {
        medicines = newList
        notifyDataSetChanged()
    }

    // ViewHolder class
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val medicineTitle: TextView = itemView.findViewById(R.id.medicineTitle)
    }
}
