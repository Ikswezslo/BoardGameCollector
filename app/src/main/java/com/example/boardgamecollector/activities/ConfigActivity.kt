package com.example.boardgamecollector.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import com.example.boardgamecollector.R

class ConfigActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)
    }

    fun onConfirmButtonClicked(v: View) {
        val sharedPref = getSharedPreferences("myPref", MODE_PRIVATE)
        val editText:EditText = findViewById(R.id.editText)
        sharedPref.edit().putString("user_id", editText.text.toString()).apply()
        val intent = Intent(this, SynchronizationActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        intent.putExtra("First_Synchronization", true)
        startActivity(intent)
        finish()
    }
}