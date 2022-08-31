package com.example.boardgamecollector.activities

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.example.boardgamecollector.utils.AsyncXMLDownloader
import com.example.boardgamecollector.utils.DBHandler
import com.example.boardgamecollector.R
import com.example.boardgamecollector.utils.AsyncImageDownloader
import com.example.boardgamecollector.utils.AsyncXMLProcessor
import org.w3c.dom.Document
import java.io.File
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.collections.ArrayList

class SynchronizationActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var lastSyncTextView: TextView
    private lateinit var progressCircle: ProgressBar
    private lateinit var progressBar: ProgressBar
    private lateinit var username: String
    private lateinit var nextButton: Button
    private lateinit var syncButton: Button
    private var actualProgress: Int = 0
    private var isFirstSynchronization: Boolean = false
    private var lastSync: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_synchronization)
        statusText = findViewById(R.id.status)
        statusText.text = ""
        progressCircle = findViewById(R.id.progressCircle)
        progressBar = findViewById(R.id.progressBar)
        nextButton = findViewById(R.id.exit)
        syncButton = findViewById(R.id.syncBtn)
        lastSyncTextView = findViewById(R.id.lastSync)

        val sharedPref = getSharedPreferences("myPref", MODE_PRIVATE)
        username = sharedPref.getString("user_id", "none").orEmpty()
        val extras = intent.extras ?: return
        isFirstSynchronization = extras.getBoolean("First_Synchronization")
        if(isFirstSynchronization) {
            nextButton.isEnabled = false
        }
        else{
            nextButton.alpha = 0.0f
            nextButton.isClickable = false
        }
        val dbHandler = DBHandler(this, null, null, 1)
        lastSync = dbHandler.getNewestHistoryTimestamp()
        if(lastSync == 0L) {
            lastSyncTextView.text = "Ostatnia synchronizacja: brak"
        }
        else {
            val timestamp = Timestamp(lastSync)
            val date = Date(timestamp.time)
            val formattedDate: String = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ENGLISH).format(date)
            lastSyncTextView.text = "Ostatnia synchronizacja: $formattedDate"
        }

    }

    fun onSynchronizeButtonClicked(v: View) {
        syncButton.isClickable = false
        val diff = System.currentTimeMillis() - lastSync
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        if(hours < 24) {
            val alertDialog: AlertDialog? = this.let {
                val builder = AlertDialog.Builder(it)
                builder.apply {
                    setPositiveButton("Tak",
                        DialogInterface.OnClickListener { _, _ ->
                            startDownloading()
                        })
                    setNegativeButton("Nie",
                        DialogInterface.OnClickListener { _, _ ->
                            syncButton.isClickable = true
                        })
                }
                builder.setMessage("Ostatnia synchronizacja była mniej niż 24 godziny temu. Czy na pewno chcesz kontynuować?")
                builder.setTitle("Synchronizacja")
                builder.create()
            }
            alertDialog?.show()
        }
        else {
            startDownloading()
        }
    }

    private fun startDownloading() {
        syncButton.isEnabled = false
        syncButton.isClickable = true
        progressCircle.alpha = 1.0F
        statusText.text = "Sprawdzam gry"
        asyncDownloadGames(0)
    }

    private fun asyncDownloadGames(sleepTime: Long) {
        AsyncXMLDownloader(
            sleepTime,
            "https://boardgamegeek.com/xmlapi2/collection?username=$username&stats=1",
            "$filesDir/XML",
            "games.xml") { result ->
            val isCorrect = (result == "success" && isCorrectXML("games.xml"))
            if(isCorrect) {
                statusText.text = "Sprawdzam dodatki"
                asyncDownloadExpansions(0)
            } else {
                asyncDownloadGames(sleepTime + 10000)
            }
        }.execute()
    }

    private fun asyncDownloadExpansions(sleepTime: Long) {
        AsyncXMLDownloader(
            sleepTime,
            "https://boardgamegeek.com/xmlapi2/collection?username=$username&stats=1&subtype=boardgameexpansion",
            "$filesDir/XML",
            "expansions.xml") { result ->
            val isCorrect = (result == "success" && isCorrectXML("expansions.xml"))
            if(isCorrect) {
                statusText.text = "Przetwarzam"
                asyncProcessXMLFiles()
            } else {
                asyncDownloadExpansions(sleepTime + 10000)
            }
        }.execute()
    }

    private fun asyncProcessXMLFiles() {
        val dbHandler = DBHandler(this, null, null, 1)
        AsyncXMLProcessor(filesDir, this) { idArray, linkArray, toRemoveIdArray ->
            if(toRemoveIdArray.size > 0) {
                val alertDialog: AlertDialog? = this.let {
                    val builder = AlertDialog.Builder(it)
                    builder.apply {
                        setPositiveButton("Tak",
                            DialogInterface.OnClickListener { _, _ ->
                                dbHandler.deleteGamesByID(toRemoveIdArray)
                            })
                        setNegativeButton("Nie",
                            DialogInterface.OnClickListener { _, _ -> })
                    }
                    builder.setMessage("Niektóre gry zostały usunięte z konta. Czy chcesz je usunąć z aplikacji?")
                    builder.setTitle("Usunięte gry")
                    builder.create()
                }
                alertDialog?.show()
            }
            progressCircle.alpha = 0.0f
            if (idArray.size > 0) {
                progressBar.max = idArray.size
                progressBar.progress = 0
                progressBar.alpha = 1.0f
                statusText.text = "Pobieram zdjęcia"
            }
            asyncDownloadPictures(idArray, linkArray)
        }.execute()
    }

    private fun asyncDownloadPictures(idArray: ArrayList<Long>, linkArray: ArrayList<String?>) {
        if(idArray.isEmpty()) {
            finishSynchronization()
        } else {
            actualProgress += 1
            progressBar.progress = actualProgress
            val id = idArray.removeFirst()
            val link = linkArray.removeFirst()
            if(link != null) {
                AsyncImageDownloader(
                    link,
                    "$filesDir/images",
                    id.toString()) {
                    asyncDownloadPictures(idArray, linkArray)
                }.execute()
            }
            else {
                asyncDownloadPictures(idArray, linkArray)
            }
        }
    }

    private fun finishSynchronization() {
        val dbHandler = DBHandler(this, null, null, 1)
        statusText.text = ""
        nextButton.isEnabled = true
        progressBar.alpha = 0.0f
        lastSync = dbHandler.getNewestHistoryTimestamp()
        if(lastSync == 0L) {
            lastSyncTextView.text = "Ostatnia synchronizacja: brak"
        }
        else {
            val timestamp = Timestamp(lastSync)
            val date = Date(timestamp.time)
            val formattedDate: String =
                SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ENGLISH).format(date)
            lastSyncTextView.text = "Ostatnia synchronizacja: $formattedDate"
        }
        syncButton.isEnabled = true
    }

    private fun isCorrectXML(filename: String): Boolean {
        val path = filesDir
        val inDir = File(path, "XML")
        if (inDir.exists()) {
            val file = File(inDir, filename)
            if (file.exists()) {
                val xmlDoc: Document =
                    DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
                xmlDoc.documentElement.normalize()
                val tag = xmlDoc.documentElement.tagName
                if(tag == "items") {
                    return true
                }
            }
        }
        return false
    }

    fun onNextButtonClicked(v: View) {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        if(statusText.text == "") {
            super.onBackPressed()
        } else {
            Toast.makeText(this, "Poczekaj na zakończenie synchronizacji", Toast.LENGTH_SHORT).show()
        }
    }
}