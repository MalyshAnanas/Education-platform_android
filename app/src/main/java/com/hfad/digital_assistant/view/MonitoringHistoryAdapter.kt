package com.hfad.digital_assistant.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hfad.digital_assistant.databinding.ItemMonitoringHistoryBinding
import com.hfad.digital_assistant.model.api.HistoryPeriodDto

class MonitoringHistoryAdapter :
    RecyclerView.Adapter<MonitoringHistoryAdapter.HistoryViewHolder>() {

    private val items = mutableListOf<HistoryPeriodDto>()

    fun submitList(newItems: List<HistoryPeriodDto>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemMonitoringHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class HistoryViewHolder(
        private val binding: ItemMonitoringHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HistoryPeriodDto) {
            binding.periodText.text = item.period

            val text = item.indicators.joinToString("\n") {
                "${it.name}: ${it.value.toInt()}"
            }

            binding.answersText.text = text
        }
    }
}