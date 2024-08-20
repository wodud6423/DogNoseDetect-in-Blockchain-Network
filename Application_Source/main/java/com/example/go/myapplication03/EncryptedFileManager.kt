package com.example.go.myapplication03
import android.content.Context
import java.io.*

class EncryptedFileManager(private val context: Context) {
    // 암호화된 문자열을 텍스트 파일로 저장
    fun saveEncryptedStringToFile(encryptedString: String, fileName: String) {
        try {
            val fileOutputStream: FileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)
            val outputStreamWriter = OutputStreamWriter(fileOutputStream)
            outputStreamWriter.write(encryptedString)
            outputStreamWriter.flush()
            outputStreamWriter.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // 텍스트 파일에서 암호화된 문자열 읽어오기
    fun readEncryptedStringFromFile(fileName: String): String? {
        try {
            val fileInputStream: FileInputStream = context.openFileInput(fileName)
            val inputStreamReader = InputStreamReader(fileInputStream)
            val bufferedReader = BufferedReader(inputStreamReader)
            val stringBuilder = StringBuilder()
            var line: String? = bufferedReader.readLine()
            while (line != null) {
                stringBuilder.append(line)
                line = bufferedReader.readLine()
            }
            bufferedReader.close()
            return stringBuilder.toString()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
}