package com.yarsi.javora.data.remote

import com.google.ai.client.generativeai.GenerativeModel
import com.yarsi.javora.data.QuizQuestion
import org.json.JSONArray
import org.json.JSONObject

class AiService {
    private val apiKey = "AIzaSyCCw_Fj8tMYdP1AO9bmdpvXMrjwVHo1dvk"
    
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash", // Kita coba nama model standar
        apiKey = apiKey
    )

    suspend fun generateQuestions(topic: String): List<QuizQuestion> {
        val prompt = """
            Generate exactly 10 multiple choice questions about Java topic: "$topic".
            Output MUST be a raw JSON array.
            Format: [{"question": "text", "codeSnippet": "optional/null", "options": ["A", "B", "C", "D"], "correctAnswerIndex": 0}]
            No markdown, no talk, just the JSON.
        """.trimIndent()

        return try {
            val response = generativeModel.generateContent(prompt)
            var jsonText = response.text ?: throw Exception("Empty response")
            
            val startIndex = jsonText.indexOf("[")
            val endIndex = jsonText.lastIndexOf("]")
            
            if (startIndex == -1 || endIndex == -1) throw Exception("Invalid JSON format")
            
            jsonText = jsonText.substring(startIndex, endIndex + 1)
            
            val jsonArray = JSONArray(jsonText)
            val questions = mutableListOf<QuizQuestion>()
            
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val optionsJson = obj.getJSONArray("options")
                val options = mutableListOf<String>()
                for (j in 0 until optionsJson.length()) {
                    options.add(optionsJson.getString(j))
                }
                
                val codeSnippet = if (obj.has("codeSnippet") && !obj.isNull("codeSnippet") && obj.getString("codeSnippet") != "null") {
                    obj.getString("codeSnippet")
                } else null
                
                questions.add(
                    QuizQuestion(
                        question = obj.getString("question"),
                        codeSnippet = codeSnippet,
                        options = options,
                        correctAnswerIndex = obj.getInt("correctAnswerIndex")
                    )
                )
            }
            questions
        } catch (e: Exception) {
            android.util.Log.e("AiService", "AI Error, using fallback: ${e.message}")
            getFallbackQuestions(topic)
        }
    }

    private fun getFallbackQuestions(topic: String): List<QuizQuestion> {
        return listOf(
            QuizQuestion("Apa output dari: System.out.println(10 + 20);?", null, listOf("30", "1020", "Error", "10"), 0),
            QuizQuestion("Manakah tipe data untuk angka desimal?", null, listOf("int", "String", "double", "boolean"), 2),
            QuizQuestion("Keyword untuk membuat class turunan adalah...", null, listOf("extends", "implements", "this", "super"), 0),
            QuizQuestion("Method utama dalam Java adalah...", "public static void main(String[] args)", listOf("main", "start", "run", "init"), 0),
            QuizQuestion("Apa itu JVM?", null, listOf("Java Visual Machine", "Java Virtual Machine", "Java Variable Method", "Java Version Manager"), 1),
            QuizQuestion("Tanda untuk mengakhiri baris kode Java adalah...", null, listOf(":", ".", ",", ";"), 3),
            QuizQuestion("Apa arti dari 'final' pada variabel?", null, listOf("Nilai bisa diubah", "Nilai tetap (konstan)", "Variabel dihapus", "Variabel publik"), 1),
            QuizQuestion("Operator untuk sisa bagi adalah...", null, listOf("/", "*", "%", "&"), 2),
            QuizQuestion("Index array di Java dimulai dari...", null, listOf("1", "-1", "0", "bebas"), 2),
            QuizQuestion("Siapa penemu bahasa pemrograman Java?", null, listOf("James Gosling", "Mark Zuckerberg", "Bill Gates", "Elon Musk"), 0)
        )
    }
}
