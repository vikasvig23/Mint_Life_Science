package com.mintlifescience.app.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mintlifescience.app.R
import com.mintlifescience.app.model.Medicine

class MedicineAdapter(
    private val context: Context,
    private var dataList: List<Medicine>,
    private val selectedMedicines: MutableList<Medicine>,
    private val alreadySelectedMedicine: List<Medicine>
) : RecyclerView.Adapter<MedicineAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val medicineTitle: TextView = itemView.findViewById(R.id.card_medicine_name)
        val medicinePrice: TextView = itemView.findViewById(R.id.priceTxt)
        val recCard: ConstraintLayout = itemView.findViewById(R.id.medicine_card_holder)
        val selectedIcon: TextView = itemView.findViewById(R.id.new_selectedIcon)
        val image: ImageView = itemView.findViewById(R.id.card_medicine_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        MyViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.viewholder_medicine, parent, false))

    override fun getItemCount() = dataList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val medicine = dataList[position]
        holder.medicineTitle.text = medicine.name
        holder.medicinePrice.text = "₹${medicine.mrp ?: "N/A"}"

        Glide.with(context)
            .load(medicine.image)
            .placeholder(R.drawable.placeholder_image)
            .into(holder.image)

        if (alreadySelectedMedicine.contains(medicine) && !selectedMedicines.contains(medicine)) {
            selectedMedicines.add(medicine)
        }

        updateSelectionUI(holder, selectedMedicines.contains(medicine))

        holder.selectedIcon.setOnClickListener {
            if (selectedMedicines.contains(medicine)) {
                selectedMedicines.remove(medicine)
                Log.d("MedicineAdapter", "Removed: ${medicine.name}")
            } else {
                selectedMedicines.add(medicine)
                Log.d("MedicineAdapter", "Added: ${medicine.name}")
            }
            updateSelectionUI(holder, selectedMedicines.contains(medicine))
        }
    }

    fun updateMedicineList(newList: List<Medicine>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = dataList.size
            override fun getNewListSize() = newList.size
            override fun areItemsTheSame(o: Int, n: Int) = dataList[o].name == newList[n].name
            override fun areContentsTheSame(o: Int, n: Int) = dataList[o] == newList[n]
        })
        dataList = newList
        diff.dispatchUpdatesTo(this)
    }

    private fun updateSelectionUI(holder: MyViewHolder, selected: Boolean) {
        holder.selectedIcon.text = if (selected) "−" else "+"
        holder.selectedIcon.setBackgroundResource(
            if (selected) R.drawable.unselected_medicine_icon_background
            else R.drawable.plus_selected_medicine_background
        )
    }
}
