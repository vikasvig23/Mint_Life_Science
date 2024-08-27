package com.example.mintlifesciences.Adapters

import DocMedicineViewModel
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.mintlifesciences.Model.Medicine
import com.example.mintlifesciences.R

class MedicineAdapter(
    private val context: Context,
    private var dataList: List<Medicine>,
    private val selectedMedicines: MutableList<Medicine>
) : RecyclerView.Adapter<MyViewHolder>() {
    private lateinit var viewModel : DocMedicineViewModel

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.medicine_list_item, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val medicine = dataList[position]

        holder.medicineTitle.text = medicine.name

        // Check if this medicine is selected
        if (selectedMedicines.contains(medicine)) {
            holder.selectedIcon.visibility = View.VISIBLE
        } else {
            holder.selectedIcon.visibility = View.GONE
        }

        holder.recCard.setOnClickListener {
            if (selectedMedicines.contains(medicine)) {
                selectedMedicines.remove(medicine)
                viewModel.removeMedicine(medicine)
                holder.selectedIcon.visibility = View.GONE
            } else {
                selectedMedicines.add(medicine)
                viewModel.addMedicine(medicine);
                holder.selectedIcon.visibility = View.VISIBLE
            }
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    fun updateMedicineList(newList: List<Medicine>) {
        dataList = newList
        notifyDataSetChanged()
    }

}

class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var medicineTitle: TextView = itemView.findViewById(R.id.medicineTitle)
    var recCard: CardView = itemView.findViewById(R.id.recCard)
    var selectedIcon: ImageView = itemView.findViewById(R.id.selectedIcon)
}