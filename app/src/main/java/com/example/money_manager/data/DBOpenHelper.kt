package com.example.money_manager.data

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.util.Log
import com.example.money_manager.DEFAULT_CAT_LIST
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

private const val SQL_CREATE_GROUP_ENTRIES =
    "CREATE TABLE IF NOT EXISTS ${DBContract.CategoryEntry.TABLE_NAME} (" +
            "${BaseColumns._ID} INTEGER PRIMARY KEY," +
            "${DBContract.CategoryEntry.COLUMN_NAME} TEXT NOT NULL," +
            "${DBContract.CategoryEntry.COLUMN_PARENT_GROUP_ID} INTEGER);"

private const val SQL_CREATE_TRANSACTION_ENTRIES =
    "CREATE TABLE IF NOT EXISTS ${DBContract.TransactionEntry.TABLE_NAME} (" +
            "${BaseColumns._ID} INTEGER PRIMARY KEY, " +
            "${DBContract.TransactionEntry.COLUMN_VALUE} REAL DEFAULT 0, " +
            "${DBContract.TransactionEntry.COLUMN_TITLE} TEXT, " +
            "${DBContract.TransactionEntry.COLUMN_IS_EXPENSE} INTEGER DEFAULT 1, " +
            "${DBContract.TransactionEntry.COLUMN_DATE} TEXT, " +
            "${DBContract.TransactionEntry.COLUMN_DESCRIPTION} TEXT, " +
            "${DBContract.TransactionEntry.COLUMN_CATEGORY} INTEGER NOT NULL, " +
            "FOREIGN KEY (${DBContract.TransactionEntry.COLUMN_CATEGORY}) REFERENCES " +
            "${DBContract.CategoryEntry.TABLE_NAME} (${BaseColumns._ID}) " +
            ");"

private const val SQL_DROP_GROUPS = "DROP TABLE IF EXISTS ${DBContract.CategoryEntry.TABLE_NAME}"
private const val SQL_DROP_TRANSACTIONS = "DROP TABLE IF EXISTS ${DBContract.TransactionEntry.TABLE_NAME}"

private const val ORDER_BY_DATE = " ORDER BY datetime(${DBContract.TransactionEntry.COLUMN_DATE}) DESC"

class DBOpenHelper(context: Context):
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "finances.db"
        const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("PRAGMA foreign_keys=ON")
        db?.execSQL(SQL_CREATE_GROUP_ENTRIES)
        db?.execSQL(SQL_CREATE_TRANSACTION_ENTRIES)
        createDefaultGroups(db)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(SQL_DROP_TRANSACTIONS)
        db?.execSQL(SQL_DROP_GROUPS)
        onCreate(db)
    }


    private fun createDefaultGroups(db: SQLiteDatabase?) {

        for (cat in DEFAULT_CAT_LIST) {
            val group = ContentValues().apply {
                put(DBContract.CategoryEntry.COLUMN_NAME, cat) }
            db?.insert(DBContract.CategoryEntry.TABLE_NAME, null, group)
        }
    }

    fun getGroups(): Cursor? {
        val db = this.readableDatabase
        return db?.rawQuery("SELECT ${DBContract.CategoryEntry.COLUMN_NAME} from " +
                "${DBContract.CategoryEntry.TABLE_NAME}", null)
    }

    @SuppressLint("SimpleDateFormat")
    fun insertTransaction(value: Float, isExpense: Boolean, date: Date, title: String, category: Int,
                          desc: String): Long? {
        val expenseFlag = if(isExpense) 1 else 0
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        val newTransaction = ContentValues().apply {
            put(DBContract.TransactionEntry.COLUMN_VALUE, value)
            put(DBContract.TransactionEntry.COLUMN_IS_EXPENSE, expenseFlag)
            put(DBContract.TransactionEntry.COLUMN_DATE, dateFormat.format(date))
            put(DBContract.TransactionEntry.COLUMN_TITLE, title)
            put(DBContract.TransactionEntry.COLUMN_CATEGORY, category)
            put(DBContract.TransactionEntry.COLUMN_DESCRIPTION, desc)
        }

        val db = this.writableDatabase

        return db?.insert(DBContract.TransactionEntry.TABLE_NAME, null, newTransaction)
    }

    @SuppressLint("SimpleDateFormat")
    fun getTransaction(id: Int): Transaction {
        val query = "SELECT * FROM ${DBContract.TransactionEntry.TABLE_NAME} WHERE ${BaseColumns._ID} = ?"
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        val db = this.readableDatabase
        val cursor = db?.rawQuery(query, arrayOf(id.toString()))

        cursor!!.moveToFirst()
        val id = cursor.getInt(0)
        val value = cursor.getFloat(1)
        val title = cursor.getString(2)
        val isExpense = cursor.getInt(3) == 1
        val date = dateFormat.parse(cursor.getString(4))
        val desc = cursor.getString(5)
        val cat = cursor.getInt(6)

        return Transaction(id, value, title, isExpense, date, desc, cat)
    }

    @SuppressLint("SimpleDateFormat")
    fun updateTransaction(transaction: Transaction) {

        val whereClause = "${BaseColumns._ID} = ?"
        val expenseFlag = if(transaction.isExpense) 1 else 0
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        val newTransaction = ContentValues().apply {
            put(DBContract.TransactionEntry.COLUMN_VALUE, transaction.value)
            put(DBContract.TransactionEntry.COLUMN_IS_EXPENSE, expenseFlag)
            put(DBContract.TransactionEntry.COLUMN_DATE, dateFormat.format(transaction.date))
            put(DBContract.TransactionEntry.COLUMN_TITLE, transaction.title)
            put(DBContract.TransactionEntry.COLUMN_CATEGORY, transaction.category)
            put(DBContract.TransactionEntry.COLUMN_DESCRIPTION, transaction.desc)
        }

        val db = this.writableDatabase
        val rowsUpdated = db.update(DBContract.TransactionEntry.TABLE_NAME, newTransaction, whereClause, arrayOf(transaction.id.toString()))
        Log.d(this.javaClass.name, "Updated $rowsUpdated row")
    }

    fun deleteTransaction(id: Int) {
        val whereClause = "${BaseColumns._ID} = ?"
        val db = this.writableDatabase
        val rowsUpdated = db.delete(DBContract.TransactionEntry.TABLE_NAME, whereClause, arrayOf(id.toString()))
        Log.d(this.javaClass.name, "Updated $rowsUpdated rows")
    }

    fun getAllTransactions(orderByDate: Boolean): Cursor? {
        var query = "SELECT * from ${DBContract.TransactionEntry.TABLE_NAME}"
        if(orderByDate) {
            query += ORDER_BY_DATE
        }

        val db = this.readableDatabase
        return db?.rawQuery(query, null)
    }

    fun getMonthTransactions(month: String, year: String, orderByDate: Boolean): Cursor? {
        var query = "SELECT * FROM ${DBContract.TransactionEntry.TABLE_NAME} WHERE " +
                "strftime('%Y', ${DBContract.TransactionEntry.COLUMN_DATE}) = '$year' AND " +
                "strftime('%m', ${DBContract.TransactionEntry.COLUMN_DATE}) = '$month' "
        if(orderByDate) {
            query += ORDER_BY_DATE
        }

        val db = this.readableDatabase
        return db?.rawQuery(query, null)
    }

    fun getMinDate(): Date{
        var query = "SELECT min(${DBContract.TransactionEntry.COLUMN_DATE}) FROM " +
                "${DBContract.TransactionEntry.TABLE_NAME}"

        val db = this.readableDatabase
        val cursor = db.rawQuery(query, null)
        if(cursor.moveToFirst()) {
            val rawDate = cursor.getString(0)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            return dateFormat.parse(rawDate)
        }
        return Calendar.getInstance().time
    }

    fun getBalance(): Float {
        val expenses = getOperations(true)
        val incomes = getOperations(false)
        return incomes - expenses
    }

    fun getOperations(expense: Boolean): Float {
        val selection = " WHERE ${DBContract.TransactionEntry.COLUMN_IS_EXPENSE} = ?"
        val selectionArg = if(expense) "1" else "0"
        return getSum(selection, arrayOf(selectionArg))
    }

    fun getValueByCategory(expense: Boolean): HashMap<String, Float> {
        val selectionArg = if(expense) "1" else "0"
        var map = HashMap<String, Float>()
        for(category in 1 until DEFAULT_CAT_LIST.size) {
            val selection = " WHERE ${DBContract.TransactionEntry.COLUMN_CATEGORY} = ? AND " +
                    "${DBContract.TransactionEntry.COLUMN_IS_EXPENSE} = ?"
            val result = getSum(selection, arrayOf(category.toString(), selectionArg))
            if(result != 0f){
                map[DEFAULT_CAT_LIST[category]] = result
            }

        }
        return map
    }

    private fun getSum(selection: String, selectionArgs: Array<String>): Float {
        var query = "SELECT sum(${DBContract.TransactionEntry.COLUMN_VALUE}) FROM " +
                "${DBContract.TransactionEntry.TABLE_NAME}"
        val db = this.readableDatabase
        val cursor = db.rawQuery(query + selection, selectionArgs)
        return if(cursor.moveToFirst()) {
            cursor.getFloat(0)
        } else 0 as Float
    }

}