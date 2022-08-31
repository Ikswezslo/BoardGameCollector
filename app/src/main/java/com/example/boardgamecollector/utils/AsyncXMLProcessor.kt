package com.example.boardgamecollector.utils

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.example.boardgamecollector.datasets.Game
import org.w3c.dom.Document
import org.w3c.dom.NodeList
import java.io.*
import javax.xml.parsers.DocumentBuilderFactory

@Suppress("DEPRECATION")
class AsyncXMLProcessor(
    private var filesDir: File,
    private var context: Context,
    private var exitFunc: (idArray: ArrayList<Long>,
                           linkArray: ArrayList<String?>,
                           toRemoveIdArray: ArrayList<Long>) -> Unit
) : AsyncTask<String, Int, String>() {

    private val idArray = ArrayList<Long>()
    private val linkArray = ArrayList<String?>()
    private var toRemoveIdArray = ArrayList<Long>()

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        exitFunc(idArray, linkArray, toRemoveIdArray)
    }

    override fun doInBackground(vararg p0: String?): String {
        val timestamp: Long = System.currentTimeMillis()
        val dbHandler = DBHandler(context, null, null, 1)
        toRemoveIdArray = dbHandler.getIdArray()
        processXML("expansions.xml", false, timestamp, idArray, linkArray, toRemoveIdArray)
        processXML("games.xml", true, timestamp, idArray, linkArray, toRemoveIdArray)
        return "success"
    }

    private fun processXML(filename: String, isGame: Boolean, timestamp: Long,
                           idArray: ArrayList<Long>, linkArray: ArrayList<String?>, toRemoveIdArray: ArrayList<Long>) {
        val path = filesDir
        val inDir = File(path, "XML")
        val dbHandler = DBHandler(context, null, null, 1)
        val currentGames = dbHandler.getIdArray()

        if (inDir.exists()) {
            val file = File(inDir, filename)
            if (file.exists()) {
                val xmlDoc: Document =
                    DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
                xmlDoc.documentElement.normalize()
                val items: NodeList = xmlDoc.documentElement.getElementsByTagName("item")
                val rankItems = xmlDoc.documentElement.getElementsByTagName("ranks")
                for (i in 0 until items.length) {
                    val game = Game(items.item(i).attributes.getNamedItem("objectid").nodeValue.toLong())
                    game.isGame = isGame
                    if (!idArray.contains(game.id)) {
                        var children: NodeList = rankItems.item(i).childNodes
                        for (j in 0 until children.length) {
                            val item = children.item(j)
                            if (isGame && item.nodeName == "rank" && item.attributes.getNamedItem("type").nodeValue == "subtype") {
                                val ranking = item.attributes.getNamedItem("value").nodeValue.toIntOrNull()
                                game.ranking = ranking
                                dbHandler.addHistory(game.id, timestamp, ranking)
                            }
                        }
                        children = items.item(i).childNodes
                        for (j in 0 until children.length) {
                            val item = children.item(j)
                            if (item.nodeName == "name") {
                                game.title = item.textContent
                            }
                            if (item.nodeName == "yearpublished") {
                                game.year = item.textContent
                            }
                            if (item.nodeName == "thumbnail") {
                                game.picture = item.textContent
                            }
                        }
                        if(!currentGames.contains(game.id)) {
                            idArray.add(game.id)
                            linkArray.add(game.picture)
                            dbHandler.addGame(game)
                        } else {
                            toRemoveIdArray.remove(game.id)
                        }
                    }
                }
            }
        }
    }
}