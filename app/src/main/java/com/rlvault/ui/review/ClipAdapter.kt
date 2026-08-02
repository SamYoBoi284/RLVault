package com.rlvault.ui.review

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rlvault.data.model.Clip
import com.rlvault.databinding.ItemClipBinding
import java.text.SimpleDateFormat
import java.util.Locale

class ClipAdapter(private val onClipClicked: (Clip) -> Unit) :
    ListAdapter<Clip, ClipAdapter.ClipViewHolder>(DIFF_CALLBACK) {

    private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClipViewHolder {
        val binding = ItemClipBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ClipViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ClipViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ClipViewHolder(private val binding: ItemClipBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(clip: Clip) {
            binding.clipTitleText.text = clip.title ?: "(untitled clip)"
            binding.clipDateText.text = dateFormat.format(java.util.Date(clip.importedAt))
            binding.root.setOnClickListener { onClipClicked(clip) }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Clip>() {
            override fun areItemsTheSame(oldItem: Clip, newItem: Clip) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Clip, newItem: Clip) = oldItem == newItem
        }
    }
}
