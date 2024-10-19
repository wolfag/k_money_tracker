package com.example.monneytracker.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DbConfig {
    companion object {
        const val DB_NAME = "money-tracker"
        const val DB_VERSION = 1
    }

    object TaskLog{
        const val TABLE_NAME = "task_logs"
        const val COL_ID = "_id"
        const val COL_TASK_NAME = "task_name"
        const val COL_MONEY = "money"
        const val COL_TYPE = "type"

        fun buildSchema() = """
            create table $TABLE_NAME (
                $COL_ID integer primary key autoincrement,
                $COL_TASK_NAME text,
                $COL_MONEY integer,
                $COL_TYPE text
            )
        """.trimIndent()

        fun dropTable() = "drop table if exists $TABLE_NAME"
    }
}

class MoneyTrackerDb : SQLiteOpenHelper {
    constructor(context: Context) : super(context, DbConfig.DB_NAME, null, DbConfig.DB_VERSION)
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(DbConfig.TaskLog.buildSchema())
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        db?.execSQL(DbConfig.TaskLog.dropTable())
        db?.execSQL(DbConfig.TaskLog.buildSchema())
    }

}