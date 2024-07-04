package com.example.expensetracker

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.firestore

class Categories : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var btnAddCategory: FloatingActionButton
    private lateinit var inputCategory: EditText
    private lateinit var auth: FirebaseAuth
    private var user: FirebaseUser? = null
    private val db = Firebase.firestore
    private var email: String? = null

    private val categories = mutableListOf<String>()
    private lateinit var adapter: CategoriesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_categories)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser
        email = user!!.email
        if (user == null) {
            val intent = Intent(applicationContext, Login::class.java)
            startActivity(intent)
            finish()
        }
        setupRecyclerView()
        clickListeners()
        updateCategories()
    }

    private fun setupRecyclerView() {
        adapter = CategoriesAdapter(categories)
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view_categories)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun clickListeners(){
        imageView = findViewById(R.id.btn_back)
        btnAddCategory = findViewById(R.id.fab_add_category)
        inputCategory = findViewById(R.id.input_new_category)

        // Go back to the MainActivity
        imageView.setOnClickListener{
            val intent = Intent(applicationContext, Profile::class.java)
            startActivity(intent)
            finish()
        }

        // Check if user is premium before allowing category creation
        checkIfUserIsPremium(email!!) { isPremium ->
            if (isPremium) {
                btnAddCategory.setOnClickListener {
                    createNewCategory()
                }
            } else {
                btnAddCategory.setOnClickListener {
                    showPremiumOnlyMessage()
                }
            }
        }
    }

    private fun createNewCategory() {
        val newCategory = inputCategory.text.toString().trim()
        if(newCategory.isNotBlank()){
            val categoriesRef = db.collection(email!!).document("Categories")
            categoriesRef.update(newCategory, newCategory)
                .addOnSuccessListener {
                    categories.add(newCategory)
                    adapter.notifyItemInserted(categories.size - 1)
                    inputCategory.text.clear()
                }
                .addOnFailureListener { exception ->
                    Log.d(ContentValues.TAG, "Error adding document", exception)
                }
        } else {
            Toast.makeText(baseContext, "Write the name of the new category" , Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateCategories(){
        // Add all the categories existing in the database to the page
        val docRef = db.collection(email.toString()).document("Categories")
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val data = document.data
                    if (data != null) {
                        categories.clear()
                        for ((_, value) in data) {
                            categories.add(value.toString())
                            println(value.toString())
                        }
                        adapter.notifyDataSetChanged()
                    }
                } else Log.d(ContentValues.TAG, "No such document")
            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "get failed with ", exception)
            }
    }

    private fun showPremiumOnlyMessage() {
        Snackbar.make(findViewById(R.id.main), "This functionality is only for premium users.", Snackbar.LENGTH_LONG).show()
    }

    private fun checkIfUserIsPremium(email: String, callback: (Boolean) -> Unit) {
        val docRef = db.collection(email).document("Plan")
        docRef.get().addOnSuccessListener { document ->
            val isPremium = document?.getBoolean("premium") ?: false
            callback(isPremium)
        }.addOnFailureListener {
            callback(false)
        }
    }

    class CategoriesAdapter(private val categories: List<String>) : RecyclerView.Adapter<CategoriesAdapter.CategoryViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
            return CategoryViewHolder(view as TextView)
        }

        override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
            holder.textView.text = categories[position]
        }

        override fun getItemCount() = categories.size

        class CategoryViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)
    }
}
