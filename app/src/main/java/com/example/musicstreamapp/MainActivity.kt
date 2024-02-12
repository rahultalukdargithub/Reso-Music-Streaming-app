package com.example.musicstreamapp

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var toggle: ActionBarDrawerToggle
    lateinit var drawerLayout: DrawerLayout



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window?.statusBarColor = resources.getColor(R.color.applcolor)
        setSupportActionBar(findViewById(R.id.my_toolbar))
        drawerLayout = findViewById(R.id.my_drawer_layout)
        toggle = ActionBarDrawerToggle(this, drawerLayout,R.string.open,R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        recyclerView = findViewById(R.id.categories_recycler_view)
        getCategories()
        val l=findViewById<RelativeLayout>(R.id.section_1_main_layout)
        val lt=findViewById<TextView>(R.id.section_1_title)
        val ltr=findViewById<RecyclerView>(R.id.section_1_recycler_view)
        setupSection("section_1",l,lt,ltr)
        val l1=findViewById<RelativeLayout>(R.id.section_2_main_layout)
        val lt1=findViewById<TextView>(R.id.section_2_title)
        val ltr1=findViewById<RecyclerView>(R.id.section_2_recycler_view)
        setupSection("section_2",l1,lt1,ltr1)
        val l2=findViewById<RelativeLayout>(R.id.section_3_main_layout)
        val lt2=findViewById<TextView>(R.id.section_3_title)
        val ltr2=findViewById<RecyclerView>(R.id.section_3_recycler_view)
        setupSection("section_3",l2,lt2,ltr2)


        val navv=findViewById<NavigationView>(R.id.navView)
        navv.setNavigationItemSelectedListener{
            when(it.itemId)
            {
                R.id.navAbout-> Toast.makeText(baseContext,"About",Toast.LENGTH_LONG).show()
                R.id.navSettings-> Toast.makeText(baseContext,"Settings",Toast.LENGTH_LONG).show()
                R.id.navExit-> {
                    val builder =AlertDialog.Builder(this)
                    builder.setTitle("Exit")
                        .setMessage("Do You Want to close app?")
                        .setIcon(R.drawable.logo)
                        .setPositiveButton("Yes"){_,_->
                            if(PlayerActivity.musicService!!.mediaPlayer!=null) {
                                PlayerActivity.musicService!!.mediaPlayer!!.release()
                                PlayerActivity.musicService!!.mediaPlayer = null
                            }
                            exitProcess(1)
                        }
                        .setNegativeButton("No"){dialog,_->
                            dialog.dismiss()
                        }
                    val customDialog=builder.create()
                    customDialog.show()
                    customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
                    customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)

                }
            }
            true
        }

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (toggle.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)
    }


    private fun getCategories() {
        FirebaseFirestore.getInstance().collection("category")
            .get().addOnSuccessListener {
                val categoryList = it.toObjects(CategoryModel::class.java)
                setupCategoryRecyclerView(categoryList)
            }
    }

    private fun setupCategoryRecyclerView(categoryList: List<CategoryModel>) {

        categoryAdapter = CategoryAdapter(this@MainActivity,categoryList)
        recyclerView.layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = categoryAdapter
    }



    private fun setupSection(id : String, mainLayout : RelativeLayout, titleView : TextView, recyclerView: RecyclerView){
        FirebaseFirestore.getInstance().collection("sections")
            .document(id)
            .get().addOnSuccessListener {
                val section = it.toObject(CategoryModel::class.java)
                section?.apply {
                    mainLayout.visibility = View.VISIBLE
                    titleView.text = name
                    recyclerView.layoutManager = LinearLayoutManager(this@MainActivity,
                        LinearLayoutManager.HORIZONTAL,false)

                    recyclerView.adapter = SectionSongListAdapter(this@MainActivity,songs)
                    mainLayout.setOnClickListener {
                        SongListActivity.category = section
                        startActivity(Intent(this@MainActivity,SongListActivity::class.java))
                    }
                }
            }

    }

    override fun onDestroy() {
        super.onDestroy()
        if(!PlayerActivity.isPlaying  && PlayerActivity.musicService!=null)
        {
            if(PlayerActivity.musicService != null){
                PlayerActivity.musicService!!.stopForeground(true)
                PlayerActivity.musicService!!.mediaPlayer!!.release()
                PlayerActivity.musicService = null}
            exitProcess(1)
        }
    }


}