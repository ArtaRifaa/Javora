package com.yarsi.javora.data

object QuizRepository {
    fun getQuestionsForTopic(topic: String): List<QuizQuestion> {
        return when (topic) {
            "Pewarisan (Inheritance)" -> listOf(
                QuizQuestion(
                    question = "Keyword apa yang digunakan untuk melakukan pewarisan di Java?",
                    options = listOf("implements", "extends", "inherits", "this"),
                    correctAnswerIndex = 1
                ),
                QuizQuestion(
                    question = "Apa output dari kode berikut?",
                    codeSnippet = "class A { void show() { System.out.print(\"A\"); } }\nclass B extends A { void show() { System.out.print(\"B\"); } }\n\nA obj = new B();\nobj.show();",
                    options = listOf("A", "B", "Runtime Error", "Compilation Error"),
                    correctAnswerIndex = 1
                ),
                QuizQuestion(
                    question = "Dapatkah sebuah class di Java mewarisi lebih dari satu class (Multiple Inheritance)?",
                    options = listOf("Ya", "Tidak", "Hanya jika menggunakan keyword super", "Hanya class tertentu"),
                    correctAnswerIndex = 1
                )
                // Tambahkan soal ke-4 sampai ke-20 di sini
            )
            "Perulangan (Looping)" -> listOf(
                QuizQuestion(
                    question = "Perulangan mana yang minimal dijalankan satu kali meskipun kondisi salah?",
                    options = listOf("for", "while", "do-while", "foreach"),
                    correctAnswerIndex = 2
                ),
                QuizQuestion(
                    question = "Apa arti dari keyword 'break' dalam perulangan?",
                    options = listOf("Melompati satu iterasi", "Menghentikan seluruh perulangan", "Mengulang dari awal", "Menghapus variabel"),
                    correctAnswerIndex = 1
                )
            )
            else -> getGeneralQuestions() // Soal umum jika topik tidak ditemukan
        }
    }

    private fun getGeneralQuestions(): List<QuizQuestion> {
        return listOf(
            QuizQuestion("Siapa penemu bahasa Java?", null, listOf("James Gosling", "Steve Jobs", "Bill Gates", "Elon Musk"), 0),
            QuizQuestion("Apa ekstensi file kode sumber Java?", null, listOf(".class", ".java", ".exe", ".jar"), 1)
        )
    }
}
