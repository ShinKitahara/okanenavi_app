package okanenavi.co.jp.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import okanenavi.co.jp.databinding.ItemRecordBinding
import okanenavi.co.jp.model.Record
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class HomeRecyclerAdapter(private val listener: (Record) -> Unit) :
    ListAdapter<Record, HomeRecyclerAdapter.HomeRecordHolder>(RecordDiffCallback()) {
    class HomeRecordHolder private constructor(private val binding: ItemRecordBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Record) {
            val created = Date(item.date ?: 0)
            binding.createdText.text = SimpleDateFormat("M月d日").format(created)
            binding.debitText.text = item.debit
            binding.priceText.text = NumberFormat.getInstance().format(item.price) + " 円"
            binding.creditText.text = item.credit
        }

        companion object {
            fun from(parent: ViewGroup): HomeRecordHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemRecordBinding.inflate(layoutInflater, parent, false)
                return HomeRecordHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeRecordHolder {
        return HomeRecordHolder.from(parent)
    }

    override fun onBindViewHolder(holder: HomeRecordHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.setOnClickListener { listener(item) }
    }
}

class RecordDiffCallback : DiffUtil.ItemCallback<Record>() {
    override fun areItemsTheSame(oldItem: Record, newItem: Record): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Record, newItem: Record): Boolean {
        return oldItem == newItem
    }
}
