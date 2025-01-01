package com.example.mintlifesciences.addDoctor

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mintlifesciences.R
import com.example.mintlifesciences.model.FeedbackData

class FeedbackAdapter : RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder>() {

    private var feedbackList: List<FeedbackData> = listOf()

    class FeedbackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val feedbackText: TextView = itemView.findViewById(R.id.feedbackText)
        val feedbackDate: TextView = itemView.findViewById(R.id.feedbackDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedbackViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.feedback_item, parent, false)
        return FeedbackViewHolder(view)
    }

    override fun getItemCount(): Int = feedbackList.size

    override fun onBindViewHolder(holder: FeedbackViewHolder, position: Int) {
        val feedback = feedbackList[position]
        holder.feedbackText.text = feedback.message
        holder.feedbackDate.text = feedback.date
    }

    fun submitList(result: Result<List<FeedbackData>>) {
        result.onSuccess { newList ->
            feedbackList = newList
            notifyDataSetChanged()
        }.onFailure { exception ->
            Log.e("FeedbackAdapter", "Error updating feedback list: ${exception.message}")
        }
    }
}