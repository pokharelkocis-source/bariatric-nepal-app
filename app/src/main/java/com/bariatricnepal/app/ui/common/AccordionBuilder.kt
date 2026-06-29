package com.bariatricnepal.app.ui.common

import android.animation.ValueAnimator
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.bariatricnepal.app.R
import com.bariatricnepal.app.databinding.ItemAccordionGroupBinding
import com.bariatricnepal.app.util.DateUtils

object AccordionBuilder {
    fun <T> build(
        context: Context,
        container: ViewGroup,
        groups: List<Pair<String, List<T>>>,
        emptyText: String,
        inflateEntry: (parent: LinearLayout, item: T) -> Unit
    ) {
        container.removeAllViews()
        val inflater = LayoutInflater.from(context)

        if (groups.isEmpty()) {
            val tv = TextView(context)
            tv.text = emptyText
            tv.setTextColor(context.getColor(R.color.bn_muted))
            tv.textSize = 13f
            tv.gravity = Gravity.CENTER
            tv.setPadding(0, 64, 0, 64)
            container.addView(tv)
            return
        }

        groups.forEachIndexed { idx, (dateKey, items) ->
            val gb = ItemAccordionGroupBinding.inflate(inflater, container, false)
            val label = if (dateKey == "undated") "Date not recorded"
                        else DateUtils.prettyDateLong(dateKey)
            gb.tvGroupDate.text = "📅 $label"
            gb.tvGroupCount.text = "${items.size} ${if (items.size == 1) "entry" else "entries"}"
            items.forEach { item -> inflateEntry(gb.accordionBody, item) }

            if (idx == 0) {
                gb.accordionBody.visibility = View.VISIBLE
                gb.ivChevron.rotation = 180f
            }
            gb.accordionHeader.setOnClickListener {
                val body = gb.accordionBody
                val chevron = gb.ivChevron
                if (body.visibility == View.VISIBLE) {
                    body.visibility = View.GONE
                    ValueAnimator.ofFloat(180f, 0f).apply {
                        duration = 180
                        addUpdateListener { chevron.rotation = it.animatedValue as Float }
                        start()
                    }
                } else {
                    body.visibility = View.VISIBLE
                    ValueAnimator.ofFloat(0f, 180f).apply {
                        duration = 180
                        addUpdateListener { chevron.rotation = it.animatedValue as Float }
                        start()
                    }
                }
            }
            container.addView(gb.root)
        }
    }
}
