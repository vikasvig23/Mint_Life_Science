package com.example.mintlifesciences.recentDoctors

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.mintlifesciences.R
import com.example.mintlifesciences.addDoctor.DoctorData
import com.example.mintlifesciences.doctorMedicine.DoctorMedicineActivity

class RecentDoctorAdapter(private var docList: List<RecentDoctorData>) :
    RecyclerView.Adapter<RecentDoctorAdapter.RecentDoctorViewHolder>() {

    class RecentDoctorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardDoc: CardView = itemView.findViewById(R.id.card_doc)
        val docName: TextView = itemView.findViewById(R.id.tvName)
        val docSpeciality: TextView = itemView.findViewById(R.id.tvClass)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentDoctorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.doctor_list, parent, false)
        return RecentDoctorViewHolder(view)
    }

    override fun getItemCount(): Int {
        return docList.size
    }

    override fun onBindViewHolder(holder: RecentDoctorViewHolder, position: Int) {
        val doctorData = docList[position]
        holder.docName.text = doctorData.docName
        holder.docSpeciality.text = doctorData.docSpeciality

        holder.cardDoc.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, DoctorMedicineActivity::class.java).apply {
                putExtra("doctorName", doctorData.docName)
                putExtra("brandName", doctorData.brandName)
            }
            context.startActivity(intent)
        }
    }

    fun updateList(newDocList: List<RecentDoctorData>) {
        docList = newDocList
        notifyDataSetChanged()
        Log.d("RecyclerViewBinding", "List updated: $newDocList")
    }
}
