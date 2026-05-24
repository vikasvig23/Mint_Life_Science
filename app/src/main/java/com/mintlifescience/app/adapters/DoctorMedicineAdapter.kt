package com.mintlifescience.app.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.facebook.shimmer.ShimmerFrameLayout
import com.mintlifescience.app.R
import com.mintlifescience.app.model.Medicine

class DoctorMedicineAdapter(
    private val context: Context,
    private var medicines: List<Medicine>,
    private val brandName: String
) : RecyclerView.Adapter<DoctorMedicineAdapter.MyViewHolder>() {

    private var expandedPosition = -1

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val medicineTitle: TextView      = itemView.findViewById(R.id.medTitle)
        val medicineSalt: TextView       = itemView.findViewById(R.id.medicineSalt)
        val expandArrow: ImageView       = itemView.findViewById(R.id.expandArrow)
        val expandedLayout: View         = itemView.findViewById(R.id.expandedLayout)
        val shimmer: ShimmerFrameLayout  = itemView.findViewById(R.id.shimmer_layout)
        val medicineImage: ImageView     = itemView.findViewById(R.id.medicineImage)
        val medicineDescription: TextView = itemView.findViewById(R.id.medicineDescription)
        val mrpChip: TextView            = itemView.findViewById(R.id.mrpChip)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        MyViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.doctor_medicine_recycler_item, parent, false)
        )

    override fun getItemCount() = medicines.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val medicine = medicines[position]

        // ── Collapsed header ──
        holder.medicineTitle.text = medicine.name ?: "Unknown"
        holder.medicineSalt.text = if (!medicine.salt.isNullOrBlank())
            "Salt: ${medicine.salt}" else "Salt: —"

        // ── Expand / collapse toggle ──
        val isExpanded = position == expandedPosition
        holder.expandedLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE
        holder.expandArrow.setImageResource(
            if (isExpanded) R.drawable.baseline_arrow_drop_up_24
            else R.drawable.baseline_arrow_drop_down_24
        )

        // ── Expanded content ──
        if (isExpanded) {
            // MRP chip
            if (!medicine.mrp.isNullOrBlank()) {
                holder.mrpChip.text = "MRP  ₹${medicine.mrp}"
                holder.mrpChip.visibility = View.VISIBLE
            } else {
                holder.mrpChip.visibility = View.GONE
            }

            // Description
            holder.medicineDescription.text = medicine.description?.takeIf { it.isNotBlank() }
                ?: "No description available."

            // Image with shimmer
            if (!medicine.image.isNullOrBlank()) {
                holder.medicineImage.visibility = View.VISIBLE
                holder.shimmer.visibility = View.VISIBLE
                holder.shimmer.startShimmer()

                Glide.with(context)
                    .load(medicine.image)
                    .placeholder(R.drawable.logo)
                    .error(R.drawable.baseline_image_24)
                    .centerCrop()
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?, model: Any?,
                            target: Target<Drawable>?, isFirst: Boolean
                        ): Boolean {
                            holder.shimmer.stopShimmer()
                            holder.shimmer.visibility = View.GONE
                            return false
                        }
                        override fun onResourceReady(
                            resource: Drawable, model: Any,
                            target: Target<Drawable>, dataSource: DataSource, isFirst: Boolean
                        ): Boolean {
                            holder.shimmer.stopShimmer()
                            holder.shimmer.visibility = View.GONE
                            return false
                        }
                    })
                    .into(holder.medicineImage)
            } else {
                holder.shimmer.visibility = View.GONE
                holder.medicineImage.visibility = View.GONE
            }
        } else {
            // Stop shimmer when collapsed so it doesn't run in the background
            holder.shimmer.stopShimmer()
            holder.shimmer.visibility = View.GONE
        }

        // ── Click anywhere on the card to expand/collapse ──
        val toggle = View.OnClickListener {
            val prev = expandedPosition
            expandedPosition = if (isExpanded) -1 else holder.adapterPosition
            if (prev != -1) notifyItemChanged(prev)
            notifyItemChanged(holder.adapterPosition)
        }
        holder.expandArrow.setOnClickListener(toggle)
        holder.itemView.setOnClickListener(toggle)
    }

    override fun onViewRecycled(holder: MyViewHolder) {
        holder.shimmer.stopShimmer()
        super.onViewRecycled(holder)
    }

    fun updateMedicineList(newList: List<Medicine>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = medicines.size
            override fun getNewListSize() = newList.size
            override fun areItemsTheSame(o: Int, n: Int) =
                medicines[o].name == newList[n].name
            override fun areContentsTheSame(o: Int, n: Int) =
                medicines[o] == newList[n]
        })
        medicines = newList
        expandedPosition = -1
        diff.dispatchUpdatesTo(this)
    }
}
