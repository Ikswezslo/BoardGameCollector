package com.example.boardgamecollector.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL


@Suppress("DEPRECATION")
class AsyncImageDownloader(
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
        try {
            val url = URL(url)
            val connection: HttpURLConnection = url
                .openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input: InputStream = connection.inputStream
            val bitmap = BitmapFactory.decodeStream(input)
            val dir = File(dir)
            if (!dir.exists()) dir.mkdirs()
            val fOut = FileOutputStream("$dir/$filename.png")
            bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut)
            fOut.flush()
            fOut.close()

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