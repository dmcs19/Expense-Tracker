package com.example.expensetracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class Profile : AppCompatActivity() {
    private lateinit var categories: ConstraintLayout
    private lateinit var settings: ConstraintLayout
    private lateinit var about: ConstraintLayout
    private lateinit var imageView: ImageView
    private lateinit var button: Button
    private lateinit var textView: TextView
    private lateinit var auth: FirebaseAuth
    private var user: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser
        textView = findViewById(R.id.textView)
        if(user == null){
            val intent = Intent(applicationContext, Login::class.java)
            startActivity(intent)
            finish()
        }

        clickListeners()

        // Set the text to the user email
        textView.text = user!!.email
    }

    private fun clickListeners(){
        categories = findViewById(R.id.categories)
        settings = findViewById(R.id.settings)
        about = findViewById(R.id.about)
        button = findViewById(R.id.btn_logout)
        imageView = findViewById(R.id.btn_back)

        // Go back to the MainActivity
        imageView.setOnClickListener{
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
        }

        // Go to the Categories activity
        categories.setOnClickListener{
            val intent = Intent(applicationContext, Categories::class.java)
            startActivity(intent)
        }

        // Go to the Settings activity
        settings.setOnClickListener{
            val intent = Intent(applicationContext, Settings::class.java)
            startActivity(intent)
        }

        // Go to the About activity
        about.setOnClickListener{
            val intent = Intent(applicationContext, About::class.java)
            startActivity(intent)
        }

        // Logout
        button.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(applicationContext, Login::class.java)
            startActivity(intent)
            finish()
        }
    }
}