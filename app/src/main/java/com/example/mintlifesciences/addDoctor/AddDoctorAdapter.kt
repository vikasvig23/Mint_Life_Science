package com.example.mintlifesciences.addDoctor

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.mintlifesciences.R
import com.example.mintlifesciences.doctorMedicine.DoctorMedicineActivity

class AddDoctorAdapter(var docList: List<DoctorData>):
    RecyclerView.Adapter<AddDoctorAdapter.DoctorViewHolder>() {

    class DoctorViewHolder(ItemView: View):RecyclerView.ViewHolder(ItemView){
        val carddoc:CardView=itemView.findViewById(R.id.card_doc)
        val docName:TextView=itemView.findViewById(R.id.tvName)
        val docSpeciality:TextView=itemView.findViewById(R.id.tvClass)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.doctor_list,parent,false)
        return DoctorViewHolder(view)
    }

    override fun getItemCount(): Int {
        return docList.size
    }

    override fun onBindViewHolder(holder: DoctorViewHolder, position: Int) {
        val itemViewModel = docList[position]
        holder.docName.text = itemViewModel.docName
        holder.docSpeciality.text =itemViewModel.docSpeciality

        holder.carddoc.setOnClickListener{
            val context=holder.itemView.context
            val intent= Intent(context,DoctorMedicineActivity::class.java).apply {
                putExtra("DOC_NAME",itemViewModel.docName)
            }
            context.startActivity(intent)
        }
    }

    fun updateList(newDocList: List<DoctorData>) {
        docList = newDocList
        notifyDataSetChanged()
        Log.d("RecyclerViewBinding", "List updated: $newDocList")
    }

}