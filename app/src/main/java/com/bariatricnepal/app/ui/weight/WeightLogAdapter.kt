package com.bariatricnepal.app.ui.weight

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bariatricnepal.app.data.api.models.WeightLog
import com.bariatricnepal.app.databinding.ItemWeightLogBinding
import com.bariatricnepal.app.util.DateUtils
import com.bariatricnepal.app.util.formatNumber

class WeightLogAdapter(private var items: List<WeightLog> = emptyList()) :
    RecyclerView.Adapter<WeightLogAdapter.VH>() {

    fun submitList(newItems: List<WeightLog>) {
        items = newItems
        notifyDataSetChanged()
    }

    class VH(val b: ItemWeightLogBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemWeightLogBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.b.tvWeightValue.text = item.weight_kg.formatNumber("kg")
        holder.b.tvWeightDate.text = DateUtils.prettyDateTime(item.logged_at)
        if (item.bmi.isNullOrBlank()) {
            holder.b.tvWeightBmi.visibility = View.GONE
        } else {
            holder.b.tvWeightBmi.visibility = View.VISIBLE
            holder.b.tvWeightBmi.text = "BMI ${item.bmi.formatNumber()}"
        }
        if (item.notes.isNullOrBlank()) {
            holder.b.tvWeightNotes.visibility = View.GONE
        } else {
            holder.b.tvWeightNotes.visibility = View.VISIBLE
            holder.b.tvWeightNotes.text = item.notes
        }
    }
}
