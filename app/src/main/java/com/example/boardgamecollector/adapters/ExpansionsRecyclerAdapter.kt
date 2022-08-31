package com.example.boardgamecollector.adapters

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.boardgamecollector.datasets.Game
import com.example.boardgamecollector.R
import java.io.File


class ExpansionsRecyclerAdapter(private val dataSet: ArrayList<Game>) : RecyclerView.Adapter<ExpansionsRecyclerAdapter.ViewHolder>() {

    private lateinit var ctx: Context

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        val position: TextView = view.findViewById(R.id.position)
        val year: TextView = view.findViewById(R.id.year)
        val picture: ImageView = view.findViewById(R.id.picture)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.text_row_expansion, parent, false)
        ctx = parent.context
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.title.text = dataSet[position].title
        holder.position.text = "${position + 1}."
        if(dataSet[position].year == null) {
            holder.year.text = "brak"
        } else {
            holder.year.text = dataSet[position].year
        }
        val path = ctx.filesDir
        val inDir = File(path, "images")
        if (inDir.exists()) {
            val id = dataSet[position].id.toString()
            val file = File(inDir, "$id.png")
            if (file.exists()) {
                val myBitmap = BitmapFactory.decodeFile(file.absolutePath)
                holder.picture.setImageBitmap(myBitmap)
            } else {
                val resourceId: Int = ctx.resources.getIdentifier("@drawable/placeholder", "drawable", ctx.packageName)
                holder.picture.setImageResource(resourceId)
            }
        }
    }

    override fun getItemCount() = dataSet.size
}