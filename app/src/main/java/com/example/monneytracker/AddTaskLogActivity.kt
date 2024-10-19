package com.example.monneytracker

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import com.example.monneytracker.databinding.ActivityAddTaskLogBinding
import com.example.monneytracker.db.MoneyTrackerDb
import com.example.monneytracker.db.MoneyTrackerRepo
import com.example.monneytracker.helper.SharePref
import com.example.monneytracker.helper.get
import com.example.monneytracker.helper.put
import com.example.monneytracker.model.LogType
import com.example.monneytracker.model.TaskLog
import io.ghyeok.stickyswitch.widget.StickySwitch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.lang.Exception
import kotlin.coroutines.CoroutineContext

class AddTaskLogActivity : AppCompatActivity(), CoroutineScope {

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private val moneyTrackerRepo: MoneyTrackerRepo by lazy {
        val db = MoneyTrackerDb(this)
        MoneyTrackerRepo(db)
    }

    private lateinit var binding: ActivityAddTaskLogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddTaskLogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = "Add task log"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.apply {
            var logType = LogType.ADD
            stickySwitch.setA(object: StickySwitch.OnSelectedChangeListener{
                override fun onSelectedChange(direction: StickySwitch.Direction, text: String) {
                    logType = LogType.valueOf(text)
                }
            })

            btnAddTask.setOnClickListener {
                val taskName = extendedEditTaskName.text.toString()
                val money = extendedEditMoney.text.toString().toInt()

                launch {
                    moneyTrackerRepo.insert(TaskLog(name = taskName, money = money, type = logType))
                }

                calculateTrackingMoney(logType, money)
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }

    private fun calculateTrackingMoney(logType: LogType, money: Int) {
        try {
            val currentMoney = SharePref.create(this)?.get(logType.toString(), 0) ?: 0
            SharePref.create(this)?.put(logType.toString(), currentMoney + money)
        }catch(e: Exception){
            Log.e("error", e.stackTraceToString());
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

}