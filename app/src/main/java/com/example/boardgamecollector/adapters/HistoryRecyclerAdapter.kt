package com.example.boardgamecollector.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.boardgamecollector.R
import com.example.boardgamecollector.datasets.History
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class HistoryRecyclerAdapter(private val dataSet: ArrayList<History>) : RecyclerView.Adapter<HistoryRecyclerAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val timestamp: TextView = view.findViewById(R.id.history_timestamp)
        val ranking: TextView = view.findViewById(R.id.history_ranking)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.text_row_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val timestamp = dataSet[position].timestamp
        if(timestamp != null) {
            val date = Date(timestamp.time)
            val formattedDate: String = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ENGLISH).format(date)
            holder.timestamp.text = formattedDate
        }
        holder.ranking.text = dataSet[position].ranking.toString()
    }

    override fun getItemCount() = dataSet.size
}