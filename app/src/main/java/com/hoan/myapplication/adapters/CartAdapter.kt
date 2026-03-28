package com.hoan.myapplication.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hoan.myapplication.databinding.CartproductItemBinding
import com.hoan.myapplication.models.Cart
import com.hoan.myapplication.utils.SwipeToDelete

class CartAdapter(
        private val context: Context,
        private val list: ArrayList<Cart>,
        private val onLongClickRemove: OnLongClickRemove,
        private val onQuantityChanged: OnQuantityChanged
) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: CartproductItemBinding) :
            RecyclerView.ViewHolder(binding.root) {

        private val onSwipeDelete =
                object : SwipeToDelete() {
                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        val position = viewHolder.adapterPosition
                        list.removeAt(position)
                    }
                }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                CartproductItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val currentItem = list[position]

        Glide.with(context).load(currentItem.imageUrl).into(holder.binding.ivCartProduct)

        holder.binding.tvCartProductName.text = currentItem.name
        holder.binding.tvCartProductPrice.text = "$${currentItem.price}"
        val initialCount = currentItem.quantity ?: 1
        holder.binding.tvCartItemCount.text = initialCount.toString()
        holder.binding.tvCartProductSize.text = currentItem.size

        var count = initialCount

        holder.binding.btnCartItemAdd.setOnClickListener {
            count++
            currentItem.quantity = count
            holder.binding.tvCartItemCount.text = count.toString()
            onQuantityChanged.onQuantityChanged(currentItem, count, holder.adapterPosition)
        }

        holder.binding.btnCartItemMinus.setOnClickListener {
            if (count > 1) {
                count--
                currentItem.quantity = count
                holder.binding.tvCartItemCount.text = count.toString()
                onQuantityChanged.onQuantityChanged(currentItem, count, holder.adapterPosition)
            }
        }

        holder.itemView.setOnLongClickListener {
            onLongClickRemove.onLongRemove(currentItem, position)
            true
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface OnLongClickRemove {
        fun onLongRemove(item: Cart, position: Int)
    }

    interface OnQuantityChanged {
        fun onQuantityChanged(item: Cart, newQuantity: Int, position: Int)
    }

    abstract class SwipeToDeleteCallback :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
        override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
        ): Boolean {
            return false // không cho phép kéo lên xuống
        }
    }
}
