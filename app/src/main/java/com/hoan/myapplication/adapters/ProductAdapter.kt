package com.hoan.myapplication.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hoan.myapplication.databinding.ProductItemBinding
import com.hoan.myapplication.models.Product

class ProductDisplayAdapter(
        private val context: Context,
        private val list: List<Product>,
        private val productClickInterface: ProductOnClickInterface,
        private val likeClickInterface: LikeOnClickInterface
) : RecyclerView.Adapter<ProductDisplayAdapter.ViewHolder>() {

    private val likedProducts = mutableSetOf<String>()

    inner class ViewHolder(val binding: ProductItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                ProductItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = list[position]
        holder.binding.tvNameShoeDisplayItem.text = "${currentItem.brand} ${currentItem.name}"
        holder.binding.tvPriceShoeDisplayItem.text = "$${currentItem.price}"

        Glide.with(context).load(currentItem.imageUrl).into(holder.binding.ivShoeDisplayItem)

        val productId = currentItem.id
        val isLiked = productId != null && likedProducts.contains(productId)
        holder.binding.btnLike.isChecked = isLiked
        holder.binding.btnLike.backgroundTintList =
                ColorStateList.valueOf(if (isLiked) Color.RED else Color.WHITE)

        holder.itemView.setOnClickListener { productClickInterface.onClickProduct(currentItem) }

        holder.binding.btnLike.setOnClickListener {
            val btnLike = holder.binding.btnLike
            val id = productId
            if (id == null) {
                btnLike.isChecked = false
                btnLike.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
                return@setOnClickListener
            }

            val nowLiked = !likedProducts.contains(id)
            if (nowLiked) {
                likedProducts.add(id)
                btnLike.isChecked = true
                btnLike.backgroundTintList = ColorStateList.valueOf(Color.RED)
                likeClickInterface.onClickLike(currentItem)
            } else {
                likedProducts.remove(id)
                btnLike.isChecked = false
                btnLike.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
                likeClickInterface.onUnlike(currentItem)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun updateLikedProducts(likedSet: Set<String>) {
        likedProducts.clear()
        likedProducts.addAll(likedSet)
        notifyDataSetChanged()
    }
}

interface ProductOnClickInterface {
    fun onClickProduct(item: Product)
}

interface LikeOnClickInterface {
    fun onClickLike(item: Product)
    fun onUnlike(item: Product)
}
