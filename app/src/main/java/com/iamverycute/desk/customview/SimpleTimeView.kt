package com.iamverycute.desk.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.TextPaint
import android.util.AttributeSet
import android.view.Gravity
import android.widget.TextClock

class SimpleTimeView : TextClock {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        textSize = 74f
        gravity = Gravity.CENTER
        format24Hour = "HH:dd,EEEE"
        format12Hour = "HH:dd,EEEE"
        setTextColor(Color.WHITE)
        timePaint = TextPaint()
        weekPaint = TextPaint()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var timePaint: TextPaint? = null
    private var weekPaint: TextPaint? = null
    override fun onDraw(canvas: Canvas) {
        val paint = paint
        timePaint!!.textAlign = Paint.Align.CENTER
        timePaint!!.textSize = paint.textSize
        timePaint!!.typeface = paint.typeface
        timePaint!!.flags = paint.flags
        timePaint!!.style = Paint.Style.FILL
        timePaint!!.color = resources.getColor(android.R.color.white, null)
        timePaint!!.strokeWidth = 3f
        val time = text as String
        val xPos = width / 2
        canvas.drawText(time.substring(0, time.indexOf(",")), xPos.toFloat(), (baseline - 12).toFloat(), timePaint!!)
        weekPaint!!.textAlign = Paint.Align.CENTER
        weekPaint!!.textSize = paint.textSize
        weekPaint!!.typeface = paint.typeface
        weekPaint!!.flags = paint.flags
        weekPaint!!.style = Paint.Style.FILL
        weekPaint!!.color = resources.getColor(android.R.color.white, null)
        weekPaint!!.strokeWidth = 3f
        val week = time.substring(time.indexOf(",") + 1)
        weekPaint!!.textSize = 76f
        canvas.drawText(week, xPos.toFloat(), (baseline + 120).toFloat(), weekPaint!!)
    }
}