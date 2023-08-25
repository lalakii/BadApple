package com.iamverycute.desk.adapter

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class ExTouchHelper(val adapter: ExAdapter) : ItemTouchHelper.SimpleCallback(0, 0) {
    private val helper = ItemTouchHelper(this)
    fun attachToView(view: RecyclerView?) = helper.attachToRecyclerView(view)
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) = Unit
    override fun onMoved(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        fromPos: Int,
        target: RecyclerView.ViewHolder,
        toPos: Int,
        x: Int,
        y: Int
    ) {
        super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y)
        val config =
            adapter.appContext.readConfig().filter { !it.startsWith(adapter.appContext.sortTAG) }
                .toMutableList()
        config.addAll(adapter.list.map { adapter.appContext.sortTAG + it.info.packageName })
        adapter.appContext.writeConfig(config)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        val sourcePos = viewHolder.layoutPosition
        val targetPos = target.layoutPosition
        if (sourcePos < targetPos) {
            for (i in sourcePos until targetPos) {
                adapter.swap(i, i + 1)
            }
        } else {
            for (i in sourcePos downTo targetPos + 1) {
                adapter.swap(i, i - 1)
            }
        }
        adapter.notifyItemMoved(sourcePos, targetPos)
        return true
    }

    private var holder: ExHolder? = null
    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        when (actionState) {
            ItemTouchHelper.ACTION_STATE_DRAG -> {
                if (viewHolder is ExHolder) holder = viewHolder.util.setAnimationState(false)
            }

            ItemTouchHelper.ACTION_STATE_IDLE -> {
                if (holder != null && !adapter.state) {
                    holder!!.util.setAnimationState(true)
                }
                holder = null
            }
        }
    }
}