package com.example.expensetracker

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var linearLayout: LinearLayout
    private lateinit var btn_add_expense: LinearLayout
    private lateinit var btn_view_all_expenses: LinearLayout
    private lateinit var btn_currency_converter: LinearLayout
    private lateinit var btn_profile: LinearLayout
    private lateinit var totalExpense: TextView
    private lateinit var category1: TextView
    private lateinit var category2: TextView
    private lateinit var category3: TextView
    private lateinit var value1: TextView
    private lateinit var value2: TextView
    private lateinit var value3: TextView
    private lateinit var auth: FirebaseAuth
    private var user: FirebaseUser? = null
    private val db = Firebase.firestore
    private var email: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
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

        updateExpenses()

        clickListeners()

        animateViewsOnLoad()
    }


    // Add this function to show a message for non-premium users
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

    private fun clickListeners(){
        imageView = findViewById(R.id.profile_icon)
        linearLayout = findViewById(R.id.linearLayout)
        btn_add_expense = findViewById(R.id.btn_add_expense)
        btn_view_all_expenses = findViewById(R.id.btn_view_all_expenses)
        btn_currency_converter = findViewById(R.id.btn_currency_converter)
        btn_profile = findViewById(R.id.btn_profile)

        // Go to the AddExpense activity
        btn_add_expense.setOnClickListener {
            val intent = Intent(applicationContext, AddExpense::class.java)
            startActivity(intent)
        }

        // Go to the ViewExpenses activity
        btn_view_all_expenses.setOnClickListener {
            val intent = Intent(applicationContext, ViewExpenses::class.java)
            startActivity(intent)
        }

        checkIfUserIsPremium(email!!) { isPremium ->
            if (isPremium) {
                btn_currency_converter.setOnClickListener {
                    val intent = Intent(applicationContext, CurrencyConverter::class.java)
                    startActivity(intent)
                }
            } else {
                btn_currency_converter.setOnClickListener {
                    showPremiumOnlyMessage()
                }
            }
        }

        // Go to the profile activity
        imageView.setOnClickListener {
            val intent = Intent(applicationContext, Profile::class.java)
            startActivity(intent)
        }

        btn_profile.setOnClickListener {
            val intent = Intent(applicationContext, Profile::class.java)
            startActivity(intent)
        }
    }

    private fun setupDefaultCategoriesAndCurrencies(email: String) {
        val categories = mapOf(
            "Housing" to "Housing",
            "Transportation" to "Transportation",
            "Food" to "Food",
            "Entertainment" to "Entertainment",
            "Other" to "Other"
        )
        db.collection(email).document("Categories").set(categories)
            .addOnSuccessListener {
                Log.d(TAG, "Default categories created successfully!")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error creating default categories", e)
            }
        val currencies = mapOf(
            "active" to "EUR:€",
            "USD" to "USD:$",
            "JPY" to "JPY:¥",
            "GBP" to "GBP:£",
            "INR" to "INR:₹"
        )
        db.collection(email).document("Currencies").set(currencies)
            .addOnSuccessListener {
                Log.d(TAG, "Default currencies created successfully!")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error creating default categories", e)
            }

        val plan = mapOf("premium" to false)
        db.collection(email).document("Plan").set(plan)
            .addOnSuccessListener {
                Log.d(TAG, "Default plan created successfully!")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error creating default plan", e)
            }
    }

    private fun updateExpenses() {
        totalExpense = findViewById(R.id.total_expense)
        category1 = findViewById(R.id.category_1)
        category2 = findViewById(R.id.category_2)
        category3 = findViewById(R.id.category_3)
        value1 = findViewById(R.id.value_1)
        value2 = findViewById(R.id.value_2)
        value3 = findViewById(R.id.value_3)

        email?.let {
            fetchCurrency(it) { currency ->
                fetchCategories(it) { categories ->
                    fetchExpenses(it, categories) { monthlyExpense, sortedCategories ->
                        updateUI(monthlyExpense, currency, sortedCategories)
                    }
                }
            }
        }
    }

    private fun fetchCurrency(email: String, callback: (String) -> Unit) {
        var docRef = db.collection(email).document("Currencies")
        docRef.get().addOnSuccessListener { document ->
            var currency = document?.getString("active")?.split(":")?.get(1) ?: ""
            if (currency.isEmpty()) {
                setupDefaultCategoriesAndCurrencies(email)
                docRef = db.collection(email).document("Currencies")
                docRef.get().addOnSuccessListener { document2 ->
                    currency = document2?.getString("active")?.split(":")?.get(1) ?: ""
                    if (currency.isEmpty()) {
                        setupDefaultCategoriesAndCurrencies(email)
                    }
                    callback(currency)
                }.addOnFailureListener {
                    setupDefaultCategoriesAndCurrencies(email)
                    callback("")
                }
            }
            callback(currency)
        }.addOnFailureListener {
            setupDefaultCategoriesAndCurrencies(email)
            callback("")
        }
    }

    private fun fetchCategories(email: String, callback: (MutableMap<String, Double>) -> Unit) {
        val docRef = db.collection(email).document("Categories")
        docRef.get().addOnSuccessListener { document ->
            val categories = document?.data?.mapValues { 0.0 }?.toMutableMap() ?: mutableMapOf()
            callback(categories)
        }.addOnFailureListener {
            callback(mutableMapOf())
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun fetchExpenses(
        email: String,
        categories: MutableMap<String, Double>,
        callback: (Double, Map<String, Double>) -> Unit
    ) {
        val calendar = Calendar.getInstance()
        val currentMonth = SimpleDateFormat("MMM").format(calendar.time)
        val currentYear = calendar.get(Calendar.YEAR).toString()
        var monthlyExpense = 0.0

        db.collection(email).get().addOnSuccessListener { result ->
            for (document in result) {
                val date = document.getString("date") ?: continue
                if (date.contains(currentMonth) && date.contains(currentYear)) {
                    val amount = document.getDouble("amount") ?: continue
                    monthlyExpense += amount
                    val category = document.getString("category") ?: "Other"
                    categories[category] = categories.getOrDefault(category, 0.0) + "%.2f".format(amount).toDouble()
                }
            }
            val sortedCategories = categories.toList().sortedByDescending { it.second }.toMap()
            callback(monthlyExpense, sortedCategories)
        }.addOnFailureListener {
            callback(0.0, emptyMap())
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(monthlyExpense: Double, currency: String, sortedCategories: Map<String, Double>) {
        val topCategories = sortedCategories.entries.take(3)
        if (topCategories.isNotEmpty()) {
            category1.text = "• ${topCategories[0].key}"
            value1.text = "%.2f".format(topCategories[0].value) + currency
            if (topCategories.size >= 2) {
                category2.text = "• ${topCategories[1].key}"
                value2.text = "%.2f".format(topCategories[1].value) + currency
            }
            if (topCategories.size == 3) {
                category3.text = "• ${topCategories[2].key}"
                value3.text = "%.2f".format(topCategories[2].value) + currency
            }
        }
        totalExpense.text = "%.2f".format(monthlyExpense) + " $currency"
    }


    private fun fadeInView(view: View) {
        view.alpha = 0f
        view.animate()
            .alpha(1f)
            .setDuration(1000)
            .setListener(null)
    }

    private fun slideInFromBottom(view: View) {
        view.translationY = 1000f
        view.animate()
            .translationY(0f)
            .setDuration(1000)
            .setListener(null)
    }

    private fun animateViewsTogether(vararg views: View) {
        val animatorSet = AnimatorSet()
        val animators = views.map { view ->
            ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).apply {
                duration = 1000
            }
        }
        animatorSet.playTogether(animators)
        animatorSet.start()
    }

    private fun animateViewsOnLoad() {
        fadeInView(totalExpense)
        slideInFromBottom(btn_add_expense)
        slideInFromBottom(btn_view_all_expenses)
        slideInFromBottom(btn_currency_converter)
        slideInFromBottom(btn_profile)

        animateViewsTogether(category1, category2, category3, value1, value2, value3)
    }
}