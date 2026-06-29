package com.bariatricnepal.app.ui.weight

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View

/**
 * A small, dependency-free line chart for the weight history. Pass values
 * oldest-first or newest-first — it just plots them left to right in the
 * order given.
 */
class WeightChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var values: List<Float> = emptyList()

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#0A5FB4")
        style = Paint.Style.STROKE
        strokeWidth = 6f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#0A5FB4")
        style = Paint.Style.FILL
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E6E9EF")
        strokeWidth = 2f
    }

    private val emptyTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#69728A")
        textSize = 36f
        textAlign = Paint.Align.CENTER
    }

    fun setValues(newValues: List<Float>) {
        values = newValues
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        val paddingTop = 24f
        val paddingBottom = 24f
        val paddingSide = 16f

        if (values.size < 2) {
            canvas.drawText(
                if (values.isEmpty()) "Log your weight to see your progress chart"
                else "Log one more weight to see your trend",
                w / 2, h / 2, emptyTextPaint
            )
            return
        }

        val max = values.max()
        val min = values.min()
        val range = if (max - min < 1f) 1f else (max - min)

        val chartWidth = w - paddingSide * 2
        val chartHeight = h - paddingTop - paddingBottom
        val stepX = if (values.size > 1) chartWidth / (values.size - 1) else 0f

        // Horizontal guide lines
        for (i in 0..3) {
            val y = paddingTop + chartHeight * i / 3
            canvas.drawLine(paddingSide, y, w - paddingSide, y, gridPaint)
        }

        fun pointY(v: Float): Float = paddingTop + chartHeight - ((v - min) / range) * chartHeight

        val linePath = Path()
        val fillPath = Path()

        values.forEachIndexed { i, v ->
            val x = paddingSide + stepX * i
            val y = pointY(v)
            if (i == 0) {
                linePath.moveTo(x, y)
                fillPath.moveTo(x, h - paddingBottom)
                fillPath.lineTo(x, y)
            } else {
                linePath.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }
        fillPath.lineTo(paddingSide + stepX * (values.size - 1), h - paddingBottom)
        fillPath.close()

        fillPaint.shader = LinearGradient(
            0f, paddingTop, 0f, h - paddingBottom,
            Color.parseColor("#330A5FB4"), Color.parseColor("#000A5FB4"),
            Shader.TileMode.CLAMP
        )
        canvas.drawPath(fillPath, fillPaint)
        canvas.drawPath(linePath, linePaint)

        values.forEachIndexed { i, v ->
            val x = paddingSide + stepX * i
            val y = pointY(v)
            canvas.drawCircle(x, y, 7f, dotPaint)
        }
    }
}
