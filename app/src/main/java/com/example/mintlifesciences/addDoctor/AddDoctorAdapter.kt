package com.example.mintlifesciences.addDoctor

import android.app.AlertDialog
import android.content.Intent
import android.util.Log

import android.content.Context
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

class AddDoctorAdapter(
    private val context: Context,
    var docList: List<DoctorData>,
    private val brandName: String,
    private val viewModel: AddDoctorViewModel // Pass the ViewModel to handle deletion
) : RecyclerView.Adapter<AddDoctorAdapter.DoctorViewHolder>() {

    class DoctorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val carddoc: CardView = itemView.findViewById(R.id.card_doc)
        val docName: TextView = itemView.findViewById(R.id.tvName)
        val docSpeciality: TextView = itemView.findViewById(R.id.tvClass)
        val deleteIcon: ImageView = itemView.findViewById(R.id.deleteIcon) // Add deleteIcon reference
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

        // Handle click event for viewing doctor's medicines
        holder.carddoc.setOnClickListener {
            val intent = Intent(context, DoctorMedicineActivity::class.java)
            intent.putExtra("doctorName", itemViewModel.docName)
            intent.putExtra("brandName", brandName)
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
                    viewModel.deleteDoctor(brandName, itemViewModel.docName)
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
