package com.example.expensetracker

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Calendar

class AddExpense : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var spinner: Spinner
    private lateinit var datePickerEditText: TextInputEditText
    private lateinit var name_input: EditText
    private lateinit var value_input: EditText
    private lateinit var btn_add_expense: Button
    private lateinit var auth: FirebaseAuth
    private var user: FirebaseUser? = null
    private val db = Firebase.firestore
    private var email: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_expense)
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

        clickListeners()

        updateCategories()
    }

    private fun clickListeners(){
        imageView = findViewById(R.id.btn_back)
        btn_add_expense = findViewById(R.id.btn_add_expense)
        datePickerEditText = findViewById(R.id.datePickerEditText)
        name_input = findViewById(R.id.name_input)
        value_input = findViewById(R.id.value_input)

        // Go back to the MainActivity
        imageView.setOnClickListener {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Add the expense to the database
        btn_add_expense.setOnClickListener {
            if (name_input.text.toString().trim().isBlank()) {
                Toast.makeText(this, "Introduce a valid name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (spinner.selectedItem.toString() == "Select a category") {
                Toast.makeText(this, "Select a category", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (value_input.text.toString().trim().isBlank()) {
                Toast.makeText(this, "Introduce a valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (datePickerEditText.text.toString().isEmpty()) {
                Toast.makeText(this, "Select a date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val data = hashMapOf(
                "name" to name_input.text.toString(),
                "amount" to String.format("%.2f", value_input.text.toString().toDouble()).toDouble(),
                "category" to spinner.selectedItem.toString(),
                "date" to datePickerEditText.text.toString()
            )
            db.collection(email.toString()).add(data)
                .addOnSuccessListener { documentReference ->
                    Log.d(TAG, "DocumentSnapshot written with ID: ${documentReference.id}")
                    Toast.makeText(this, "Expense added successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding document", e)
                    Toast.makeText(this, "Error adding the expense", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateCategories(){
        spinner = findViewById(R.id.spinner_categories)

        // Add all the categories existing in the database to the dropdown
        val docRef = db.collection(email.toString()).document("Categories")
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val data = document.data
                    if (data != null) {
                        val categories = mutableListOf<String>()
                        categories.add("Select a category")
                        for ((_, value) in data) {
                            categories.add(value.toString())
                        }
                        val adapter =
                            ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spinner.adapter = adapter
                    }
                } else Log.d(TAG, "No such document")
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
    }

    fun showDatePickerDialog(view: View) {
        val builder = MaterialDatePicker.Builder.datePicker()
        builder.setTitleText("Select Date")

        // Set default date to the current date
        val currentDate = Calendar.getInstance().timeInMillis
        builder.setSelection(currentDate)

        val materialDatePicker = builder.build()

        materialDatePicker.addOnPositiveButtonClickListener {
            // Handle selected date
            val selectedDate = Calendar.getInstance().apply {
                timeInMillis = materialDatePicker.selection!!
            }.time

            // Do something with selectedDate
            datePickerEditText.setText(SimpleDateFormat.getDateInstance().format(selectedDate))
        }

        materialDatePicker.show(supportFragmentManager, "DATE_PICKER")
    }
}