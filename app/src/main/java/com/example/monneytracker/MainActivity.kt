package com.example.monneytracker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.monneytracker.adapter.LogAdapter
import com.example.monneytracker.databinding.ActivityMainBinding
import com.example.monneytracker.db.MoneyTrackerDb
import com.example.monneytracker.db.MoneyTrackerRepo
import com.example.monneytracker.helper.SharePref
import com.example.monneytracker.helper.get
import com.example.monneytracker.helper.put
import com.example.monneytracker.model.LogType
import com.example.monneytracker.model.TaskLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), CoroutineScope {

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private lateinit var binding: ActivityMainBinding
    private lateinit var logAdapter: LogAdapter

    private val moneyTrackerRepo :MoneyTrackerRepo by lazy {
        val db = MoneyTrackerDb(applicationContext)
        MoneyTrackerRepo.create(db)
    }

    private val openAddTaskLogActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result ->
        when(result.resultCode){
            Activity.RESULT_OK -> {
                Toast.makeText(this, "ok", Toast.LENGTH_SHORT).show()
                loadData(false)
            }
            Activity.RESULT_CANCELED -> {
                Toast.makeText(this, "cancel", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        title = "Money tracker"

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        logAdapter = LogAdapter(moneyTrackerRepo = moneyTrackerRepo,
            onDeleteItem = {
                val current = SharePref.create(applicationContext)?.get(it.type.toString(), 0) ?: 0
                SharePref.create(applicationContext)?.put(it.type.toString(), current - it.money)

                if(binding.recyclerview.adapter?.itemCount == 0){
                    showEmptyTask()
                }else{
                    showTrackMoney()
                }

            },
            onUpdateItem = {}
        )

        binding.apply {
            recyclerview.apply {
                val linearLayoutManager = LinearLayoutManager(applicationContext)
                ContextCompat.getDrawable(applicationContext, R.drawable.bg_divider)

                setItemViewCacheSize(2)
                layoutManager = linearLayoutManager
                adapter = logAdapter
                addItemDecoration(DividerItemDecoration(applicationContext, linearLayoutManager.orientation))
            }
        }

        loadData(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.addTaskLog -> {
                val intent = Intent(this, AddTaskLogActivity::class.java)
                openAddTaskLogActivityResultLauncher.launch(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadData(enabledDelay: Boolean) {
        launch{
            if(enabledDelay){
                delay(2000L)
            }


            val listTasks = moneyTrackerRepo.list()

            withContext(Dispatchers.Main){
                if(listTasks.isEmpty()){
                    showEmptyTask()
                }else {
                    displayTasks(listTasks)
                    showTrackMoney()
                }
            }
        }
    }

    private fun showTrackMoney(){
//        SharePref.create(applicationContext)?.put(LogType.ADD.toString(), 0)
//        SharePref.create(applicationContext)?.put(LogType.SUBTRACT.toString(), 0)
        binding.apply {
            tvAddMoney.text = (SharePref.create(applicationContext)?.getInt(LogType.ADD.toString(), 0) ?: 0).toString()
            tvSubtractMoney.text = (SharePref.create(applicationContext)?.getInt(LogType.SUBTRACT.toString(), 0) ?: 0).toString()
        }
    }

    private fun showEmptyTask() {
        binding.loading.visibility = View.GONE
        binding.container.visibility = View.GONE

        binding.tvStatus.apply {
            visibility = View.VISIBLE
            text = "EMPTY"
        }
    }
    private fun displayTasks(listTasks: MutableList<TaskLog>) {
        binding.loading.visibility = View.GONE
        binding.tvStatus.visibility = View.GONE

        binding.container.visibility = View.VISIBLE

        logAdapter.setData(listTasks)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }


}