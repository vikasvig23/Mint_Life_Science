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

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val medicine = medicines[position]

        holder.medicineTitle.text = medicine.name
        holder.medicineDescription.text = medicine.description

        holder.itemView.setOnClickListener {

        }
    }

    override fun getItemCount(): Int = medicines.size

    fun updateMedicineList(newList: List<Medicine>) {
        medicines = newList
        notifyDataSetChanged()
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val medicineTitle: TextView = itemView.findViewById(R.id.medTitle)
        val medicineDescription: TextView = itemView.findViewById(R.id.medicineSubtitle)
    }
}
