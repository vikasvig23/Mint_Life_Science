package com.mintlifescience.app.allPresentation

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
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
        val scheduleMeet : TextView = itemView.findViewById(R.id.scheduleMeet)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllPresentationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recent_doctor_list, parent, false)
        return AllPresentationViewHolder(view)
    }

    override fun getItemCount(): Int {
        return presentationList.size
    }

    override fun onBindViewHolder(holder: AllPresentationViewHolder, position: Int) {
        val doctorData = presentationList[position]
        holder.docName.text = doctorData.docName
        holder.docSpeciality.text = doctorData.docSpeciality


        if (doctorData.havePresentation) {
            holder.scheduleMeet.text = doctorData.scheduleMeet

            // Check if the date is past or future
            val currentDate = System.currentTimeMillis()
            val meetDate = try {
                SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(doctorData.scheduleMeet)?.time
            } catch (e: Exception) {
                Log.e("AddDoctorAdapter", "Date parsing failed for: ${doctorData.scheduleMeet}", e)
                null
            }

            if (meetDate != null) {
                if (meetDate < currentDate) {
                    holder.scheduleMeet.setBackgroundResource(R.drawable.rounded_red)
                } else {
                    holder.scheduleMeet.setBackgroundResource(R.drawable.rounded_blue)
                }

                holder.scheduleMeet.visibility = View.VISIBLE
            } else {
                holder.scheduleMeet.visibility = View.GONE
            }
        }


        holder.cardDoc.setOnClickListener {
            val context = holder.itemView.context
            val intent: Intent = if (doctorData.havePresentation) {
                Intent(context, MedicineScreenActivity::class.java)
            } else {
                Intent(context, HomeActivity::class.java)
            }
            intent.putExtra("doctorName", doctorData.docName) // Pass doctorName
            intent.putExtra("allPresentation", true) // Pass doctorName
            context.startActivity(intent)
        }
    }


    fun updateList(newDocList: List<DoctorData>) {
        presentationList = newDocList
        notifyDataSetChanged()
        Log.d("RecyclerViewBinding", "List updated: $newDocList")
    }
}
