package com.mintlifescience.app.allPresentation

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mintlifescience.app.R
import com.mintlifescience.app.addDoctor.DoctorData
import com.mintlifescience.app.homescreen.HomeActivity
import com.mintlifescience.app.medicinePresentation.MedicineScreenActivity
import java.text.SimpleDateFormat
import java.util.Locale

class AllPresentationAdapter(private var presentationList: List<DoctorData>) :
    RecyclerView.Adapter<AllPresentationAdapter.AllPresentationViewHolder>() {

    class AllPresentationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardDoc: CardView = itemView.findViewById(R.id.card_doc)
        val docName: TextView = itemView.findViewById(R.id.tvName)
        val docSpeciality: TextView = itemView.findViewById(R.id.tvClass)
        val scheduleMeet: TextView = itemView.findViewById(R.id.scheduleMeet)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        AllPresentationViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.recent_doctor_list, parent, false))

    override fun getItemCount() = presentationList.size

    override fun onBindViewHolder(holder: AllPresentationViewHolder, position: Int) {
        val doctor = presentationList[position]
        holder.docName.text = doctor.docName
        holder.docSpeciality.text = doctor.docSpeciality

        if (doctor.havePresentation && doctor.scheduleMeet.isNotEmpty()) {
            holder.scheduleMeet.text = doctor.scheduleMeet
            val current = System.currentTimeMillis()
            val meetMs = try {
                SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(doctor.scheduleMeet)?.time
            } catch (e: Exception) {
                Log.e("AllPresentationAdapter", "Date parse failed: ${doctor.scheduleMeet}", e)
                null
            }
            if (meetMs != null) {
                holder.scheduleMeet.setBackgroundResource(
                    if (meetMs < current) R.drawable.rounded_red else R.drawable.rounded_blue
                )
                holder.scheduleMeet.visibility = View.VISIBLE
            } else {
                holder.scheduleMeet.visibility = View.GONE
            }
        } else {
            holder.scheduleMeet.visibility = View.GONE
        }

        holder.cardDoc.setOnClickListener {
            val ctx = holder.itemView.context
            val intent = if (doctor.havePresentation)
                Intent(ctx, MedicineScreenActivity::class.java)
            else Intent(ctx, HomeActivity::class.java)
            intent.putExtra("doctorName", doctor.docName)
            intent.putExtra("allPresentation", true)
            ctx.startActivity(intent)
        }
    }

    fun updateList(newList: List<DoctorData>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = presentationList.size
            override fun getNewListSize() = newList.size
            override fun areItemsTheSame(o: Int, n: Int) = presentationList[o].docName == newList[n].docName
            override fun areContentsTheSame(o: Int, n: Int) = presentationList[o] == newList[n]
        })
        presentationList = newList
        diff.dispatchUpdatesTo(this)
    }
}
