package com.bariatricnepal.app.ui.diet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bariatricnepal.app.BNApplication
import com.bariatricnepal.app.R
import com.bariatricnepal.app.data.api.models.DietChart
import com.bariatricnepal.app.databinding.FragmentDietBinding
import com.bariatricnepal.app.databinding.ItemDietChartDetailBinding
import com.bariatricnepal.app.databinding.ItemMealRowBinding
import com.bariatricnepal.app.ui.common.AccordionBuilder
import com.bariatricnepal.app.util.ApiResult
import com.bariatricnepal.app.util.DateUtils
import com.bariatricnepal.app.util.groupByDateDesc
import kotlinx.coroutines.launch

class DietFragment : Fragment() {
    private var _b: FragmentDietBinding? = null
    private val b get() = _b!!
    private val app get() = requireActivity().application as BNApplication

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentDietBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        b.swipeRefresh.setOnRefreshListener { load() }
        load()
    }

    private fun load() {
        lifecycleScope.launch {
            b.swipeRefresh.isRefreshing = true
            when (val r = app.repository!!.getDietCharts()) {
                is ApiResult.Success -> render(r.data)
                is ApiResult.Error   -> showError(r.message)
            }
            if (_b != null) b.swipeRefresh.isRefreshing = false
        }
    }

    private fun render(charts: List<DietChart>) {
        if (_b == null) return
        val groups = charts.groupByDateDesc { it.created_at }
        AccordionBuilder.build(requireContext(), b.dietContainer, groups, getString(R.string.no_diet)) { parent, chart ->
            val cb = ItemDietChartDetailBinding.inflate(layoutInflater, parent, false)
            cb.tvChartTitle.text = chart.title
            if (chart.valid_from != null || chart.valid_until != null) {
                cb.tvChartValidity.visibility = View.VISIBLE
                cb.tvChartValidity.text = "Valid: ${DateUtils.prettyDate(chart.valid_from)} – ${DateUtils.prettyDate(chart.valid_until)}"
            }
            val sorted = (chart.chart_data ?: emptyList()).sortedBy { it.time ?: "" }
            sorted.forEachIndexed { i, meal ->
                val mb = ItemMealRowBinding.inflate(layoutInflater, cb.mealsContainer, false)
                mb.tvMealName.text = "${i + 1}. ${meal.meal ?: "Meal"}"
                mb.tvMealTime.text = meal.time ?: ""
                mb.tvMealFood.text = meal.food ?: ""
                cb.mealsContainer.addView(mb.root)
            }
            if (!chart.notes.isNullOrBlank()) {
                cb.tvChartNotes.visibility = View.VISIBLE
                cb.tvChartNotes.text = "📋 ${chart.notes}"
            }
            parent.addView(cb.root)
        }
    }

    private fun showError(msg: String) {
        if (_b == null) return
        b.dietContainer.removeAllViews()
        val tv = TextView(requireContext())
        tv.text = msg
        tv.setTextColor(requireContext().getColor(R.color.bn_red))
        tv.textSize = 13f
        tv.setPadding(0, 48, 0, 0)
        b.dietContainer.addView(tv)
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
