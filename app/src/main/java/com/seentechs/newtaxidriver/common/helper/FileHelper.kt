package com.seentechs.newtaxidriver.common.helper

import android.text.TextUtils
import android.util.Log
import java.io.*

/**
 * Created by Tan on 2/18/2016.
 */
object FileHelper {
    internal val fileName = "data.txt"
    internal val path = ""
    internal val TAG = FileHelper::class.java.name

    fun ReadFile(): String? {
        var line: String = ""

        try {
            val fileInputStream = FileInputStream(File(path + fileName))
            val inputStreamReader = InputStreamReader(fileInputStream)
            val bufferedReader = BufferedReader(inputStreamReader)
            val stringBuilder = StringBuilder()
            line = bufferedReader.readLine()
            if (line != null) {
                stringBuilder.append(line + System.getProperty("line.separator")!!)
            }
            fileInputStream.close()
            line = stringBuilder.toString()

            bufferedReader.close()
        } catch (ex: FileNotFoundException) {
            Log.d(TAG, ex.message.toString())
        } catch (ex: IOException) {
            Log.d(TAG, ex.message.toString())
        }

        return line
    }

    fun saveToFile(data: String, fileNames: String): Boolean {
        try {
            File(path).mkdir()

            val file: File
            if (TextUtils.isEmpty(fileNames))
                file = File(path + fileName)
            else
                file = File("$path$fileNames.txt")

            if (!file.exists()) {
                file.createNewFile()
            }
            val fileOutputStream = FileOutputStream(file, true)
            fileOutputStream.write((data + System.getProperty("line.separator")!!).toByteArray())

            return true
        } catch (ex: FileNotFoundException) {
            Log.d(TAG, ex.message.toString())
        } catch (ex: IOException) {
            Log.d(TAG, ex.message.toString())
        }

        return false


    }

}