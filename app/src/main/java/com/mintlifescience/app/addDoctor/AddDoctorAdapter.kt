package com.mintlifescience.app.addDoctor

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.mintlifescience.app.R
import com.mintlifescience.app.helperUtils.AvatarUtils
import com.mintlifescience.app.homescreen.HomeActivity
import com.mintlifescience.app.medicinePresentation.MedicineScreenActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddDoctorAdapter(
    private val context: Context,
    var docList: List<DoctorData>,
    private val viewModel: AddDoctorViewModel
) : RecyclerView.Adapter<AddDoctorAdapter.DoctorViewHolder>() {

    class DoctorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardDoc: MaterialCardView = itemView.findViewById(R.id.card_doc)
        val tvAvatar: TextView = itemView.findViewById(R.id.tvAvatar)
        val docName: TextView = itemView.findViewById(R.id.tvName)
        val docSpeciality: TextView = itemView.findViewById(R.id.tvClass)
        val scheduleMeet: TextView = itemView.findViewById(R.id.scheduleMeet)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorViewHolder =
        DoctorViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_doctor, parent, false))

    override fun getItemCount() = docList.size

    override fun onBindViewHolder(holder: DoctorViewHolder, position: Int) {
        val doctor = docList[position]

        holder.docName.text = doctor.docName
        holder.docSpeciality.text = doctor.docSpeciality

        holder.tvAvatar.text = AvatarUtils.initials(doctor.docName)
        holder.tvAvatar.background = AvatarUtils.tintedCircle(doctor.docName)

        bindScheduleBadge(holder.scheduleMeet, doctor)

        holder.cardDoc.setOnClickListener { openDoctor(doctor) }

        holder.cardDoc.setOnLongClickListener {
            val popup = PopupMenu(context, holder.cardDoc)
            MenuInflater(context).inflate(R.menu.card_doc_menu, popup.menu)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
                popup.gravity = android.view.Gravity.END
            popup.setOnMenuItemClickListener { menu ->
                when (menu.itemId) {
                    R.id.menu_open -> { openDoctor(doctor); true }
                    R.id.menu_feedback_history -> { showFeedbackDialog(doctor.docName); true }
                    else -> false
                }
            }
            popup.show()
            true
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
        } catch (e: Exception) { null }

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
            override fun getOldListSize() = docList.size
            override fun getNewListSize() = newList.size
            override fun areItemsTheSame(o: Int, n: Int) = docList[o].docName == newList[n].docName
            override fun areContentsTheSame(o: Int, n: Int) = docList[o] == newList[n]
        })
        docList = newList
        diff.dispatchUpdatesTo(this)
    }

    fun removeItem(position: Int) {
        val newList = docList.toMutableList().also { it.removeAt(position) }
        docList = newList
        notifyItemRemoved(position)
    }

    fun restoreItem(doctor: DoctorData, position: Int) {
        val newList = docList.toMutableList().also { it.add(position, doctor) }
        docList = newList
        notifyItemInserted(position)
    }

    private fun openDoctor(doctor: DoctorData) {
        val intent = if (doctor.havePresentation)
            Intent(context, MedicineScreenActivity::class.java)
        else Intent(context, HomeActivity::class.java)
        intent.putExtra("doctorName", doctor.docName)
        intent.putExtra("recentDoctor", false)
        context.startActivity(intent)
    }

    private fun showFeedbackDialog(doctorName: String) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_feedback_history, null)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.feedbackRecyclerView)
        val noFeedbackText = dialogView.findViewById<TextView>(R.id.noFeedbackText)

        val feedbackAdapter = FeedbackAdapter()
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = feedbackAdapter
        noFeedbackText.visibility = View.GONE

        viewModel.fetchFeedbackForDoctor(doctorName) { result ->
            result.onSuccess { list ->
                if (list.isEmpty()) {
                    noFeedbackText.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    feedbackAdapter.submitList(Result.success(list))
                }
            }.onFailure {
                Log.d("FeedbackList", "Unable to fetch feedback: ${it.message}")
            }
        }

        AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(true)
            .setNegativeButton("Close") { d, _ -> d.dismiss() }
            .show()
    }
}
