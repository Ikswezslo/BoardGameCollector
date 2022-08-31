package com.example.boardgamecollector.utils

import android.os.AsyncTask
import android.util.Log
import java.io.*
import java.net.MalformedURLException
import java.net.URL

@Suppress("DEPRECATION")
class AsyncXMLDownloader(
    private var preSleep: Long,
    private var url: String,
    private var dir: String,
    private var filename: String,
    private var afterExecuteFunc: (String?) -> Unit
) : AsyncTask<String, Int, String>() {

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        afterExecuteFunc(result)
    }

    override fun doInBackground(vararg p0: String?): String {
        if(preSleep > 0) {
            Thread.sleep(preSleep)
        }
        try {
            val url = URL(url)
            val connection = url.openConnection()
            connection.connectTimeout = 10000
            connection.connect()
            val isStream = BufferedInputStream(url.openStream(), 8192)
            val testDirectory = File(dir)
            if(!testDirectory.exists()) testDirectory.mkdir()
            val fos = FileOutputStream("$testDirectory/$filename")
            val data = ByteArray(1024)
            var count: Int
            count = isStream.read(data)
            while(count != -1) {
                fos.write(data, 0, count)
                count = isStream.read(data)
            }
            isStream.close()
            fos.flush()
            fos.close()
        } catch (e: MalformedURLException) {
            return "Zły URL"
        } catch (e: FileNotFoundException) {
            return "Brak pliku"
        } catch (e: IOException) {
            return "Wyjątek IO"
        }
        return "success"
    }
}