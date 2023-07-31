package com.example.hellofood

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hellofood.databinding.FragmentHomeMenuBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList

class HomeMenuFragment : Fragment(), CategoryAdapter.OnItemClickListener,
    ShopAdapter.OnItemClickListener {
    private var _binding: FragmentHomeMenuBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: DatabaseReference
    private lateinit var categoryList: List<Category>
    private lateinit var shopList: ArrayList<Shop>
    private var itemCount: Int = 0
    private lateinit var parent: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeMenuBinding.inflate(inflater, container, false)

        binding.imgSearch.setOnClickListener {
            (activity as HomeActivity).replaceFragment(SearchFragment(), getString(R.string.text_search))
        }

        binding.etSearch.setOnClickListener {
            (activity as HomeActivity).replaceFragment(SearchFragment(), getString(R.string.text_search))
        }

        binding.tvShowAll.setOnClickListener {
            getShopData()
            it.isGone = true
        }

        binding.imgCart.setOnClickListener {
            startActivity(Intent(context, CartActivity::class.java))
        }

//        categoryList = mutableListOf(
//            Category("Bangladeshi", R.drawable.cuisine_bangladeshi),
//            Category("Middle Eastern", R.drawable.cuisine_middleeastern),
//            Category("Fast Food", R.drawable.cuisine_fastfood),
//            Category("Italian", R.drawable.cuisine_italian),
//            Category("Kebab", R.drawable.cuisine_kebab),
//            Category("Asian", R.drawable.cuisine_asian)
//        )


        categoryList = mutableListOf(
            Category(getString(R.string.title_bangladeshi), R.drawable.cuisine_bangladeshi),
            Category(getString(R.string.title_middle_eastern), R.drawable.cuisine_middleeastern),
            Category(getString(R.string.title_fast_food), R.drawable.cuisine_fastfood),
            Category(getString(R.string.title_italian), R.drawable.cuisine_italian),
            Category(getString(R.string.title_kebab), R.drawable.cuisine_kebab),
            Category(getString(R.string.title_asian), R.drawable.cuisine_asian)
        )
        val categoryapapter = CategoryAdapter(categoryList, this)
        itemCount = categoryapapter.itemCount
        binding.recviewCategory.adapter = categoryapapter
        binding.recviewCategory.layoutManager =
            LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)

        shopList = ArrayList<Shop>()

        getShopData()
        getCartItemCount()

        return binding.root
    }

    fun getCartItemCount() {
        database =
            FirebaseDatabase.getInstance("https://hellofood-c1d85-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("carts").child(Firebase.auth.currentUser!!.uid)
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    //println(dataSnapshot.childrenCount)
                    binding.tvCartCounter.text = dataSnapshot.childrenCount.toString()
                } else {
                    binding.tvCartCounter.text = "0"
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                binding.tvCartCounter.text = "0"
            }
        }
        database.addListenerForSingleValueEvent(postListener)
        //database.addValueEventListener(postListener)
    }

    override fun onCategoryItemClick(position: Int) {
        binding.tvShowAll.isVisible = true
        val clickedItem = categoryList[position]
        val category = getEnFromRus(clickedItem.title)
        for (i in 0..itemCount) {
            getShopDataByCategory(category)
        }
    }

    fun getEnFromRus(name :String?) : String? {
        if (Locale.getDefault().language.equals("en"))
            return name;
        if (name.equals("Азиатская")) {
            return "Asian"
        } else if (name.equals("Ближневосточная")){
            return "Middle Eastern"
        } else if (name.equals("Итальянская")){
            return "Italian"
        } else if (name.equals("Фастфуд")){
            return "Fast Food"
        } else if (name.equals("Кебаб")){
            return "Kebab"
        }

        return "Bangladeshi"
    }

    override fun onShopItemClick(position: Int) {
        val clickedItem = shopList[position]
        val shopName = clickedItem.title
        database =
            FirebaseDatabase.getInstance("https://hellofood-c1d85-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("shops")
        database.orderByChild("title").equalTo(shopName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    dataSnapshot.children.forEach {
                        parent = it.key.toString()
                        val intent = Intent(
                            context, ShopMenuActivity::class.java
                        ).putExtra("shop_id", parent)
                        startActivity(intent)
                    }
                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })
    }

    private fun getShopData() {
        database =
            FirebaseDatabase.getInstance("https://hellofood-c1d85-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("shops")
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    shopList.clear()
                    for (shopSnapshot in snapshot.children) {
                        val shop = shopSnapshot.getValue(Shop::class.java)
                        shopList.add(shop!!)
                    }
                    //println("size ${shopList.size}")
                    binding.recviewShops.adapter = ShopAdapter(shopList, this@HomeMenuFragment)
                    binding.recviewShops.layoutManager =
                        LinearLayoutManager(context)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun getShopDataByCategory(category: String?) {
        database =
            FirebaseDatabase.getInstance("https://hellofood-c1d85-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("shops")
        val orderByCategory = database.orderByChild("category").equalTo(category)
        orderByCategory.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    shopList.clear()
                    for (shopSnapshot in snapshot.children) {
                        val shop = shopSnapshot.getValue(Shop::class.java)
                        shopList.add(shop!!)
                    }
                    binding.recviewShops.adapter = ShopAdapter(shopList, this@HomeMenuFragment)
                    binding.recviewShops.layoutManager =
                        LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}