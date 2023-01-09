package com.example.money_manager.data

import java.io.Serializable
import java.util.*

data class Transaction(var id: Int, var value: Float, var title: String, var isExpense: Boolean,
                       var date: Date, var desc: String, var category: Int): Serializable