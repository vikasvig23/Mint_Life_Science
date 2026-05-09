package com.mintlifescience.app.addDoctor

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mintlifescience.app.R
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
        val carddoc: CardView = itemView.findViewById(R.id.card_doc)
        val docName: TextView = itemView.findViewById(R.id.tvName)
        val docSpeciality: TextView = itemView.findViewById(R.id.tvClass)
        val deleteIcon: ImageView = itemView.findViewById(R.id.deleteIcon)
        val scheduleMeet: TextView = itemView.findViewById(R.id.scheduleMeet)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.doctor_list, parent, false)
        return DoctorViewHolder(view)
    }

    override fun getItemCount() = docList.size

    override fun onBindViewHolder(holder: DoctorViewHolder, position: Int) {
        val doctor = docList[position]
        holder.docName.text = doctor.docName
        holder.docSpeciality.text = doctor.docSpeciality

        if (doctor.havePresentation && doctor.scheduleMeet.isNotEmpty()) {
            holder.scheduleMeet.text = doctor.scheduleMeet
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val meetMs = try {
                SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(doctor.scheduleMeet)?.let {
                    Calendar.getInstance().apply { time = it
                        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                }
            } catch (e: Exception) { null }

            if (meetMs != null) {
                holder.scheduleMeet.setBackgroundResource(
                    if (meetMs < today) R.drawable.rounded_red else R.drawable.rounded_blue
                )
                holder.scheduleMeet.visibility = View.VISIBLE
            } else {
                holder.scheduleMeet.visibility = View.GONE
            }
        } else {
            holder.scheduleMeet.visibility = View.GONE
        }

        holder.carddoc.setOnClickListener { openDoctor(doctor) }

        holder.carddoc.setOnLongClickListener {
            val popup = PopupMenu(context, holder.carddoc)
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

        holder.deleteIcon.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Delete Doctor")
                .setMessage("Are you sure you want to delete ${doctor.docName}?")
                .setPositiveButton("Yes") { dialog, _ ->
                    viewModel.deleteDoctor(doctor.docName)
                    dialog.dismiss()
                }
                .setNegativeButton("No", null)
                .show()
        }
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
