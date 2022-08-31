package com.example.boardgamecollector.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.boardgamecollector.utils.DBHandler
import com.example.boardgamecollector.adapters.ExpansionsRecyclerAdapter
import com.example.boardgamecollector.adapters.GamesRecyclerAdapter
import com.example.boardgamecollector.R

class GamesListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var sortByRankingBtn: Button
    private var sortingType: String = "title ASC"
    private var isGame: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_games_list)

        recyclerView = findViewById(R.id.recyclerView)
        sortByRankingBtn = findViewById(R.id.sort_by_ranking)
        val header: TextView = findViewById(R.id.header)
        val extras = intent.extras ?: return
        val type = extras.getString("Type")
        if(type == "game") {
            isGame = 1
            header.text = "Lista gier"
        } else if(type == "expansion") {
            isGame = 0
            sortByRankingBtn.isEnabled = false
            sortByRankingBtn.alpha = 0.0f
            sortByRankingBtn.width = 5
            sortByRankingBtn.layoutParams.width = 80
            header.text = "Lista dodatków"
        }
        setRecyclerAdapter(sortingType)
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setRecyclerAdapter(sortBy: String) {
        val dbHandler = DBHandler(this, null, null, 1)
        if(isGame == 1) {
            val adapter = GamesRecyclerAdapter(dbHandler.getGamesArray(1, sortBy)) {
                val intent = Intent(this, GameHistoryActivity::class.java)
                intent.putExtra("id", it.id)
                intent.putExtra("title", it.title)
                startActivity(intent)
            }
            recyclerView.adapter = adapter
        } else if(isGame == 0) {
            val adapter = ExpansionsRecyclerAdapter(dbHandler.getGamesArray(0, sortBy))
            recyclerView.adapter = adapter
        }
    }
    fun sortByTitle(v: View) {
        if(sortingType == "title ASC") {
            sortingType = "title DESC"
        } else {
            sortingType = "title ASC"
        }
        setRecyclerAdapter(sortingType)
    }

    fun sortByRanking(v: View) {
        if(sortingType == "ranking ASC") {
            sortingType = "ranking DESC"
        } else {
            sortingType = "ranking ASC"
        }
        setRecyclerAdapter(sortingType)
    }

    fun sortByYear(v: View) {
        if(sortingType == "year ASC") {
            sortingType = "year DESC"
        } else {
            sortingType = "year ASC"
        }
        setRecyclerAdapter(sortingType)
    }

}