package com.mintlifescience.app.adapters

import android.content.Context
import android.graphics.Color
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
import com.google.android.material.card.MaterialCardView
import com.mintlifescience.app.R
import com.mintlifescience.app.model.Medicine

class MedicineAdapter(
    private val context: Context,
    private var dataList: List<Medicine>,
    private val selectedMedicines: MutableList<Medicine>,
    private val alreadySelectedMedicine: List<Medicine>,
    private val onSelectionChanged: (selectedCount: Int) -> Unit = {}
) : RecyclerView.Adapter<MedicineAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: MaterialCardView     = itemView as MaterialCardView
        val shimmer: ShimmerFrameLayout = itemView.findViewById(R.id.shimmer_layout)
        val image: ImageView           = itemView.findViewById(R.id.card_medicine_image)
        val selectedOverlay: View      = itemView.findViewById(R.id.selectedOverlay)
        val selectedIcon: ImageView    = itemView.findViewById(R.id.new_selectedIcon)
        val medicineTitle: TextView    = itemView.findViewById(R.id.card_medicine_name)
        val medicinePrice: TextView    = itemView.findViewById(R.id.priceTxt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        MyViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.viewholder_medicine, parent, false)
        )

    override fun getItemCount() = dataList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val medicine = dataList[position]

        // Text
        holder.medicineTitle.text = medicine.name ?: "Unknown"
        holder.medicinePrice.text = if (!medicine.mrp.isNullOrBlank()) "₹${medicine.mrp}" else "₹ —"

        // Image with shimmer
        holder.shimmer.visibility = View.VISIBLE
        holder.shimmer.startShimmer()
        holder.image.visibility = View.INVISIBLE

        Glide.with(context)
            .load(medicine.image)
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.baseline_image_24)
            .centerCrop()
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?, model: Any?,
                    target: Target<Drawable>?, isFirst: Boolean
                ): Boolean {
                    holder.shimmer.stopShimmer()
                    holder.shimmer.visibility = View.GONE
                    holder.image.visibility = View.VISIBLE
                    return false
                }
                override fun onResourceReady(
                    resource: Drawable, model: Any,
                    target: Target<Drawable>, dataSource: DataSource, isFirst: Boolean
                ): Boolean {
                    holder.shimmer.stopShimmer()
                    holder.shimmer.visibility = View.GONE
                    holder.image.visibility = View.VISIBLE
                    return false
                }
            })
            .into(holder.image)

        // Sync pre-selected medicines into the selection list on first bind
        if (alreadySelectedMedicine.contains(medicine) && !selectedMedicines.contains(medicine)) {
            selectedMedicines.add(medicine)
        }

        updateSelectionUI(holder, selectedMedicines.contains(medicine))

        // Toggle selection on icon click or whole card tap
        val toggle = View.OnClickListener {
            if (selectedMedicines.contains(medicine)) {
                selectedMedicines.remove(medicine)
            } else {
                selectedMedicines.add(medicine)
            }
            updateSelectionUI(holder, selectedMedicines.contains(medicine))
            onSelectionChanged(selectedMedicines.size)
        }
        holder.selectedIcon.setOnClickListener(toggle)
        holder.card.setOnClickListener(toggle)
    }

    override fun onViewRecycled(holder: MyViewHolder) {
        holder.shimmer.stopShimmer()
        super.onViewRecycled(holder)
    }

    private fun updateSelectionUI(holder: MyViewHolder, selected: Boolean) {
        if (selected) {
            // Green card stroke + tint overlay + checkmark icon on green circle
            holder.card.strokeColor = context.getColor(R.color.brand_green)
            holder.selectedOverlay.visibility = View.VISIBLE
            holder.selectedIcon.setImageResource(R.drawable.baseline_done_24)
            holder.selectedIcon.setBackgroundResource(R.drawable.circle_avatar_bg)
            holder.selectedIcon.setColorFilter(Color.WHITE)
        } else {
            // No stroke + no overlay + plus icon on white circle
            holder.card.strokeColor = Color.TRANSPARENT
            holder.selectedOverlay.visibility = View.GONE
            holder.selectedIcon.setImageResource(R.drawable.baseline_add_24)
            holder.selectedIcon.setBackgroundResource(R.drawable.round_white_bg)
            holder.selectedIcon.clearColorFilter()
        }
    }

    fun updateMedicineList(newList: List<Medicine>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = dataList.size
            override fun getNewListSize() = newList.size
            override fun areItemsTheSame(o: Int, n: Int) = dataList[o].name == newList[n].name
            override fun areContentsTheSame(o: Int, n: Int) = dataList[o] == newList[n]
        })
        dataList = newList
        diff.dispatchUpdatesTo(this)
    }
}
