package com.example.mintlifesciences.addDoctor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.mintlifesciences.R

class AddDoctorAdapter(val docList: List<DoctorData>):
    RecyclerView.Adapter<AddDoctorAdapter.DoctorViewHolder>() {

    class DoctorViewHolder(ItemView: View):RecyclerView.ViewHolder(ItemView){
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
    }
}