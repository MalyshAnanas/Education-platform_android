package com.hfad.digital_assistant.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.hfad.digital_assistant.R
import com.hfad.digital_assistant.model.api.CaseUiStatus
import com.hfad.digital_assistant.model.api.PracticumCaseUi
import com.hfad.digital_assistant.viewModel.PracticumViewModel

class CaseAdapter(
    private val onClick: (PracticumCaseUi) -> Unit
) : ListAdapter<PracticumCaseUi, CaseAdapter.VH>(Diff()) {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view.findViewById(R.id.card)
        val title: TextView = view.findViewById(R.id.title)
        val desc: TextView = view.findViewById(R.id.desc)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_case, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)

        holder.title.text = item.name
        holder.desc.text = item.description.take(80) + "..."

        val color = when (item.status) {
            CaseUiStatus.OPEN -> R.color.blue_for_blocks
            CaseUiStatus.CHECKING -> R.color.gray_for_blocks
            CaseUiStatus.DONE -> R.color.gray_for_blocks
        }

        holder.card.setCardBackgroundColor(
            ContextCompat.getColor(holder.itemView.context, color)
        )

        holder.itemView.setOnClickListener {
            if (item.status == CaseUiStatus.OPEN) {
                onClick(item)
            }
        }
    }

    class Diff : DiffUtil.ItemCallback<PracticumCaseUi>() {
        override fun areItemsTheSame(old: PracticumCaseUi, new: PracticumCaseUi) =
            old.id == new.id

        override fun areContentsTheSame(old: PracticumCaseUi, new: PracticumCaseUi) =
            old == new
    }
}