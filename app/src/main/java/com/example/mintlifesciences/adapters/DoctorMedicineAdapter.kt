package com.example.mintlifesciences.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mintlifesciences.model.Medicine
import com.example.mintlifesciences.R

class DoctorMedicineAdapter(
    private val context: Context,
    private var medicines: List<Medicine>,
    private val brandName: String
) : RecyclerView.Adapter<DoctorMedicineAdapter.MyViewHolder>() {

    private var expandedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.doctor_medicine_recycler_item, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val medicine = medicines[position]

        holder.medicineTitle.text = medicine.name
        holder.medicineSubtitle.text = medicine.description  // Assuming this is a brief description

        holder.medicineDescription.text = medicine.description
        holder.medicineSalt.text = "Salt: ${medicine.salt}"

        Glide.with(context)
            .load(medicine.image)
            .placeholder(R.drawable.logo)
            .into(holder.medicineImage)

        // Handle expand/collapse logic
        val isExpanded = position == expandedPosition
        holder.expandedLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE
        holder.expandArrow.setImageResource(
            if (isExpanded) R.drawable.baseline_arrow_drop_up_24 else R.drawable.baseline_arrow_drop_down_24
        )

        // Toggle visibility of medicineSubtitle on expand/collapse
        holder.medicineSubtitle.visibility = if (!isExpanded) View.VISIBLE else View.GONE

        holder.expandArrow.setOnClickListener {
            expandedPosition = if (isExpanded) -1 else position
            notifyDataSetChanged()
        }

        holder.itemView.setOnClickListener {
            expandedPosition = if (isExpanded) -1 else position
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int = medicines.size

    fun updateMedicineList(newList: List<Medicine>) {
        medicines = newList
        notifyDataSetChanged()
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val medicineTitle: TextView = itemView.findViewById(R.id.medTitle)
        val medicineSubtitle: TextView = itemView.findViewById(R.id.medicineSubtitle)
        val expandArrow: ImageView = itemView.findViewById(R.id.expandArrow)
        val expandedLayout: View = itemView.findViewById(R.id.expandedLayout)
        val medicineImage: ImageView = itemView.findViewById(R.id.medicineImage)
        val medicineDescription: TextView = itemView.findViewById(R.id.medicineDescription)
        val medicineSalt: TextView = itemView.findViewById(R.id.medicineSalt)
    }
}
