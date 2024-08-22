package com.example.mintlifesciences.doctorMedicine

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mintlifesciences.R
import org.w3c.dom.Text

class AddMedicineAdapter(var medList: List<MedicineData>):
    RecyclerView.Adapter<AddMedicineAdapter.MedViewHolder>() {

    class MedViewHolder(ItemView:View):RecyclerView.ViewHolder(ItemView) {
        val medName:TextView=itemView.findViewById(R.id.tvMedicineName)
        val medDesc:TextView=itemView.findViewById(R.id.tvMedicineDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.med_list,parent,false)
        return MedViewHolder(view)
    }

    override fun getItemCount(): Int {
       return medList.size
    }

    override fun onBindViewHolder(holder: MedViewHolder, position: Int) {
        val itemViewModel=medList[position]
        holder.medName.text=itemViewModel.medName
        holder.medDesc.text=itemViewModel.medDesc
    }

}