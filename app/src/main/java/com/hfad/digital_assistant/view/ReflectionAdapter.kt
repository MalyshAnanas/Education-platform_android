package com.hfad.digital_assistant.view

import android.annotation.SuppressLint
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

            emojis.forEach { it.alpha = 1f }

            val selected = viewModel.getAnswer(question.id) as? Int

            if (selected != null && selected in 1..5) {
                emojis[selected - 1].alpha = 0.3f
            }

            emojis.forEachIndexed { index, imageView ->

                imageView.setOnClickListener {

                    if (isReadOnly) return@setOnClickListener  // ВОТ ЭТО ГЛАВНОЕ

                    emojis.forEach { it.alpha = 1f }
                    imageView.alpha = 0.3f

                    viewModel.setAnswer(question.id, index + 1)
                }
            }
        }
    }

    //  Text ViewHolder
    inner class TextViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val text = view.findViewById<TextView>(R.id.questionText)
        private val editText = view.findViewById<EditText>(R.id.answerInput)
        private val textView = view.findViewById<TextView>(R.id.answerText)

        fun bind(question: Question) {

            text.text = question.text

            val answer = viewModel.getAnswer(question.id)?.toString() ?: ""

            if (isReadOnly) {

                editText.visibility = View.GONE
                textView.visibility = View.VISIBLE

                textView.text = if (answer.isNotEmpty()) answer else "Нет ответа"

            } else {

                editText.visibility = View.VISIBLE
                textView.visibility = View.GONE

                editText.isEnabled = true

                if (!editText.hasFocus()) {
                    editText.setText(answer)
                }

                editText.doAfterTextChanged {
                    if (!isReadOnly) {
                        viewModel.setAnswer(question.id, it.toString())
                    }
                }
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
            return oldItem.text == newItem.text && oldItem.user_answer == newItem.user_answer
        }
    }

    // Ставим режим "только для чтения"
    private var isReadOnly = false

    fun setReadOnly(value: Boolean) {
        isReadOnly = value
        notifyDataSetChanged()
    }
}