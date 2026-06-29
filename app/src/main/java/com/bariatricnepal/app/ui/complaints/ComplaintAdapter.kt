package com.bariatricnepal.app.ui.complaints

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bariatricnepal.app.R
import com.bariatricnepal.app.data.api.models.Complaint
import com.bariatricnepal.app.databinding.ItemComplaintBinding
import com.bariatricnepal.app.util.DateUtils

class ComplaintAdapter(private var items: List<Complaint> = emptyList()) :
    RecyclerView.Adapter<ComplaintAdapter.VH>() {

    fun submitList(newItems: List<Complaint>) {
        items = newItems
        notifyDataSetChanged()
    }

    class VH(val b: ItemComplaintBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemComplaintBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val c = items[position]
        val b = holder.b
        val ctx = holder.itemView.context

        b.tvComplaintDate.text = DateUtils.prettyDate(c.created_at)
        b.tvComplaintText.text = c.complaint
        b.tvSeverity.text = c.severity.replaceFirstChar { it.uppercase() }

        val (sevBg, sevColor) = when (c.severity) {
            "urgent" -> R.drawable.bg_pill_danger to R.color.bn_danger_text
            "high", "medium" -> R.drawable.bg_pill_warning to R.color.bn_warning_text
            else -> R.drawable.bg_pill_success to R.color.bn_success_text
        }
        b.tvSeverity.setBackgroundResource(sevBg)
        b.tvSeverity.setTextColor(ctx.getColor(sevColor))
        b.tvStatus.text = c.status.replaceFirstChar { it.uppercase() }

        val (statBg, statColor) = when (c.status) {
            "resolved" -> R.drawable.bg_pill_success to R.color.bn_success_text
            "reviewed" -> R.drawable.bg_pill_info to R.color.bn_primary
            else -> R.drawable.bg_stat_card to R.color.bn_muted
        }
        b.tvStatus.setBackgroundResource(statBg)
        b.tvStatus.setTextColor(ctx.getColor(statColor))

        if (c.doctor_reply.isNullOrBlank()) {
            b.tvDoctorReply.visibility = View.GONE
        } else {
            b.tvDoctorReply.visibility = View.VISIBLE
            b.tvDoctorReply.text = "Doctor's reply: ${c.doctor_reply}"
        }
    }
}
