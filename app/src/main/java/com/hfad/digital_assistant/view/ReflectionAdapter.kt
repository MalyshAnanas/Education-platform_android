package com.hfad.digital_assistant.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hfad.digital_assistant.R
import com.hfad.digital_assistant.model.api.Question
import com.hfad.digital_assistant.viewModel.ReflectionViewModel

class ReflectionAdapter(
    private val viewModel: ReflectionViewModel
) : ListAdapter<Question, RecyclerView.ViewHolder>(QuestionDiffCallback()) {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).type == "choice") 0 else 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_choice, parent, false)
            ChoiceViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_text, parent, false)
            TextViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val question = getItem(position)
        if (holder is ChoiceViewHolder) holder.bind(question)
        if (holder is TextViewHolder) holder.bind(question)
    }

    //  Choice ViewHolder
    inner class ChoiceViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val text = view.findViewById<TextView>(R.id.questionText)
        private val emojis = listOf(
            view.findViewById<ImageView>(R.id.excellent),
            view.findViewById<ImageView>(R.id.good),
            view.findViewById<ImageView>(R.id.normal),
            view.findViewById<ImageView>(R.id.bad),
            view.findViewById<ImageView>(R.id.terrible)
        )

        fun bind(question: Question) {
            text.text = question.text

            // Сброс alpha
            emojis.forEach { it.alpha = 1f }

            val selected = viewModel.getAnswer(question.id) as? Int
            if (selected != null && selected in 1..5) {
                emojis[selected - 1].alpha = 0.2f
            }

            emojis.forEachIndexed { index, imageView ->
                imageView.setOnClickListener {
                    emojis.forEach { it.alpha = 1f }
                    imageView.alpha = 0.2f
                    viewModel.setAnswer(question.id, index + 1)
                }
            }
        }
    }

    //  Text ViewHolder
    inner class TextViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val text = view.findViewById<TextView>(R.id.questionText)
        private val editText = view.findViewById<EditText>(R.id.answerInput)

        fun bind(question: Question) {
            text.text = question.text

            val currentText = editText.text.toString()
            val newText = question.answer ?: ""
            if (currentText != newText) {
                editText.setText(newText)
            }

            // Сохраняем фокус
            editText.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    viewModel.setAnswer(question.id, editText.text.toString())
                }
            }

            editText.doAfterTextChanged {
                viewModel.setAnswer(question.id, it.toString())
            }
        }
    }

    //  DiffUtil
    class QuestionDiffCallback : DiffUtil.ItemCallback<Question>() {
        override fun areItemsTheSame(oldItem: Question, newItem: Question): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Question, newItem: Question): Boolean {
            // Сравниваем текст и текущий ответ
            return oldItem.text == newItem.text && oldItem.answer == newItem.answer
        }
    }
}