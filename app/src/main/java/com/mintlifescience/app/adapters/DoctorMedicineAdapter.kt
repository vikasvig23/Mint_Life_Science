package com.mintlifescience.app.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mintlifescience.app.R
import com.mintlifescience.app.model.Medicine

class DoctorMedicineAdapter(
    private val context: Context,
    private var medicines: List<Medicine>,
    private val brandName: String
) : RecyclerView.Adapter<DoctorMedicineAdapter.MyViewHolder>() {

    private var expandedPosition = -1

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val medicineTitle: TextView = itemView.findViewById(R.id.medTitle)
        val medicineSubtitle: TextView = itemView.findViewById(R.id.medicineSubtitle)
        val expandArrow: ImageView = itemView.findViewById(R.id.expandArrow)
        val expandedLayout: View = itemView.findViewById(R.id.expandedLayout)
        val medicineImage: ImageView = itemView.findViewById(R.id.medicineImage)
        val medicineDescription: TextView = itemView.findViewById(R.id.medicineDescription)
        val medicineSalt: TextView = itemView.findViewById(R.id.medicineSalt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        MyViewHolder(LayoutInflater.from(context)
            .inflate(R.layout.doctor_medicine_recycler_item, parent, false))

    override fun getItemCount() = medicines.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val medicine = medicines[position]
        holder.medicineTitle.text = medicine.name
        holder.medicineSubtitle.text = medicine.description
        holder.medicineDescription.text = medicine.description
        holder.medicineSalt.text = "Salt: ${medicine.salt}"

        Glide.with(context)
            .load(medicine.image)
            .placeholder(R.drawable.logo)
            .into(holder.medicineImage)

        val isExpanded = position == expandedPosition
        holder.expandedLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE
        holder.medicineSubtitle.visibility = if (isExpanded) View.GONE else View.VISIBLE
        holder.expandArrow.setImageResource(
            if (isExpanded) R.drawable.baseline_arrow_drop_up_24
            else R.drawable.baseline_arrow_drop_down_24
        )

        val toggle = View.OnClickListener {
            val prev = expandedPosition
            expandedPosition = if (isExpanded) -1 else position
            if (prev != -1) notifyItemChanged(prev)
            notifyItemChanged(position)
        }
        holder.expandArrow.setOnClickListener(toggle)
        holder.itemView.setOnClickListener(toggle)
    }

    fun updateMedicineList(newList: List<Medicine>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = medicines.size
            override fun getNewListSize() = newList.size
            override fun areItemsTheSame(o: Int, n: Int) = medicines[o].name == newList[n].name
            override fun areContentsTheSame(o: Int, n: Int) = medicines[o] == newList[n]
        })
        medicines = newList
        expandedPosition = -1
        diff.dispatchUpdatesTo(this)
    }
}
