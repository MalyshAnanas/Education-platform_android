package com.hfad.digital_assistant.view

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
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

    private var isReadOnly = false

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

    fun setReadOnly(value: Boolean) {
        isReadOnly = value
        notifyDataSetChanged()
    }

    inner class ChoiceViewHolder(private val root: View) : RecyclerView.ViewHolder(root) {

        private val text = root.findViewById<TextView>(R.id.questionText)
        private val emojis = listOf(
            root.findViewById<ImageView>(R.id.excellent),
            root.findViewById<ImageView>(R.id.good),
            root.findViewById<ImageView>(R.id.normal),
            root.findViewById<ImageView>(R.id.bad),
            root.findViewById<ImageView>(R.id.terrible)
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
                    if (isReadOnly) return@setOnClickListener

                    emojis.forEach { it.alpha = 1f }
                    imageView.alpha = 0.3f
                    viewModel.setAnswer(question.id, index + 1)
                }
            }
        }
    }

    inner class TextViewHolder(private val root: View) : RecyclerView.ViewHolder(root) {

        private val text = root.findViewById<TextView>(R.id.questionText)
        private val editText = root.findViewById<EditText>(R.id.answerInput)
        private val textView = root.findViewById<TextView>(R.id.answerText)

        private var textWatcher: TextWatcher? = null

        fun bind(question: Question) {
            text.text = question.text

            val answer = viewModel.getAnswer(question.id)?.toString().orEmpty()

            if (isReadOnly) {
                editText.visibility = View.GONE
                textView.visibility = View.VISIBLE
                textView.text = if (answer.isNotBlank()) answer else "Нет ответа"
                return
            }

            textView.visibility = View.GONE
            editText.visibility = View.VISIBLE
            editText.isEnabled = true

            textWatcher?.let { editText.removeTextChangedListener(it) }

            if (editText.text.toString() != answer) {
                editText.setText(answer)
                editText.setSelection(editText.text.length)
            }

            textWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

                override fun afterTextChanged(s: Editable?) {
                    if (!isReadOnly) {
                        viewModel.setAnswer(question.id, s?.toString().orEmpty())
                    }
                }
            }

            editText.addTextChangedListener(textWatcher)
        }
    }

    class QuestionDiffCallback : DiffUtil.ItemCallback<Question>() {
        override fun areItemsTheSame(oldItem: Question, newItem: Question): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Question, newItem: Question): Boolean {
            return oldItem == newItem
        }
    }
}