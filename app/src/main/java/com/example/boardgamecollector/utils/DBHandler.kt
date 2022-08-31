package com.example.boardgamecollector.utils

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.boardgamecollector.datasets.Game
import com.example.boardgamecollector.datasets.History
import java.sql.Timestamp

class DBHandler (context: Context, name: String?,
                 factory: SQLiteDatabase.CursorFactory?, version: Int) : SQLiteOpenHelper(context,
DATABASE_NAME, factory, DATABASE_VERSION
){
    companion object {
        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "bgcDB.db"
        val TABLE_COLLECTION = "collection"
        val TABLE_HISTORY = "history"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createCollectionQuery = "CREATE TABLE $TABLE_COLLECTION (id INTEGER PRIMARY KEY, title TEXT, ranking INTEGER, picture TEXT, year TEXT, is_game INTEGER)"
        db.execSQL(createCollectionQuery)

        val createHistoryQuery = "CREATE TABLE $TABLE_HISTORY (id INTEGER, ts INTEGER, ranking INTEGER)"
        db.execSQL(createHistoryQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_COLLECTION")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_HISTORY")
        onCreate(db)
    }

    fun addGame(game: Game) {
        val values = ContentValues()
        values.put("id", game.id)
        values.put("ranking", game.ranking)
        values.put("title", game.title)
        values.put("year", game.year)
        values.put("picture", game.picture)
        values.put("is_game", game.isGame)
        val db = this.writableDatabase
        db.insert(TABLE_COLLECTION, null, values)
        db.close()
    }

    fun addHistory(id: Long, ts: Long, ranking: Int?) {
        var values = ContentValues()
        values.put("id", id)
        values.put("ts", ts)
        values.put("ranking", ranking)
        val db = this.writableDatabase
        db.insert(TABLE_HISTORY, null, values)
        values = ContentValues()
        values.put("ranking", ranking)
        db.update(TABLE_COLLECTION, values, "id = ?", arrayOf(id.toString()))
        db.close()
    }

    fun getIdArray(): ArrayList<Long> {
        val result = ArrayList<Long>()
        val query = "SELECT id FROM $TABLE_COLLECTION"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        try {
            while (cursor.moveToNext()) {
                result.add(cursor.getString(0).toLong())
            }
        } finally {
            cursor.close()
        }
        db.close()
        return result
    }

    fun getGamesNumber() : Int {
        val query = "SELECT COUNT(*) FROM $TABLE_COLLECTION  WHERE is_game = 1"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        cursor.moveToFirst()
        val result = cursor.getInt(0)
        cursor.close()
        db.close()
        return result
    }

    fun getExpansionNumber() : Int {
        val query = "SELECT COUNT(*) FROM $TABLE_COLLECTION  WHERE is_game = 0"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        cursor.moveToFirst()
        val result = cursor.getInt(0)
        cursor.close()
        db.close()
        return result
    }

    fun clearDatabase() {
        val db = this.writableDatabase
        db.execSQL("DROP TABLE IF EXISTS $TABLE_COLLECTION")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_HISTORY")
        onCreate(db)
        db.close()
    }

    fun getGamesArray(is_game: Int, sortBy: String): ArrayList<Game> {
        val gamesArray = ArrayList<Game>()

        val query = "SELECT * FROM $TABLE_COLLECTION WHERE is_game = $is_game ORDER BY $sortBy"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)

        var game: Game

        try {
            while (cursor.moveToNext()) {
                game = Game(cursor.getLong(0))
                game.title = cursor.getString(1)
                game.ranking = cursor.getInt(2)
                game.picture = cursor.getString(3)
                game.year = cursor.getString(4)
                gamesArray.add(game)
            }
        } finally {
            cursor.close()
        }
        db.close()

        return gamesArray
    }

    fun getGameHistory(id: Long): ArrayList<History> {
        val historyArray = ArrayList<History>()

        val query = "SELECT ts, ranking FROM $TABLE_HISTORY WHERE id = $id ORDER BY ts DESC"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)

        var history: History

        try {
            while (cursor.moveToNext()) {
                history = History(Timestamp(cursor.getLong(0)), cursor.getInt(1))
                historyArray.add(history)
            }
        } finally {
            cursor.close()
        }
        db.close()

        return historyArray
    }


    fun getNewestHistoryTimestamp(): Long {
        val query = "SELECT MAX(ts) FROM $TABLE_HISTORY"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        cursor.moveToFirst()
        val result = cursor.getLong(0)
        cursor.close()
        db.close()
        return result
    }

    fun deleteGamesByID(idArray: ArrayList<Long>) {
        val db = this.writableDatabase
        for(id in idArray) {
            db.delete(TABLE_COLLECTION, "id" + " = ?", arrayOf(id.toString()))
            db.delete(TABLE_HISTORY, "id" + " = ?", arrayOf(id.toString()))
        }
        db.close()
    }
}