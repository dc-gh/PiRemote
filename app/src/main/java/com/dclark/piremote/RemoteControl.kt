package com.dclark.piremote

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object RemoteControl {

    fun sendRequest(ipAddress: String, port: String, device: String, command: String, onResult: (Boolean) -> Unit) {

        try {
            val url = URL("https://$ipAddress:$port/")
            val urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.requestMethod = "POST"
            urlConnection.connectTimeout = 1000
            urlConnection.doOutput = true
            urlConnection.setRequestProperty("Content-Type", "application/json")

            // Create a JSON object
            val jsonObject = JSONObject()
            // Add key-value pairs to the JSON object
            jsonObject.put("device", device)
            jsonObject.put("commands", JSONArray().put(command))

            // Convert the JSON object to a string
            val jsonString = jsonObject.toString()

            Thread {
                try {
                    urlConnection.outputStream.use { os -> os.write(jsonString.toByteArray()) }

                    // Read the response from the server
                    val inputStream: InputStream = urlConnection.inputStream
                    val reader = BufferedReader(InputStreamReader(inputStream))

                    val result = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        result.append(line)
                    }
                    onResult(JSONObject(result.toString())["success"] as Boolean)
                } catch (_: Exception) {
                    onResult(false)
                }
            }.start()
        }
        catch (_: JSONException) {
            onResult(false)
        }
    }
}