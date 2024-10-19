package com.example.monneytracker.db

import android.content.ContentValues
import com.example.monneytracker.model.LogType
import com.example.monneytracker.model.TaskLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MoneyTrackerRepo(private val db:MoneyTrackerDb) {
    companion object {
        fun create(db:MoneyTrackerDb): MoneyTrackerRepo{
            return MoneyTrackerRepo(db)
        }
    }

    suspend fun insert(taskLog: TaskLog) = withContext(Dispatchers.IO){
        val database = db.writableDatabase

        ContentValues().apply {
            put(DbConfig.TaskLog.COL_TASK_NAME, taskLog.name)
            put(DbConfig.TaskLog.COL_MONEY, taskLog.money)
            put(DbConfig.TaskLog.COL_TYPE, taskLog.type.toString())
        }.also {
            database.insert(DbConfig.TaskLog.TABLE_NAME, null, it)
        }
    }

    suspend fun list() = withContext(Dispatchers.IO){
        val database = db.readableDatabase

        val cursor = database.query(true, DbConfig.TaskLog.TABLE_NAME,
            arrayOf(
                DbConfig.TaskLog.COL_ID,
                DbConfig.TaskLog.COL_TASK_NAME,
                DbConfig.TaskLog.COL_MONEY,
                DbConfig.TaskLog.COL_TYPE,
            ),
            null, null, null, null, null, null)

        val result = mutableListOf<TaskLog>()
        cursor?.let {
            if(cursor.moveToFirst()){
                do {
                    val id = cursor.getColumnIndex(DbConfig.TaskLog.COL_ID)
                    val name = cursor.getColumnIndex(DbConfig.TaskLog.COL_TASK_NAME)
                    val money = cursor.getColumnIndex(DbConfig.TaskLog.COL_MONEY)
                    val type = cursor.getColumnIndex(DbConfig.TaskLog.COL_TYPE)
                    result.add(
                        TaskLog(
                            id = cursor.getInt(id),
                            name = cursor.getString(name),
                            money = cursor.getInt(money),
                            type = LogType.valueOf(cursor.getString(type))
                        )
                    )
                }while (cursor.moveToNext())
            }
        }
        result
    }

    suspend fun delete(id:Int) = withContext(Dispatchers.IO){
        val database = db.writableDatabase

        database.delete(DbConfig.TaskLog.TABLE_NAME, "_id=$id", null) > 0
    }

    suspend fun update(taskLog: TaskLog) = withContext(Dispatchers.IO){
        val database = db.writableDatabase
        ContentValues().apply {
            put(DbConfig.TaskLog.COL_TASK_NAME, taskLog.name)
            put(DbConfig.TaskLog.COL_MONEY, taskLog.money)
            put(DbConfig.TaskLog.COL_TYPE, taskLog.type.toString())
        }.also {
            database.update(DbConfig.TaskLog.TABLE_NAME, it, "_id=${taskLog.id}", null)
        }

    }
}