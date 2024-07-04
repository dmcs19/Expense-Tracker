package com.example.expensetracker

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Locale

class ViewExpenses : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var auth: FirebaseAuth
    private var user: FirebaseUser? = null
    private val db = Firebase.firestore
    private var email: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_expenses)
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
        }else{
            getMockExpenses { expenses ->
                updateExpenses(expenses)
            }
        }

        clickListeners()
    }

    private fun clickListeners() {
        imageView = findViewById(R.id.btn_back)

        // Go back to the MainActivity
        imageView.setOnClickListener {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun getMockExpenses(callback: (List<Expense>) -> Unit) {
        val expenses = mutableListOf<Expense>()
        db.collection(email.toString())
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    if (document.contains("amount")) {
                        expenses.add(
                            Expense(
                                document.get("name").toString(),
                                "%.2f".format(document.get("amount")).toDouble(),
                                document.get("date").toString(),
                                document.get("category").toString()
                            )
                        )
                    }
                }
                callback(expenses)
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
                callback(expenses)
            }
    }

    private fun updateExpenses(expenses: List<Expense>) {
        // Sort the expenses by date
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        val sortedExpenses = expenses.sortedByDescending { dateFormat.parse(it.date) }

        val tableLayout = findViewById<TableLayout>(R.id.expenses_table)

        // Create header row
        val headerRow = TableRow(this)
        headerRow.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT)

        val headerTextViewParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.0f)

        // Add header cells
        val headers = arrayOf("Name", "Amount", "Date", "Category")
        headers.forEach { headerText ->
            val headerTextView = TextView(this).apply {
                layoutParams = headerTextViewParams
                text = headerText
                setPadding(8, 8, 8, 8)
                setBackgroundResource(R.drawable.header_background) // Set custom header background
                setTextColor(Color.WHITE) // Set text color to white
                gravity = Gravity.CENTER // Center text horizontally and vertically
            }
            headerRow.addView(headerTextView)
        }

        // Add header row to table
        tableLayout.addView(headerRow)

        var currency = ""

        // Fetch active currency from Firestore
        val docRef = db.collection(email.toString()).document("Currencies")
        docRef.get()
            .addOnSuccessListener { document ->
                currency = document.getString("active")?.split(":")?.get(1) ?: ""
                // Add expense rows after fetching currency
                addExpenseRows(tableLayout, sortedExpenses, currency)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting currency document", exception)
            }
    }

    @SuppressLint("SetTextI18n")
    private fun addExpenseRows(tableLayout: TableLayout, expenses: List<Expense>, currency: String) {
        expenses.forEach { expense ->
            val row = TableRow(this)
            val rowParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT)
            row.layoutParams = rowParams

            val textViewParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.0f)

            val nameTextView = TextView(this).apply {
                layoutParams = textViewParams
                text = expense.name
                setPadding(8, 8, 8, 8)
                setBackgroundResource(R.drawable.cell_border)
                maxLines = 1 // Limit to 1 line of text
                ellipsize = TextUtils.TruncateAt.END // Ellipsize if text overflows
                gravity = Gravity.CENTER // Center text horizontally and vertically
            }
            row.addView(nameTextView)

            val amountTextView = TextView(this).apply {
                layoutParams = textViewParams
                text = "${expense.amount} $currency" // Display amount with currency symbol
                setPadding(8, 8, 8, 8)
                setBackgroundResource(R.drawable.cell_border)
                maxLines = 1 // Limit to 1 line of text
                ellipsize = TextUtils.TruncateAt.END // Ellipsize if text overflows
                gravity = Gravity.CENTER // Center text horizontally and vertically
            }
            row.addView(amountTextView)

            val dateTextView = TextView(this).apply {
                layoutParams = textViewParams
                text = expense.date
                setPadding(8, 8, 8, 8)
                setBackgroundResource(R.drawable.cell_border)
                maxLines = 1 // Limit to 1 line of text
                ellipsize = TextUtils.TruncateAt.END // Ellipsize if text overflows
                gravity = Gravity.CENTER // Center text horizontally and vertically
            }
            row.addView(dateTextView)

            val categoryTextView = TextView(this).apply {
                layoutParams = textViewParams
                text = expense.category
                setPadding(8, 8, 8, 8)
                setBackgroundResource(R.drawable.cell_border)
                maxLines = 1 // Limit to 1 line of text
                ellipsize = TextUtils.TruncateAt.END // Ellipsize if text overflows
                gravity = Gravity.CENTER // Center text horizontally and vertically
            }
            row.addView(categoryTextView)

            tableLayout.addView(row)
        }
    }




    data class Expense(
        val name: String,
        val amount: Double,
        val date: String,
        val category: String,
    )
}