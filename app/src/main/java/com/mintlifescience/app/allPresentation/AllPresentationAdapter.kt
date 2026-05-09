package com.mintlifescience.app.allPresentation

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.mintlifescience.app.R
import com.mintlifescience.app.addDoctor.DoctorData
import com.mintlifescience.app.helperUtils.AvatarUtils
import com.mintlifescience.app.homescreen.HomeActivity
import com.mintlifescience.app.medicinePresentation.MedicineScreenActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AllPresentationAdapter(private var presentationList: List<DoctorData>) :
    RecyclerView.Adapter<AllPresentationAdapter.AllPresentationViewHolder>() {

    class AllPresentationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardDoc: MaterialCardView = itemView.findViewById(R.id.card_doc)
        val tvAvatar: TextView = itemView.findViewById(R.id.tvAvatar)
        val docName: TextView = itemView.findViewById(R.id.tvName)
        val docSpeciality: TextView = itemView.findViewById(R.id.tvClass)
        val scheduleMeet: TextView = itemView.findViewById(R.id.scheduleMeet)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        AllPresentationViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_doctor, parent, false))

    override fun getItemCount() = presentationList.size

    override fun onBindViewHolder(holder: AllPresentationViewHolder, position: Int) {
        val doctor = presentationList[position]
        holder.docName.text = doctor.docName
        holder.docSpeciality.text = doctor.docSpeciality

        holder.tvAvatar.text = AvatarUtils.initials(doctor.docName)
        holder.tvAvatar.background = AvatarUtils.tintedCircle(doctor.docName)

        bindScheduleBadge(holder.scheduleMeet, doctor)

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

    private fun bindScheduleBadge(view: TextView, doctor: DoctorData) {
        if (!doctor.havePresentation || doctor.scheduleMeet.isEmpty()) {
            view.visibility = View.GONE
            return
        }
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val meetMs = try {
            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(doctor.scheduleMeet)?.let { d ->
                Calendar.getInstance().apply {
                    time = d
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }
        } catch (e: Exception) {
            Log.e("AllPresentationAdapter", "Date parse failed: ${doctor.scheduleMeet}", e)
            null
        }

        if (meetMs == null) {
            view.visibility = View.GONE
            return
        }
        view.text = doctor.scheduleMeet
        view.setBackgroundResource(
            when {
                meetMs < today -> R.drawable.rounded_red
                meetMs == today -> R.drawable.rounded_orange
                else -> R.drawable.rounded_green
            }
        )
        view.visibility = View.VISIBLE
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
