package com.iamverycute.desk.fragment

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.iamverycute.desk.BR
import com.iamverycute.desk.adapter.ExAdapter

class AdapterFragment(val adapter: ExAdapter, layoutId: Int) : Fragment(layoutId) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        DataBindingUtil.bind<ViewDataBinding>(view)?.setVariable(BR.Adapter, adapter)
    }
}