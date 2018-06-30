package com.raywenderlich.android.w00tze.repository

import com.raywenderlich.android.w00tze.model.Gist
import com.raywenderlich.android.w00tze.model.Repo
import java.io.*
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * Created by MiguelAngel on 28/6/18.
 */

@Throws(IOException::class)
internal fun getUrlAsString(urlAddress: String): String {
    val url = URL(urlAddress)
    val conn = url.openConnection() as HttpsURLConnection

    conn.requestMethod = "GET"
    conn.setRequestProperty("Accept", "application/json")

    return try {
        val inputStream = conn.inputStream

        if (conn.responseCode != HttpsURLConnection.HTTP_OK) {
            throw IOException("${conn.responseMessage} for $urlAddress")
        }

        if (inputStream != null) {
            convertStreamToString(inputStream)
        } else {
            "Error retrieving $urlAddress"
        }
    } finally {
        conn.disconnect()
    }
}

@Throws(IOException::class)
private fun convertStreamToString(inputStream: InputStream): String {
    val reader = BufferedReader(InputStreamReader(inputStream))
    val sb = StringBuilder()
    var line: String? = reader.readLine()
    while (line != null) {
        sb.append(line).append("\n")
        line = reader.readLine()
    }
    reader.close()
    return sb.toString()
}


