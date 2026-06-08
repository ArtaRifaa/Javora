package com.yarsi.javora.data

object QuestionRepository {
    private val questionsMap = mapOf(
        "Pewarisan (Inheritance)" to listOf(
            QuizQuestion(
                question = "Keyword apa yang digunakan untuk mewarisi sebuah class di Java?",
                options = listOf("implements", "extends", "inherits", "using"),
                correctAnswerIndex = 1
            ),
            QuizQuestion(
                question = "Apa itu 'superclass'?",
                options = listOf("Class yang mewarisi sifat", "Class yang mewariskan sifat", "Class yang tidak bisa diinstansiasi", "Class utama dalam package"),
                correctAnswerIndex = 1
            ),
            QuizQuestion(
                question = "Apakah Java mendukung multiple inheritance untuk class?",
                options = listOf("Ya", "Tidak", "Hanya untuk abstract class", "Hanya jika menggunakan keyword 'both'"),
                correctAnswerIndex = 1
            ),
            QuizQuestion(
                question = "Keyword apa yang digunakan untuk memanggil constructor dari superclass?",
                options = listOf("this()", "parent()", "super()", "base()"),
                correctAnswerIndex = 2
            ),
            QuizQuestion(
                question = "Manakah dari berikut ini yang benar tentang pewarisan?",
                options = listOf("Subclass memiliki semua member private superclass", "Private member tidak diwariskan", "Constructor diwariskan secara otomatis", "Method final bisa di-override"),
                correctAnswerIndex = 1
            )
        ),
        "Perulangan (Looping)" to listOf(
            QuizQuestion(
                question = "Perulangan mana yang menjamin kode dijalankan setidaknya satu kali?",
                options = listOf("for loop", "while loop", "do-while loop", "foreach loop"),
                correctAnswerIndex = 2
            ),
            QuizQuestion(
                question = "Apa output dari perulangan ini: for(int i=0; i<3; i++) { System.out.print(i); }",
                options = listOf("012", "123", "0123", "0 1 2 "),
                correctAnswerIndex = 0
            ),
            QuizQuestion(
                question = "Keyword apa yang digunakan untuk menghentikan perulangan sepenuhnya?",
                options = listOf("stop", "exit", "break", "continue"),
                correctAnswerIndex = 2
            ),
            QuizQuestion(
                question = "Kapan 'continue' digunakan dalam perulangan?",
                options = listOf("Untuk menghentikan loop", "Untuk melewati iterasi saat ini", "Untuk memulai ulang loop dari awal", "Untuk keluar dari program"),
                correctAnswerIndex = 1
            ),
            QuizQuestion(
                question = "Manakah penulisan for-each (enhanced for loop) yang benar di Java?",
                options = listOf("for(int i in list)", "for(int i : list)", "foreach(int i : list)", "for(list as i)"),
                correctAnswerIndex = 1
            )
        ),
        "Koleksi (Collections)" to listOf(
            QuizQuestion(
                question = "Interface mana yang tidak mengizinkan elemen duplikat?",
                options = listOf("List", "Set", "Map", "Collection"),
                correctAnswerIndex = 1
            ),
            QuizQuestion(
                question = "Koleksi mana yang menyimpan data dalam pasangan key-value?",
                options = listOf("ArrayList", "HashSet", "HashMap", "LinkedList"),
                correctAnswerIndex = 2
            ),
            QuizQuestion(
                question = "ArrayList adalah implementasi dari interface apa?",
                options = listOf("Set", "Queue", "List", "Deque"),
                correctAnswerIndex = 2
            ),
            QuizQuestion(
                question = "Method apa yang digunakan untuk menambah elemen ke dalam ArrayList?",
                options = listOf("push()", "put()", "add()", "insert()"),
                correctAnswerIndex = 2
            ),
            QuizQuestion(
                question = "Manakah koleksi yang paling cepat untuk akses data berdasarkan index?",
                options = listOf("LinkedList", "ArrayList", "Stack", "Vector"),
                correctAnswerIndex = 1
            )
        ),
        "Pemrograman Berorientasi Objek" to listOf(
            QuizQuestion(
                question = "Apa saja 4 pilar utama OOP?",
                options = listOf("Inheritance, Polymorphism, Encapsulation, Abstraction", "Class, Object, Method, Variable", "Java, Python, C++, Ruby", "Public, Private, Protected, Default"),
                correctAnswerIndex = 0
            ),
            QuizQuestion(
                question = "Apa tujuan dari 'Encapsulation'?",
                options = listOf("Menyembunyikan detail implementasi", "Membuat class banyak", "Mempercepat kode", "Mewarisi sifat"),
                correctAnswerIndex = 0
            ),
            QuizQuestion(
                question = "Sebuah 'blueprint' atau cetak biru dalam Java disebut...",
                options = listOf("Object", "Variable", "Method", "Class"),
                correctAnswerIndex = 3
            ),
            QuizQuestion(
                question = "Instansiasi dari sebuah Class disebut...",
                options = listOf("Blueprint", "Object", "Method", "Inheritance"),
                correctAnswerIndex = 1
            ),
            QuizQuestion(
                question = "Modifier apa yang membuat member hanya bisa diakses di dalam class yang sama?",
                options = listOf("public", "protected", "private", "default"),
                correctAnswerIndex = 2
            )
        ),
        "Polymorphism" to listOf(
            QuizQuestion(
                question = "Apa arti dari Polymorphism?",
                options = listOf("Satu bentuk banyak fungsi", "Banyak bentuk satu nama", "Banyak class satu fungsi", "Pewarisan bertingkat"),
                correctAnswerIndex = 1
            ),
            QuizQuestion(
                question = "Method Overriding terjadi pada...",
                options = listOf("Satu class yang sama", "Antara superclass dan subclass", "Antara dua class yang tidak berhubungan", "Di dalam interface saja"),
                correctAnswerIndex = 1
            ),
            QuizQuestion(
                question = "Apa perbedaan overloading dan overriding?",
                options = listOf("Tidak ada perbedaan", "Overloading di class berbeda, overriding di class sama", "Overloading parameter harus beda, overriding parameter harus sama", "Overloading hanya untuk constructor"),
                correctAnswerIndex = 2
            ),
            QuizQuestion(
                question = "Keyword apa yang menandakan sebuah method meng-override method parent di Java?",
                options = listOf("@Override", "@Inherit", "@Replace", "@Method"),
                correctAnswerIndex = 0
            ),
            QuizQuestion(
                question = "Mana yang merupakan contoh polymorphism?",
                options = listOf("Variabel lokal", "Operator overloading", "Dynamic Method Dispatch", "Konstanta final"),
                correctAnswerIndex = 2
            )
        )
    )

    fun getQuestionsForTopic(topic: String): List<QuizQuestion> {
        return questionsMap[topic] ?: emptyList()
    }
}
