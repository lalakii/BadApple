package com.iamverycute.desk.customview

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.BindingMethod
import androidx.databinding.BindingMethods
import net.crosp.libs.android.circletimeview.CircleTimeView

@BindingMethods(BindingMethod(type = ClockViewGroup::class, attribute = "clockIndex", method = "setClockIndex"))
class ClockViewGroup(context: Context?, attributes: AttributeSet?) : LinearLayout(context, attributes) {

    private var clockIndex: Int = 0
    private val attr: AttributeSet? = attributes
    private val circleTimeView: CircleTimeView?
    private val simpleTimeView: SimpleTimeView?
    private val noneTextView: TextView?
    private val layoutParams: LayoutParams?

    init {
        circleTimeView = CircleTimeView(context, attr)
        simpleTimeView = SimpleTimeView(context, attr)
        noneTextView = TextView(context, attr)
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        when (clockIndex) {
            1 -> {
                setBackgroundResource(android.R.color.holo_purple)
                circleTimeView!!.layoutParams = layoutParams
                viewAdd(circleTimeView)
            }

            2 -> {
                setBackgroundResource(android.R.color.holo_blue_dark)
                simpleTimeView!!.layoutParams = layoutParams
                viewAdd(simpleTimeView)
            }

            3 -> {
                setBackgroundResource(android.R.color.darker_gray)
                noneTextView!!.text = "不显示时钟"
                noneTextView.textSize = 30f
                noneTextView.gravity = Gravity.CENTER
                noneTextView.layoutParams = layoutParams
                viewAdd(noneTextView)
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    private fun viewAdd(v: View) = run { if (v.parent == null) addView(v) }

    fun setClockIndex(clockIndex: Int) = run { this.clockIndex = clockIndex }
}