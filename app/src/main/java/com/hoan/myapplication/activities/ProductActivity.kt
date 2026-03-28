package com.hoan.myapplication.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hoan.myapplication.R
import com.hoan.myapplication.adapters.SizeAdapter
import com.hoan.myapplication.adapters.SizeOnClickInterface
import com.hoan.myapplication.databinding.ActivityProductBinding
import com.hoan.myapplication.fragments.CartFragment
import com.hoan.myapplication.models.Order
import com.hoan.myapplication.models.Product

class ProductActivity : AppCompatActivity() , SizeOnClickInterface {
    private lateinit var binding : ActivityProductBinding
    private lateinit var productDatabaseReference: DatabaseReference
    private lateinit var sizeAdapter: SizeAdapter
    private lateinit var auth: FirebaseAuth

    private lateinit var currentUID :  String
    private lateinit var orderImageUrl:String
    private lateinit var orderName:String
    private var orderSize:String?  = null
    private var orderQuantity:Int  = 1
    private lateinit var orderPrice:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProductBinding.inflate(layoutInflater)

        setContentView(binding.root)

        productDatabaseReference = FirebaseDatabase.getInstance().getReference("products")

        val productId = intent.getStringExtra("productId")
        auth = FirebaseAuth.getInstance()

        currentUID = auth.currentUser?.uid ?: ""

        binding.detailActualToolbar.setNavigationOnClickListener {
            finish()
        }


        val valueEvent = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val products = dataSnapshot.getValue(Product::class.java)

                        if (products!!.id == productId) {
                            Glide
                                .with(this@ProductActivity)
                                .load(products.imageUrl)
                                .into(binding.ivDetails)

                            orderImageUrl = products.imageUrl!!
                            orderName = products.name!!
                            orderPrice = products.price!!

                            binding.tvDetailsProductPrice.text = "$${products.price}"
                            binding.tvDetailsProductName.text = "${products.brand} ${products.name}"
                            binding.tvDetailsProductDescription.text = products.description
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProductActivity, error.message, Toast.LENGTH_SHORT).show()
            }

        }


        productDatabaseReference.addValueEventListener(valueEvent)


        val sizeList = ArrayList<String>()
        sizeList.add("5")
        sizeList.add("6")
        sizeList.add("7")
        sizeList.add("8")
        sizeList.add("9")
        sizeList.add("10")


        sizeAdapter = SizeAdapter(this@ProductActivity , sizeList , this)
        binding.rvSelectSize.adapter = sizeAdapter


        binding.btnDetailsAddToCart.setOnClickListener {
            val orderedProduct = Order(
                id = null,
                uid = currentUID,
                pid = productId,
                imageUrl = orderImageUrl,
                name = orderName,
                size = orderSize,
                quantity = orderQuantity,
                price = orderPrice
            )

            if(orderSize.isNullOrBlank()){
                Toast.makeText(this@ProductActivity, "Select Size", Toast.LENGTH_SHORT).show()
            }else{
                addDataToOrdersDatabase(orderedProduct)

                Toast.makeText(this@ProductActivity, "Added to Cart", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra(MainActivity.EXTRA_SHOW_CART, true)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                startActivity(intent)
                finish()
            }


        }

    }


    private fun addDataToOrdersDatabase(orderedProduct: Order) {
        val orderDatabaseReference = FirebaseDatabase.getInstance().getReference("orders")

        val orderId = orderDatabaseReference.push().key
        orderedProduct.id = orderId

        orderDatabaseReference.child(orderId!!).setValue(orderedProduct)
            .addOnSuccessListener {
                Toast.makeText(this@ProductActivity, "Order successfully added to database!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
               Toast.makeText(this@ProductActivity, "Failed to add order: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }


    override fun onClickSize(button: Button, position :Int) {
        orderSize = button.text.toString()
        Toast.makeText(this@ProductActivity, "Size ${button.text} Selected", Toast.LENGTH_SHORT).show()
    }


}