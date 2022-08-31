package com.example.boardgamecollector.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.boardgamecollector.utils.DBHandler
import com.example.boardgamecollector.R
import com.example.boardgamecollector.adapters.HistoryRecyclerAdapter

class GameHistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_history)

        val extras = intent.extras ?: return
        val id = extras.getLong("id")
        val title = extras.getString("title")
        val titleView: TextView = findViewById(R.id.history_title)
        titleView.text = title

        val dbHandler = DBHandler(this, null, null, 1)
        val recyclerView: RecyclerView = findViewById(R.id.history_recyclerView)
        recyclerView.adapter = HistoryRecyclerAdapter(dbHandler.getGameHistory(id))
        recyclerView.layoutManager = LinearLayoutManager(this)

    }
}