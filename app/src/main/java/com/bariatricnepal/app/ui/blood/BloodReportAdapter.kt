package com.bariatricnepal.app.ui.blood

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bariatricnepal.app.R
import com.bariatricnepal.app.data.api.models.BloodReport
import com.bariatricnepal.app.databinding.ItemBloodReportBinding
import com.bariatricnepal.app.databinding.ItemValueChipBinding
import com.bariatricnepal.app.util.DateUtils

class BloodReportAdapter(private var items: List<BloodReport> = emptyList()) :
    RecyclerView.Adapter<BloodReportAdapter.VH>() {

    fun submitList(newItems: List<BloodReport>) {
        items = newItems
        notifyDataSetChanged()
    }

    class VH(val b: ItemBloodReportBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemBloodReportBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val r = items[position]
        val b = holder.b
        val ctx = holder.itemView.context

        b.tvReportDate.text = DateUtils.prettyDate(r.report_date)
        val reviewed = r.reviewed == "1"
        b.tvReportStatus.text = if (reviewed) "✓ Reviewed" else "Needs Review"
        b.tvReportStatus.setBackgroundResource(
            if (reviewed) R.drawable.bg_pill_success else R.drawable.bg_pill_warning
        )
        b.tvReportStatus.setTextColor(
            ctx.getColor(if (reviewed) R.color.bn_success_text else R.color.bn_warning_text)
        )

        b.flexValues.removeAllViews()
        val inflater = LayoutInflater.from(ctx)

        // CHANGED v13: added WBC, Total Protein, Albumin, LFT to the display list
        val fields = listOf(
            "Hb"            to r.hb,
            "WBC"           to r.wbc,
            "B12"           to r.vitamin_b12,
            "Vit D"         to r.vitamin_d,
            "Total Protein" to r.total_protein,
            "Albumin"       to r.albumin,
            "Ferritin"      to r.ferritin,
            "Calcium"       to r.calcium,
            "Glucose"       to r.glucose,
            "HbA1c"         to r.hba1c,
            "Cholesterol"   to r.cholesterol,
            "Triglycerides" to r.triglycerides,
            "LFT/ALT"       to r.lft
        )
        for ((label, value) in fields) {
            if (value.isNullOrBlank()) continue
            val chip = ItemValueChipBinding.inflate(inflater, b.flexValues, false)
            chip.tvChipLabel.text = label
            chip.tvChipValue.text = value
            b.flexValues.addView(chip.root)
        }

        if (reviewed && !r.doctor_notes.isNullOrBlank()) {
            b.tvDoctorNotes.visibility = View.VISIBLE
            b.tvDoctorNotes.text = "Doctor's notes: ${r.doctor_notes}"
        } else {
            b.tvDoctorNotes.visibility = View.GONE
        }
    }
}
