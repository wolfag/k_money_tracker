package com.example.monneytracker.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.monneytracker.R
import com.example.monneytracker.databinding.AdapterLogBinding
import com.example.monneytracker.db.MoneyTrackerRepo
import com.example.monneytracker.model.LogType
import com.example.monneytracker.model.TaskLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

typealias OnDeleteItem = (task:TaskLog) -> Unit
typealias OnUpdateItem = (task: TaskLog) -> Unit

class LogAdapter(
    private val dataSet:MutableList<TaskLog> = mutableListOf(),
    private val moneyTrackerRepo: MoneyTrackerRepo,
    private val onDeleteItem: OnDeleteItem,
    private val onUpdateItem: OnUpdateItem
    ):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun setData(newDataSet:MutableList<TaskLog>){
        this.dataSet.clear()

        this.dataSet.addAll(newDataSet)
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val view: AdapterLogBinding): RecyclerView.ViewHolder(view.root){
        init {
            view.actionItem.setOnClickListener {
                showMenuAction(it.context, it)
            }
        }

        fun bind(item: TaskLog){
            view.apply {
                tvTaskName.text = item.name

                tvMoney.apply {
                    val (sign, color) = if(item.type == LogType.ADD) {
                        "+" to Color.GREEN
                    } else {
                        "-" to Color.RED
                    }
                    text = "$sign ${item.money}"
                    setTextColor(color)
                }

            }
        }

        private fun showMenuAction(context:Context, anchorView: View){
            var popupMenu = PopupMenu(context, anchorView)
            popupMenu.apply {
                inflate(R.menu.menu_task_action)
                setOnMenuItemClickListener { menuItem ->
                    when(menuItem.itemId){
                        R.id.updateTaskLog -> {
                            onUpdateItem(dataSet[absoluteAdapterPosition])
                            true
                        }
                        R.id.deleteTaskLog -> {
                            CoroutineScope(Dispatchers.IO).launch {
                                moneyTrackerRepo.delete(dataSet[absoluteAdapterPosition].id)
                                withContext(Dispatchers.Main){
                                    val currentPos = absoluteAdapterPosition
                                    val task = dataSet[currentPos]
                                    dataSet.removeAt(currentPos)
                                    notifyItemRemoved(currentPos)

                                    onDeleteItem(task)
                                }
                            }
                            true
                        }
                        else -> false
                    }
                }
            }.also {
                popupMenu.show()
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val holder = AdapterLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(holder)
    }

    override fun getItemCount() = dataSet.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is ViewHolder -> {
                holder.bind(dataSet[position])
            }
        }
    }
}