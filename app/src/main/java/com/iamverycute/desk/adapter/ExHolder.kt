package com.iamverycute.desk.adapter

import android.content.pm.ApplicationInfo
import android.view.animation.AnimationUtils
import androidx.databinding.ObservableField
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.iamverycute.desk.BR
import com.iamverycute.desk.R
import com.iamverycute.desk.model.AppDetails

class ExHolder(private val binding: ViewDataBinding, private val context: Any) : RecyclerView.ViewHolder(binding.root) {
    lateinit var util: AnimaUtil

    fun bind(m: AppDetails) {
        if (context is FragmentActivity) {
            util = AnimaUtil(m.info.flags and ApplicationInfo.FLAG_SYSTEM == ApplicationInfo.FLAG_SYSTEM)
            m.util = util
        }
        binding.setVariable(BR.model, m)
        binding.setVariable(BR.context, context)
        binding.executePendingBindings()
    }

    inner class AnimaUtil(private val inSystem: Boolean = false) {
        private val animation = AnimationUtils.loadAnimation(itemView.context, R.anim.shake)
        val removeState: ObservableField<Float> = ObservableField(0F)
        fun setAnimationState(state: Boolean):ExHolder {
            if (state) {
                itemView.startAnimation(animation)
                if (!inSystem) {
                    removeState.set(1F)
                }
            } else {
                itemView.clearAnimation()
                removeState.set(0F)
            }
            return this@ExHolder
        }
    }
}