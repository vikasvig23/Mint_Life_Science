package com.example.mintlifesciences.addDoctor

import android.app.AlertDialog
import android.content.Intent
import android.util.Log

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.example.mintlifesciences.R
import com.example.mintlifesciences.doctorMedicine.DoctorMedicineActivity
import com.example.mintlifesciences.homescreen.HomeActivity
import com.example.mintlifesciences.medicinePresentation.MedicineScreenActivity
import java.text.SimpleDateFormat
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

        if (itemViewModel.havePresentation) {
            holder.scheduleMeet.text = itemViewModel.scheduleMeet

            // Check if the date is past or future
            val currentDate = System.currentTimeMillis()
            val meetDate = try {
                SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(itemViewModel.scheduleMeet)?.time
            } catch (e: Exception) {
                Log.e("AddDoctorAdapter", "Date parsing failed for: ${itemViewModel.scheduleMeet}", e)
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
}
