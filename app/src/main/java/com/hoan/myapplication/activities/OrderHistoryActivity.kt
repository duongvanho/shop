package com.hoan.myapplication.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hoan.myapplication.adapters.OrderHistoryAdapter
import com.hoan.myapplication.databinding.ActivityOrderHistoryBinding
import com.hoan.myapplication.models.OrderHistory
import com.hoan.myapplication.utils.Extensions.toast

class OrderHistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOrderHistoryBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var historyRef: DatabaseReference

    private val adapter = OrderHistoryAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOrderHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            finish()
            return
        }

        historyRef = FirebaseDatabase.getInstance().getReference("OrderHistory").child(uid)

        binding.rvOrderHistory.layoutManager = LinearLayoutManager(this)
        binding.rvOrderHistory.adapter = adapter

        binding.ivBack.setOnClickListener { finish() }

        loadHistory()
    }

    private fun loadHistory() {
        historyRef
                .orderByChild("createdAt")
                .addListenerForSingleValueEvent(
                        object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val list = mutableListOf<OrderHistory>()

                                for (data in snapshot.children) {
                                    val order = data.getValue(OrderHistory::class.java)
                                    if (order != null) {
                                        if (order.id.isNullOrBlank()) {
                                            order.id = data.key
                                        }
                                        list.add(order)
                                    }
                                }

                                val sorted = list.sortedByDescending { it.createdAt ?: 0L }
                                adapter.submitList(sorted)

                                binding.tvEmpty.visibility =
                                        if (sorted.isEmpty()) View.VISIBLE else View.GONE
                            }

                            override fun onCancelled(error: DatabaseError) {
                                toast(error.message)
                            }
                        }
                )
    }
}
