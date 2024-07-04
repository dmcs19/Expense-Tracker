package com.example.expensetracker

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class Settings : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var changePassword: Button
    private lateinit var deleteAccount: Button
    private lateinit var textView: TextView
    private lateinit var spinnerCurrency: Spinner
    private lateinit var btnSave: Button

    private lateinit var auth: FirebaseAuth
    private var user: FirebaseUser? = null
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser

        if (user == null) {
            val intent = Intent(applicationContext, Login::class.java)
            startActivity(intent)
            finish()
            return
        }

        textView = findViewById(R.id.textView)
        imageView = findViewById(R.id.btn_back)
        changePassword = findViewById(R.id.btn_change_password)
        deleteAccount = findViewById(R.id.btn_delete_account)
        spinnerCurrency = findViewById(R.id.spinner_currency)
        btnSave = findViewById(R.id.btn_save)

        textView.text = user!!.email

        setupSpinner()
        clickListeners()
    }

    private fun setupSpinner() {
        val currencies = listOf("USD:$", "EUR:€", "GBP:£", "JPY:¥", "INR:₹")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCurrency.adapter = adapter
    }

    private fun clickListeners() {
        // Go back to the profile activity
        imageView.setOnClickListener {
            val intent = Intent(applicationContext, Profile::class.java)
            startActivity(intent);
            finish()
        }

        changePassword.setOnClickListener {
            FirebaseAuth.getInstance().sendPasswordResetEmail(user!!.email.toString())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            baseContext,
                            "An email was sent to you to change your password",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.d(TAG, "Email sent.")
                    }
                }
        }

        deleteAccount.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        btnSave.setOnClickListener {
            val selectedCurrency = spinnerCurrency.selectedItem.toString().split(":")[0]
            convertExpensesAndSetActiveCurrency(selectedCurrency)
            val intent = Intent(applicationContext, Profile::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Confirm Deletion")
            .setMessage("Are you sure you want to delete your account?")
            .setPositiveButton("Yes") { dialog, _ ->
                showReauthenticationDialog()
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    private fun showReauthenticationDialog() {
        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email ?: return
        val reauthenticateView = layoutInflater.inflate(R.layout.dialog_reauthenticate, null)
        val passwordInput = reauthenticateView.findViewById<EditText>(R.id.password)

        AlertDialog.Builder(this)
            .setTitle("Re-authentication Required")
            .setMessage("Please enter your password to re-authenticate.")
            .setView(reauthenticateView)
            .setPositiveButton("Confirm") { dialog, _ ->
                val password = passwordInput.text.toString()
                val credential = EmailAuthProvider.getCredential(email, password)
                user.reauthenticate(credential)
                    .addOnCompleteListener { reauthTask ->
                        if (reauthTask.isSuccessful) {
                            deleteAccount()
                        } else {
                            Log.e(TAG, "Re-authentication failed.", reauthTask.exception)
                            Toast.makeText(
                                this,
                                "Re-authentication failed. Please try again.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        dialog.dismiss()
                    }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    private fun deleteAccount() {
        user?.delete()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val intent = Intent(applicationContext, Login::class.java)
                    startActivity(intent)
                    finish()
                    Log.d(TAG, "User account deleted.")
                } else {
                    Log.e(TAG, "Account deletion failed.", task.exception)
                    Toast.makeText(
                        this,
                        "Account deletion failed. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun convertCurrency(amount: Double, fromCurrency: String, toCurrency: String): Double {
        val conversionRates = mapOf(
            "USD" to mapOf(
                "USD" to 1.0,
                "EUR" to 0.85,
                "GBP" to 0.75,
                "JPY" to 110.0,
                "INR" to 74.0
            ),
            "EUR" to mapOf(
                "USD" to 1.18,
                "EUR" to 1.0,
                "GBP" to 0.88,
                "JPY" to 129.0,
                "INR" to 87.0
            ),
            "GBP" to mapOf(
                "USD" to 1.33,
                "EUR" to 1.14,
                "GBP" to 1.0,
                "JPY" to 146.0,
                "INR" to 99.0
            ),
            "JPY" to mapOf(
                "USD" to 0.0091,
                "EUR" to 0.0078,
                "GBP" to 0.0068,
                "JPY" to 1.0,
                "INR" to 0.68
            ),
            "INR" to mapOf(
                "USD" to 0.013,
                "EUR" to 0.011,
                "GBP" to 0.010,
                "JPY" to 1.47,
                "INR" to 1.0
            )
        )
        return amount * (conversionRates[fromCurrency]?.get(toCurrency) ?: 1.0)
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun convertExpensesAndSetActiveCurrency(selectedCurrency: String) {
        val email = user?.email ?: return
        val userCollection = db.collection(email)

        GlobalScope.launch(Dispatchers.IO) {
            try {
                // Fetch all expenses
                val expenses = userCollection.get().await().documents
                // Fetch the current active currency
                val currenciesDoc = userCollection.document("Currencies").get().await()
                val activeCurrency =
                    currenciesDoc.getString("active")?.split(":")?.get(0) ?: return@launch

                if (activeCurrency == selectedCurrency) return@launch

                val activeSymbol = getCurrencySymbol(activeCurrency)
                val selectedSymbol = getCurrencySymbol(selectedCurrency)
                val data = hashMapOf(
                    "active" to "$selectedCurrency:$selectedSymbol",
                    activeCurrency to "$activeCurrency:$activeSymbol"
                )
                val existingCurrencies = currenciesDoc.data?.toMutableMap() ?: mutableMapOf()
                existingCurrencies.remove("active")
                existingCurrencies.remove(selectedCurrency)
                existingCurrencies["active"] = "$selectedCurrency:$selectedSymbol"
                existingCurrencies[activeCurrency] = "$activeCurrency:$activeSymbol"
                userCollection.document("Currencies").set(existingCurrencies)

                // Convert each expense to the new currency
                for (expense in expenses) {
                    val amount = expense.getDouble("amount") ?: continue
                    val newAmount = convertCurrency(amount, activeCurrency, selectedCurrency)
                    expense.reference.update("amount", "%.2f".format(newAmount).toDouble()).await()
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@Settings,
                        "Currency updated successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating currency", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@Settings, "Error updating currency", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun getCurrencySymbol(currencyCode: String): String {
        return when (currencyCode) {
            "USD" -> "$"
            "EUR" -> "€"
            "GBP" -> "£"
            "JPY" -> "¥"
            "INR" -> "₹"
            else -> ""
        }
    }

}
