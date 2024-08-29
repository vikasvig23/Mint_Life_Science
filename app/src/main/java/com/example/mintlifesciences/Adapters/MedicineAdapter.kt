package com.example.mintlifesciences.Adapters

import android.content.Context
import android.util.Log
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
    private val selectedMedicines: MutableList<Medicine>,
    private val alreadySelectedMedicine: List<Medicine>
) : RecyclerView.Adapter<MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.medicine_list_item, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val medicine = dataList[position]

        holder.medicineTitle.text = medicine.name

        // Check if this medicine is already selected
        if (alreadySelectedMedicine.contains(medicine)) {
            selectedMedicines.add(medicine)
            holder.selectedIcon.visibility = View.VISIBLE
        } else {
            holder.selectedIcon.visibility = View.GONE
        }

        holder.recCard.setOnClickListener {
            if (selectedMedicines.contains(medicine)) {
                selectedMedicines.remove(medicine)
                holder.selectedIcon.visibility = View.GONE

                // Log medicine removal
                Log.d("MedicineListActivity", "Medicine Removed: ${medicine.name}")
            } else {
                selectedMedicines.add(medicine)
                holder.selectedIcon.visibility = View.VISIBLE

                // Log medicine addition
                Log.d("MedicineListActivity", "Medicine Added: ${medicine.name}")
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