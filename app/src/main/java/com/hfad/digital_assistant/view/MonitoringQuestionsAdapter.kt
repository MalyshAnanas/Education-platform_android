package com.hfad.digital_assistant.view

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.recyclerview.widget.RecyclerView
import com.hfad.digital_assistant.databinding.ItemMonitoringQuestionBinding
import com.hfad.digital_assistant.model.api.CurrentIndicatorDto
import com.hfad.digital_assistant.model.api.IndicatorUpdateItemDto

class MonitoringQuestionsAdapter :
    RecyclerView.Adapter<MonitoringQuestionsAdapter.QuestionViewHolder>() {

    private val items = mutableListOf<CurrentIndicatorDto>()
    private val answers = mutableMapOf<Int, Int>()

    fun submitList(newItems: List<CurrentIndicatorDto>) {
        Log.d("MONITORING_ADAPTER", "submitList size: ${newItems.size}")

        items.clear()
        items.addAll(newItems)

        newItems.forEach {
            answers[it.id] = it.value?.toInt() ?: 3
        }

        Log.d("MONITORING_ADAPTER", "items after add: ${items.size}")

        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        Log.d("MONITORING_ADAPTER", "getItemCount: ${items.size}")
        return items.size
    }

    fun getAnswers(): List<IndicatorUpdateItemDto> {
        return answers.map { (id, value) ->
            IndicatorUpdateItemDto(
                id = id,
                value = value
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val binding = ItemMonitoringQuestionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return QuestionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        Log.d("MONITORING_ADAPTER", "bind item: ${items[position].name}")
        holder.bind(items[position])
    }

    inner class QuestionViewHolder(
        private val binding: ItemMonitoringQuestionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CurrentIndicatorDto) {
            val currentValue = answers[item.id] ?: 3

            binding.questionText.text = item.name
            binding.valueText.text = currentValue.toString()

            binding.seekBar.max = 4
            binding.seekBar.progress = currentValue - 1

            binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val value = progress + 1
                    answers[item.id] = value
                    binding.valueText.text = value.toString()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })


        }
    }
}