package com.hoan.myapplication.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hoan.myapplication.databinding.ItemOrderHistoryBinding
import com.hoan.myapplication.models.OrderHistory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderHistoryAdapter(private var items: List<OrderHistory> = emptyList()) :
        RecyclerView.Adapter<OrderHistoryAdapter.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    inner class ViewHolder(val binding: ItemOrderHistoryBinding) :
            RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
                ItemOrderHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = items[position]

        val createdAt = order.createdAt ?: 0L
        val dateText = if (createdAt > 0L) dateFormat.format(Date(createdAt)) else ""

        holder.binding.tvOrderId.text = "no: ${order.id.orEmpty()}"
        holder.binding.tvOrderDate.text = dateText
        holder.binding.tvOrderItems.text = "quantity: ${order.totalItems ?: 0}"
        holder.binding.tvOrderTotal.text = "price: ${order.totalPrice ?: 0}"
    }

    fun submitList(newItems: List<OrderHistory>) {
        items = newItems
        notifyDataSetChanged()
    }
}
