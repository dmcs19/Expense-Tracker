package com.example.expensetracker

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CurrencyConverter : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var etAmount: EditText
    private lateinit var spinnerFromCurrency: Spinner
    private lateinit var spinnerToCurrency: Spinner
    private lateinit var btnConvert: Button
    private lateinit var tvResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_currency_converter)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        clickListeners()

        setCurrency()
    }

    @SuppressLint("SetTextI18n")
    private fun clickListeners(){
        imageView = findViewById(R.id.btn_back)
        etAmount = findViewById(R.id.et_amount)
        btnConvert = findViewById(R.id.btn_convert)
        tvResult = findViewById(R.id.tv_result)

        // Go back to the MainActivity
        imageView.setOnClickListener {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Convert the currency
        btnConvert.setOnClickListener {
            val amount = etAmount.text.toString().toDoubleOrNull()
            if (amount != null) {
                val fromCurrency = spinnerFromCurrency.selectedItem.toString()
                val toCurrency = spinnerToCurrency.selectedItem.toString()
                val convertedAmount = convertCurrency(amount, fromCurrency, toCurrency)
                tvResult.text = String.format("%.2f %s", convertedAmount, toCurrency)
            } else {
                tvResult.text = "Please enter a valid amount"
            }
        }
    }

    private fun setCurrency(){
        spinnerFromCurrency = findViewById(R.id.spinner_from_currency)
        spinnerToCurrency = findViewById(R.id.spinner_to_currency)

        // Set up the spinners with currency options
        val currencies = arrayOf("USD", "EUR", "GBP", "JPY", "INR")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFromCurrency.adapter = adapter
        spinnerToCurrency.adapter = adapter
    }

    private fun convertCurrency(amount: Double, fromCurrency: String, toCurrency: String): Double {
        val conversionRates = mapOf(
            "USD" to mapOf("USD" to 1.0, "EUR" to 0.85, "GBP" to 0.75, "JPY" to 110.0, "INR" to 74.0),
            "EUR" to mapOf("USD" to 1.18, "EUR" to 1.0, "GBP" to 0.88, "JPY" to 129.0, "INR" to 87.0),
            "GBP" to mapOf("USD" to 1.33, "EUR" to 1.14, "GBP" to 1.0, "JPY" to 146.0, "INR" to 99.0),
            "JPY" to mapOf("USD" to 0.0091, "EUR" to 0.0078, "GBP" to 0.0068, "JPY" to 1.0, "INR" to 0.68),
            "INR" to mapOf("USD" to 0.013, "EUR" to 0.011, "GBP" to 0.010, "JPY" to 1.47, "INR" to 1.0)
        )
        return amount * (conversionRates[fromCurrency]?.get(toCurrency) ?: 1.0)
    }
}
