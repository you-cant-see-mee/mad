package com.example.terminallab

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val createUsersTable = """
            CREATE TABLE users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE,
                password TEXT,
                name TEXT,
                dob TEXT,
                age INTEGER
            )
        """.trimIndent()
        db.execSQL(createUsersTable)

        val createProductsTable = """
            CREATE TABLE products (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                price REAL
            )
        """.trimIndent()
        db.execSQL(createProductsTable)

        // Insert sample products
        val sampleProducts = listOf(
            Pair("Laptop", 999.99),
            Pair("Smartphone", 499.99),
            Pair("Headphones", 79.99),
            Pair("Smartwatch", 199.99),
            Pair("Camera", 599.99)
        )
        for (product in sampleProducts) {
            val values = ContentValues()
            values.put("name", product.first)
            values.put("price", product.second)
            db.insert("products", null, values)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS users")
        db.execSQL("DROP TABLE IF EXISTS products")
        onCreate(db)
    }

    fun insertUser(username: String, password: String): Boolean {
        val db = writableDatabase
        val values = ContentValues()
        values.put("username", username)
        values.put("password", password)
        val result = db.insert("users", null, values)
        db.close()
        return result != -1L
    }

    fun checkUser(username: String, password: String): Boolean {
        val db = readableDatabase
        val query = "SELECT * FROM users WHERE username = ? AND password = ?"
        val cursor = db.rawQuery(query, arrayOf(username, password))
        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }

    fun getProducts(): List<Pair<String, Double>> {
        val productList = mutableListOf<Pair<String, Double>>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT name, price FROM products", null)

        if (cursor.moveToFirst()) {
            do {
                val name = cursor.getString(0)
                val price = cursor.getDouble(1)
                productList.add(Pair(name, price))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return productList
    }

//    fun getUserDetails(username: String): User? {
//        val db = readableDatabase
//        val cursor = db.rawQuery("SELECT name, dob, age FROM users WHERE username = ?", arrayOf(username))
//
//        var user: User? = null
//
//        if (cursor != null && cursor.moveToFirst()) {
//            val nameIndex = cursor.getColumnIndex("name")
//            val dobIndex = cursor.getColumnIndex("dob")
//            val ageIndex = cursor.getColumnIndex("age")
//
//            val name = if (nameIndex != -1) cursor.getString(nameIndex) else ""
//            val dob = if (dobIndex != -1) cursor.getString(dobIndex) else ""
//            val age = if (ageIndex != -1) cursor.getInt(ageIndex) else 0
//
//            user = User(username, name, dob, age)
//        }
//
//        cursor?.close()
//        db.close()
//        return user
//    }

    fun getUserDetails(username: String): User? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT name, dob, age FROM users WHERE username = ?", arrayOf(username))

        if (cursor.moveToFirst()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val dob = cursor.getString(cursor.getColumnIndexOrThrow("dob"))
            val age = cursor.getInt(cursor.getColumnIndexOrThrow("age"))
            cursor.close()

            // Check if the values are valid
            if (name != null && dob != null) {
                return User(username, name, dob, age)
            } else {
                Log.d("DBHelper", "User data is incomplete")
                return null
            }
        } else {
            cursor.close()
            Log.d("DBHelper", "No user found with the username: $username")
            return null
        }
    }






    fun updateUserProfile(username: String, name: String, dob: String, age: Int): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("dob", dob)
            put("age", age)
        }
        val result = db.update("users", values, "username = ?", arrayOf(username))
        db.close()
        return result > 0
    }

    companion object {
        private const val DATABASE_NAME = "ShoppingApp.db"
        private const val DATABASE_VERSION = 2
    }
}

data class User(
    val username: String,
    val name: String,
    val dob: String,
    val age: Int
)
