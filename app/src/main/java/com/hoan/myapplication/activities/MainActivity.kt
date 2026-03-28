package com.hoan.myapplication.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.hoan.myapplication.R
import com.hoan.myapplication.databinding.ActivityMainBinding
import com.hoan.myapplication.fragments.CartFragment
import com.hoan.myapplication.fragments.LikeFragment
import com.hoan.myapplication.fragments.MainFragment
import com.hoan.myapplication.fragments.ProfileFragment
import com.hoan.myapplication.fragments.SearchFragment

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        val showCart = intent.getBooleanExtra(EXTRA_SHOW_CART, false)
        if (showCart) {
            replaceFragment(CartFragment.newInstance(), TAG_CART)
            binding.bnvMain.selectedItemId = R.id.cartFragment
        } else {
            replaceFragment(MainFragment.newInstance(), TAG_HOME)
        }

        binding.bnvMain.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.mainFragment -> {
                    replaceFragment(MainFragment.newInstance(), TAG_HOME)
                    true
                }
                R.id.searchFragment -> {
                    replaceFragment(SearchFragment.newInstance(), TAG_SEARCH)
                    true
                }
                R.id.likeFragment -> {
                    replaceFragment(LikeFragment.newInstance(), TAG_LIKE)
                    true
                }
                R.id.cartFragment -> {
                    replaceFragment(CartFragment.newInstance(), TAG_CART)
                    true
                }
                R.id.profileFragment -> {
                    replaceFragment(ProfileFragment.newInstance(), TAG_PROFILE)
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment, tag: String) {
        binding.bnvMain.visibility = View.VISIBLE

        val tx = supportFragmentManager.beginTransaction()
        supportFragmentManager.fragments.forEach { tx.hide(it) }
        supportFragmentManager.findFragmentByTag(tag)?.let { tx.show(it) }
                ?: run { tx.add(R.id.fragmentContainerView, fragment, tag) }
        tx.commit()
    }

    companion object {
        private const val TAG_HOME = "HOME"
        private const val TAG_SEARCH = "SEARCH"
        private const val TAG_CART = "CART"
        private const val TAG_LIKE = "LIKE"
        private const val TAG_PROFILE = "PROFILE"

        const val EXTRA_SHOW_CART = "show_cart"
    }
}
