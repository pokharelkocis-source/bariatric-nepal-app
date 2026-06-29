package com.bariatricnepal.app.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bariatricnepal.app.BNApplication
import com.bariatricnepal.app.R
import com.bariatricnepal.app.data.api.models.AppNotification
import com.bariatricnepal.app.databinding.ActivityNotificationsBinding
import com.bariatricnepal.app.databinding.ItemNotificationBinding
import com.bariatricnepal.app.util.ApiResult
import com.bariatricnepal.app.util.DateUtils
import com.bariatricnepal.app.util.toSafeBool
import kotlinx.coroutines.launch

class NotificationsActivity : AppCompatActivity() {

    private lateinit var b: ActivityNotificationsBinding
    private val app get() = application as BNApplication
    private val adapter = NotifAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(b.root)
        b.btnBack.setOnClickListener { finish() }
        b.tvMarkAll.setOnClickListener { markAllRead() }
        b.rvNotifications.layoutManager = LinearLayoutManager(this)
        b.rvNotifications.adapter = adapter
        b.swipeRefresh.setOnRefreshListener { load() }
        load()
    }

    private fun load() {
        lifecycleScope.launch {
            b.swipeRefresh.isRefreshing = true
            when (val r = app.repository.getNotifications()) {
                is ApiResult.Success -> {
                    adapter.submitList(r.data) { id: String -> markOneRead(id) }
                    b.tvEmpty.visibility = if (r.data.isEmpty()) View.VISIBLE else View.GONE
                }
                is ApiResult.Error -> {
                    b.tvEmpty.visibility = View.VISIBLE
                    b.tvEmpty.text = r.message
                }
            }
            b.swipeRefresh.isRefreshing = false
        }
    }

    private fun markAllRead() {
        lifecycleScope.launch {
            app.repository.markAllNotificationsRead()
            load()
        }
    }

    private fun markOneRead(id: String) {
        lifecycleScope.launch {
            app.repository.markNotificationRead(id)
            load()
        }
    }
}

class NotifAdapter : RecyclerView.Adapter<NotifAdapter.VH>() {

    private var items: List<AppNotification> = emptyList()
    private var onTap: ((String) -> Unit)? = null

    fun submitList(newItems: List<AppNotification>, clickHandler: (String) -> Unit) {
        items = newItems
        onTap = clickHandler
        notifyDataSetChanged()
    }

    class VH(val b: ItemNotificationBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, pos: Int) {
        val n = items[pos]
        val b = holder.b
        val ctx = holder.itemView.context

        b.tvNotifTitle.text = n.title
        b.tvNotifMessage.text = n.message
        b.tvNotifTime.text = DateUtils.timeAgo(n.created_at)

        val icons = mapOf(
            "weight" to "⚖️", "blood_report" to "🩸", "blood_reminder" to "🩸",
            "diet" to "🥗", "medication" to "💊", "complaint" to "💬",
            "complaint_reply" to "💬", "welcome" to "👋"
        )
        b.tvNotifIcon.text = icons[n.type] ?: "📌"

        val unread = !n.is_read.toSafeBool()
        b.dotUnread.visibility = if (unread) View.VISIBLE else View.GONE
        holder.itemView.setBackgroundColor(
            ctx.getColor(if (unread) R.color.bn_info_bg else R.color.white)
        )

        if (unread) {
            holder.itemView.setOnClickListener { onTap?.invoke(n.id) }
        } else {
            holder.itemView.setOnClickListener(null)
        }
    }

    override fun getItemCount(): Int = items.size
}
