package com.example.mintlifesciences.addDoctor

import android.app.AlertDialog
import android.content.Intent
import android.util.Log

import android.content.Context
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mintlifesciences.R
import com.example.mintlifesciences.homescreen.HomeActivity
import com.example.mintlifesciences.medicinePresentation.MedicineScreenActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddDoctorAdapter(
    private val context: Context,
    var docList: List<DoctorData>,
    private val viewModel: AddDoctorViewModel // Pass the ViewModel to handle deletion
) : RecyclerView.Adapter<AddDoctorAdapter.DoctorViewHolder>() {

    class DoctorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val carddoc: CardView = itemView.findViewById(R.id.card_doc)
        val docName: TextView = itemView.findViewById(R.id.tvName)
        val docSpeciality: TextView = itemView.findViewById(R.id.tvClass)
        val deleteIcon: ImageView = itemView.findViewById(R.id.deleteIcon)
        val scheduleMeet : TextView = itemView.findViewById(R.id.scheduleMeet)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.doctor_list, parent, false)
        return DoctorViewHolder(view)
    }

    override fun getItemCount(): Int {
        return docList.size
    }

    override fun onBindViewHolder(holder: DoctorViewHolder, position: Int) {
        val itemViewModel = docList[position]
        holder.docName.text = itemViewModel.docName
        holder.docSpeciality.text = itemViewModel.docSpeciality


        if (itemViewModel.havePresentation && itemViewModel.scheduleMeet.isNotEmpty()) {
            holder.scheduleMeet.text = itemViewModel.scheduleMeet

            // Get the current date (truncated to remove the time part)
            val currentDate = Calendar.getInstance()
            currentDate.timeInMillis = System.currentTimeMillis()
            currentDate.set(Calendar.HOUR_OF_DAY, 0)
            currentDate.set(Calendar.MINUTE, 0)
            currentDate.set(Calendar.SECOND, 0)
            currentDate.set(Calendar.MILLISECOND, 0)

            val meetDate = try {
                val parsedDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(itemViewModel.scheduleMeet)
                if (parsedDate != null) {
                    val meetCalendar = Calendar.getInstance()
                    meetCalendar.time = parsedDate
                    meetCalendar.set(Calendar.HOUR_OF_DAY, 0)
                    meetCalendar.set(Calendar.MINUTE, 0)
                    meetCalendar.set(Calendar.SECOND, 0)
                    meetCalendar.set(Calendar.MILLISECOND, 0)
                    meetCalendar.timeInMillis
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e("AddDoctorAdapter", "Date parsing failed for: ${itemViewModel.scheduleMeet}", e)
                null
            }

            if (meetDate != null) {
                if (meetDate < currentDate.timeInMillis) {
                    Log.d("Sahil", "${itemViewModel.docName}: meetdate $meetDate: currentData ${currentDate.timeInMillis}")
                    holder.scheduleMeet.setBackgroundResource(R.drawable.rounded_red)
                } else {
                    holder.scheduleMeet.setBackgroundResource(R.drawable.rounded_blue)
                }

                holder.scheduleMeet.visibility = View.VISIBLE
            } else {
                holder.scheduleMeet.visibility = View.GONE
            }
        }else{
            holder.scheduleMeet.visibility = View.GONE
        }

        holder.carddoc.setOnClickListener {
            val intent: Intent = if (itemViewModel.havePresentation) {
                Intent(context, MedicineScreenActivity::class.java)
            } else {
                Intent(context, HomeActivity::class.java)
            }
            intent.putExtra("doctorName", itemViewModel.docName) // Pass doctorName
            intent.putExtra("recentDoctor", false) // Pass doctorName
            context.startActivity(intent)
        }

        // Handle the Long pressed
        holder.carddoc.setOnLongClickListener {
            val popupMenu = PopupMenu(context, holder.carddoc)
            val inflater: MenuInflater = popupMenu.menuInflater
            inflater.inflate(R.menu.card_doc_menu, popupMenu.menu)

            // Align the menu to the right side
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                popupMenu.gravity = android.view.Gravity.END
            }

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_open -> {
                        // Handle "Open" action
                        val intent: Intent = if (itemViewModel.havePresentation) {
                            Intent(context, MedicineScreenActivity::class.java)
                        } else {
                            Intent(context, HomeActivity::class.java)
                        }
                        intent.putExtra("doctorName", itemViewModel.docName)
                        intent.putExtra("recentDoctor", false)
                        context.startActivity(intent)
                        true
                    }
                    R.id.menu_feedback_history -> {
                        // Show feedback dialog
                        showFeedbackDialog(itemViewModel.docName)
                        true
                    }
                    else -> false
                }
            }

            popupMenu.show()
            true
        }


        // Handle delete icon click
        holder.deleteIcon.setOnClickListener {
            // Show confirmation dialog before deleting
            AlertDialog.Builder(context)
                .setTitle("Delete Doctor")
                .setMessage("Are you sure you want to delete this doctor?")
                .setPositiveButton("Yes") { dialog, _ ->
                    // Call ViewModel to delete the doctor from Firebase
                    viewModel.deleteDoctor(itemViewModel.docName)
                    // Remove the doctor from the local list and notify the adapter
                    val updatedList = docList.toMutableList().apply { removeAt(position) }
                    updateList(updatedList)
                    dialog.dismiss()
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    fun updateList(newDocList: List<DoctorData>) {
        docList = newDocList
        notifyDataSetChanged()
        Log.d("RecyclerViewBinding", "List updated: $newDocList")
    }

    private fun showFeedbackDialog(doctorName: String) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_feedback_history, null)
        val recyclerView: RecyclerView = dialogView.findViewById(R.id.feedbackRecyclerView)
        val dialogTitle: TextView = dialogView.findViewById(R.id.dialogTitle)
        val noFeedbackText: TextView = dialogView.findViewById(R.id.noFeedbackText)

        // Set up RecyclerView
        val feedbackAdapter = FeedbackAdapter() // Create and set up your adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = feedbackAdapter

        noFeedbackText.visibility = View.GONE

        // Fetch feedback data for the selected doctor
        viewModel.fetchFeedbackForDoctor(doctorName) { result ->
            result.onSuccess { feedbackList ->
                if (feedbackList.isEmpty()) {
                    // Show "No feedback available" text if the list is empty
                    noFeedbackText.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    feedbackAdapter.submitList(Result.success(feedbackList))
                }
            }.onFailure {
                Log.d("FeedbackList", "Unable to fetch the feedback List.")
            }
        }

        // Show dialog
        AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(true)
            .setNegativeButton("Close") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
