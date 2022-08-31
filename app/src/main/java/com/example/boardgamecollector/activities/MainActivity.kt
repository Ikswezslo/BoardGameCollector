package com.example.boardgamecollector.activities

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.boardgamecollector.utils.DBHandler
import com.example.boardgamecollector.R
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val sharedPref = getSharedPreferences("myPref", MODE_PRIVATE)
        val userName = sharedPref.getString("user_id", "none")
        if(userName == "none") {
            val intent = Intent(this, ConfigActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun updateUI() {
        val dbHandler = DBHandler(this, null, null, 1)
        val sharedPref = getSharedPreferences("myPref", MODE_PRIVATE);
        val userName = sharedPref.getString("user_id", "none")
        val user:TextView = findViewById(R.id.user)
        user.text = "Użytkownik: $userName"
        val gamesNum:TextView = findViewById(R.id.gamesNum)
        gamesNum.text = "Liczba gier: ${dbHandler.getGamesNumber()}"
        val expansionNum:TextView = findViewById(R.id.expansionNum)
        expansionNum.text = "Liczba dodatków: ${dbHandler.getExpansionNumber()}"
        val timestampTextView: TextView = findViewById(R.id.timestamp)
        val millis = dbHandler.getNewestHistoryTimestamp()
        if(millis == 0L) {
            timestampTextView.text = "Ostatnia synchronizacja: brak"
        }
        else {
            val timestamp = Timestamp(millis)
            val date = Date(timestamp.time)
            val formattedDate: String = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ENGLISH).format(date)
            timestampTextView.text = "Ostatnia synchronizacja: $formattedDate"
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    fun onSynchronizeButtonClicked(v: View) {
        val intent = Intent(this, SynchronizationActivity::class.java)
        intent.putExtra("First_Synchronization", false)
        startActivity(intent)
    }

    fun onClearDataButtonClicked(v: View) {
        val context = this
        val alertDialog: AlertDialog? = this.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setPositiveButton("Tak",
                    DialogInterface.OnClickListener { _, _ ->
                        val sharedPref = getSharedPreferences("myPref", MODE_PRIVATE)
                        sharedPref.edit().putString("user_id","none").apply()
                        val dbHandler = DBHandler(context, null, null, 1)
                        dbHandler.clearDatabase()
                        finish()
                    })
                setNegativeButton("Nie",
                    DialogInterface.OnClickListener { _, _ -> })
            }
            builder.setMessage("Czy na pewno chcesz usunąć wszystkie dane?")
            builder.setTitle("Usuwanie danych")
            builder.create()
        }
        alertDialog?.show()
    }

    fun onGamesListButtonClicked(v: View) {
        val intent = Intent(this, GamesListActivity::class.java)
        intent.putExtra("Type", "game")
        startActivity(intent)
    }

    fun onExpansionsListButtonClicked(v: View) {
        val intent = Intent(this, GamesListActivity::class.java)
        intent.putExtra("Type", "expansion")
        startActivity(intent)
    }
}