package com.mintlifescience.app.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mintlifescience.app.model.Medicine
import com.mintlifescience.app.R

class MedicineAdapter(
    private val context: Context,
    private var dataList: List<Medicine>,
    private val selectedMedicines: MutableList<Medicine>,
    private val alreadySelectedMedicine: List<Medicine>
) : RecyclerView.Adapter<MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.viewholder_medicine, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val medicine = dataList[position]

        holder.medicineTitle.text = medicine.name

        holder.medicinePrice.text = "₹${medicine.mrp ?: "N/A"}"

        Glide.with(context)
            .load(medicine.image)
            .placeholder(R.drawable.placeholder_image)
            .into(holder.image)

        // Check if this medicine is already selected
        if (alreadySelectedMedicine.contains(medicine)) {
            selectedMedicines.add(medicine)
        }

        updateSelectionUI(holder, selectedMedicines.contains(medicine))

        // Handle selection
        holder.selectedIcon.setOnClickListener {
            if (selectedMedicines.contains(medicine)) {
                selectedMedicines.remove(medicine)
                Log.d("MedicineListActivity", "Medicine Removed: ${medicine.name}")
            } else {
                selectedMedicines.add(medicine)
                Log.d("MedicineListActivity", "Medicine Added: ${medicine.name}")
            }
            updateSelectionUI(holder, selectedMedicines.contains(medicine))
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    fun updateMedicineList(newList: List<Medicine>) {
        dataList = newList
        notifyDataSetChanged()
    }

    private fun updateSelectionUI(holder: MyViewHolder, isSelected: Boolean) {
        if (isSelected) {
            holder.selectedIcon.text = "−" // Change "+" to "-"
            holder.selectedIcon.setBackgroundResource(R.drawable.unselected_medicine_icon_background) // Change background to red
        } else {
            holder.selectedIcon.text = "+" // Change back to "+"
            holder.selectedIcon.setBackgroundResource(R.drawable.plus_selected_medicine_background) // Default blue
        }
    }

}

class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var medicineTitle: TextView = itemView.findViewById(R.id.card_medicine_name)
    var medicinePrice: TextView = itemView.findViewById(R.id.priceTxt)
    var recCard: ConstraintLayout = itemView.findViewById(R.id.medicine_card_holder)
    var selectedIcon: TextView = itemView.findViewById(R.id.new_selectedIcon)
    var image: ImageView = itemView.findViewById(R.id.card_medicine_image)
}