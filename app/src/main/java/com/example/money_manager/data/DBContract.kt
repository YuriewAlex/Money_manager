package com.example.money_manager.data

import android.provider.BaseColumns

object DBContract {

    object TransactionEntry : BaseColumns {
        const val TABLE_NAME = "transactions"
        const val COLUMN_VALUE = "value"
        const val COLUMN_TITLE = "title"
        const val COLUMN_IS_EXPENSE = "expense"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_DATE = "date"
        const val COLUMN_DESCRIPTION = "description"
    }

    object CategoryEntry : BaseColumns {
        const val TABLE_NAME = "categories"
        const val COLUMN_NAME = "name"
        const val COLUMN_PARENT_GROUP_ID = "parent"
    }
}