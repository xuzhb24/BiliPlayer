package com.android.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.AttrRes
import com.android.universal.R
import com.android.util.SizeUtil
import java.util.regex.Pattern
import kotlin.math.max

/**
 * Created by xuzhb on 2020/8/9
 * Desc:点击展开/收起的TextView
 */
class ExpandTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, @AttrRes defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private val DEFAULT_MAX_SHOW_LINES = 3
        private val DEFAULT_LINE_SPACING = SizeUtil.dp2px(2f)
        private val DEFAULT_CONTENT_SUFFIX_TEXT = "..."
        private val DEFAULT_CONTENT_TEXT_SIZE = SizeUtil.sp2px(15f)
        private val DEFAULT_CONTENT_TEXT_COLOR = Color.BLACK
        private val DEFAULT_LABEL_EXPAND_TEXT = "展开"
        private val DEFAULT_LABEL_SHRINK_TEXT = "收起"
        private val DEFAULT_LABEL_TEXT_SIZE = SizeUtil.sp2px(15f)
        private val DEFAULT_LABEL_TEXT_COLOR = Color.BLACK
        private val DEFAULT_LABEL_MARGIN_LEFT = SizeUtil.dp2px(5f)
        private val DEFAULT_LABEL_MARGIN_RIGHT = SizeUtil.dp2px(5f)
        private val DEFAULT_BOUNDS_ALIGN = true
    }

    var maxShowLines: Int = DEFAULT_MAX_SHOW_LINES  //收起状态时最多显示的文本行数
    var lineSpacing: Float = DEFAULT_LINE_SPACING   //文本的行间距
    var contentText: String = ""                    //主内容的文本
        set(value) {
            field = getFilterText(value)
            bottom = calculateHeight(width)
            requestLayout()
            invalidate()
        }
    var contentSuffixText: String = DEFAULT_CONTENT_SUFFIX_TEXT  //主内容收起时的文本后缀，一般是...
    var contentTextSize: Float = DEFAULT_CONTENT_TEXT_SIZE       //主内容的文本字体大小
    var contentTextColor: Int = DEFAULT_CONTENT_TEXT_COLOR       //主内容的文本字体颜色
    var labelExpandText: String = DEFAULT_LABEL_EXPAND_TEXT      //展开/收起标签展开的文本描述
    var labelShrinkText: String = DEFAULT_LABEL_SHRINK_TEXT      //展开/收起标签收起的文本描述
    var labelTextSize: Float = DEFAULT_LABEL_TEXT_SIZE           //展开/收起标签的文本字体大小
    var labelTextColor: Int = DEFAULT_LABEL_TEXT_COLOR           //展开/收起标签的文本字体颜色
    var labelMarginLeft: Float = DEFAULT_LABEL_MARGIN_LEFT       //展开/收起标签的左边距
    var labelMarginRight: Float = DEFAULT_LABEL_MARGIN_RIGHT     //展开/收起标签的右边距
    var boundsAlign: Boolean = DEFAULT_BOUNDS_ALIGN              //文本是否两端分散对齐

    //当前是否是展开状态
    var isExpand = false
        set(value) {
            field = value
            bottom = calculateHeight(width)
            requestLayout()
            invalidate()
        }

    private lateinit var mContentPaint: TextPaint
    private lateinit var mLabelPaint: TextPaint
    private var mLabelRectF: RectF = RectF()
    private var mOnTexClickListener: OnTextClickListener? = null
    private var isDownInLabel = false  //手指是否按在标签内

    init {
        attrs?.let {
            parseAttribute(getContext(), it)
        }
        initPaint()
    }

    //获取布局属性并设置属性默认值
    private fun parseAttribute(context: Context, attrs: AttributeSet) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.ExpandTextView)
        maxShowLines = ta.getInteger(R.styleable.ExpandTextView_maxShowLines, DEFAULT_MAX_SHOW_LINES)
        lineSpacing = ta.getDimension(R.styleable.ExpandTextView_lineSpacing, DEFAULT_LINE_SPACING)
        contentText = ta.getString(R.styleable.ExpandTextView_contentText) ?: ""
        contentSuffixText = ta.getString(R.styleable.ExpandTextView_contentSuffixText) ?: DEFAULT_CONTENT_SUFFIX_TEXT
        contentTextSize = ta.getDimension(R.styleable.ExpandTextView_contentTextSize, DEFAULT_CONTENT_TEXT_SIZE)
        contentTextColor = ta.getColor(R.styleable.ExpandTextView_contentTextColor, DEFAULT_CONTENT_TEXT_COLOR)
        labelExpandText = ta.getString(R.styleable.ExpandTextView_labelExpandText) ?: DEFAULT_LABEL_EXPAND_TEXT
        labelShrinkText = ta.getString(R.styleable.ExpandTextView_labelShrinkText) ?: DEFAULT_LABEL_SHRINK_TEXT
        labelTextSize = ta.getDimension(R.styleable.ExpandTextView_labelTextSize, DEFAULT_LABEL_TEXT_SIZE)
        labelTextColor = ta.getColor(R.styleable.ExpandTextView_labelTextColor, DEFAULT_LABEL_TEXT_COLOR)
        labelMarginLeft = ta.getDimension(R.styleable.ExpandTextView_labelMarginLeft, DEFAULT_LABEL_MARGIN_LEFT)
        labelMarginRight = ta.getDimension(R.styleable.ExpandTextView_labelMarginRight, DEFAULT_LABEL_MARGIN_RIGHT)
        boundsAlign = ta.getBoolean(R.styleable.ExpandTextView_boundsAlign, DEFAULT_BOUNDS_ALIGN)
        ta.recycle()
    }

    //初始化画笔
    private fun initPaint() {
        mContentPaint = TextPaint()
        with(mContentPaint) {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = contentTextColor
            textSize = contentTextSize
        }
        mLabelPaint = TextPaint()
        with(mLabelPaint) {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = labelTextColor
            textSize = labelTextSize
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        var widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        var height = MeasureSpec.getSize(heightMeasureSpec)
        when (heightMode) {
            MeasureSpec.EXACTLY -> height = height
            else -> height = calculateHeight(width)
        }
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            drawText(it)
        }
    }

    private fun drawText(canvas: Canvas) {
        if (TextUtils.isEmpty(contentText)) {
            canvas.drawText("", 0f, getBaseLine(mContentPaint, 0f), mContentPaint)
            return
        }
        val staticLayout = getStaticLayout(contentText, mContentPaint, width)
        val lineCount = staticLayout.lineCount
        if (lineCount <= maxShowLines) {  //当前内容不足以展开
            var height = 0f
            for (i in 0 until lineCount) {
                val currentLineText = contentText.substring(staticLayout.getLineStart(i), staticLayout.getLineEnd(i))
                val currentLineStaticLayout = getStaticLayout(currentLineText, mContentPaint, width)
                height += currentLineStaticLayout.height
                if (boundsAlign && i != lineCount - 1) {
                    drawAlignText(canvas, currentLineText, getBaseLine(mContentPaint, height), mContentPaint)
                } else {
                    canvas.drawText(currentLineText, 0f, getBaseLine(mContentPaint, height), mContentPaint)
                }
                height += lineSpacing
            }
        } else {
            if (isExpand) {  //已展开
                val shrinkStaticLayout = getStaticLayout(labelShrinkText, mLabelPaint, width)
                val shrinkStaticLayoutWidth = shrinkStaticLayout.getLineWidth(0)
                val shrinkStaticLayoutHeight = shrinkStaticLayout.height
                var height = 0f
                for (i in 0 until lineCount) {
                    //获取当前行内容
                    val currentLineText = contentText.substring(staticLayout.getLineStart(i), staticLayout.getLineEnd(i))
                    val currentLineStaticLayout = getStaticLayout(currentLineText, mContentPaint, width)
                    if (i == lineCount - 1) {  //最后一行
                        val currentLineWidth = currentLineStaticLayout.getLineWidth(0)
                        if (currentLineWidth + labelMarginLeft + shrinkStaticLayoutWidth > width) {  //标签需要换行处理
                            height += currentLineStaticLayout.height
                            canvas.drawText(currentLineText, 0f, getBaseLine(mContentPaint, height), mContentPaint)  //绘制主内容
                            height += lineSpacing
                            mLabelRectF.set(0f, height, shrinkStaticLayoutWidth, height + shrinkStaticLayoutHeight)
                            height += shrinkStaticLayoutHeight
                            canvas.drawText(labelShrinkText, 0f, getBaseLine(mLabelPaint, height), mLabelPaint)  //绘制展开/收起标签
                        } else {
                            var currentWidth = 0f
                            //绘制主内容
                            canvas.drawText(
                                currentLineText,
                                currentWidth,
                                getBaseLine(mContentPaint, height + currentLineStaticLayout.height),
                                mContentPaint
                            )
                            currentWidth += currentLineWidth
                            currentWidth += labelMarginLeft
                            //绘制展开/收起标签
                            canvas.drawText(
                                labelShrinkText,
                                currentWidth,
                                getBaseLine(mLabelPaint, height + shrinkStaticLayoutHeight),
                                mLabelPaint
                            )
                            mLabelRectF.set(
                                currentWidth,
                                height,
                                currentWidth + shrinkStaticLayoutWidth,
                                height + shrinkStaticLayoutHeight
                            )
                        }
                    } else {  //普通行
                        height += currentLineStaticLayout.height
                        if (boundsAlign) {
                            drawAlignText(canvas, currentLineText, getBaseLine(mContentPaint, height), mContentPaint)
                        } else {
                            canvas.drawText(currentLineText, 0f, getBaseLine(mContentPaint, height), mContentPaint)
                        }
                        height += lineSpacing
                    }
                }
            } else {  //已收起
                //收起状态多了后缀，后缀需要特殊处理
                val expandStaticLayout = getStaticLayout(labelExpandText, mLabelPaint, width)
                val expandStaticLayoutHeight = expandStaticLayout.height
                val expandStaticLayoutWidth = expandStaticLayout.getLineWidth(0)
                var height = 0f
                for (i in 0 until maxShowLines) {
                    //获取当前行内容
                    val currentLineText = contentText.substring(staticLayout.getLineStart(i), staticLayout.getLineEnd(i))
                    val currentLineStaticLayout = getStaticLayout(currentLineText, mContentPaint, width)
                    if (i == maxShowLines - 1) {  //最后一行
                        //需要计算后缀的内容信息
                        val contentSuffixStaticLayout = getStaticLayout(contentSuffixText, mContentPaint, width)
                        val contentSuffixStaticLayoutWidth = contentSuffixStaticLayout.getLineWidth(0)
                        //主内容可以进行绘制的宽度，labelMarginRight收起时是不生效的
                        val contentTextWidth =
                            width - expandStaticLayoutWidth - labelMarginLeft - contentSuffixStaticLayoutWidth - labelMarginRight
                        //当前正在绘制的宽度
                        var currentWidth = 0f
                        //临时的一个高度，表达当前主内容的y值
                        val tempHeight = height + currentLineStaticLayout.height
                        for (text in currentLineText) {
                            val currentString = text.toString()
                            val currentStringStaticLayout = getStaticLayout(currentString, mContentPaint, width)
                            val currentStringStaticLayoutWidth = currentStringStaticLayout.getLineWidth(0)
                            if (currentWidth + currentStringStaticLayoutWidth > contentTextWidth) {
                                //这个文字不能继续绘制了，开始绘制标签
                                //绘制后缀信息
                                canvas.drawText(
                                    contentSuffixText,
                                    currentWidth,
                                    getBaseLine(mContentPaint, tempHeight),
                                    mContentPaint
                                )
                                currentWidth += contentSuffixStaticLayoutWidth
                                currentWidth += labelMarginLeft
                                //绘制展开/收起标签
                                canvas.drawText(
                                    labelExpandText,
                                    currentWidth,
                                    getBaseLine(mLabelPaint, height + expandStaticLayoutHeight),
                                    mLabelPaint
                                )
                                //设置其响应的区域范围
                                mLabelRectF.set(
                                    currentWidth,
                                    height,
                                    currentWidth + expandStaticLayoutWidth,
                                    height + expandStaticLayoutHeight
                                )
                                return
                            } else {
                                //绘制主内容
                                canvas.drawText(
                                    currentString,
                                    currentWidth,
                                    getBaseLine(mContentPaint, tempHeight),
                                    mContentPaint
                                )
                                currentWidth += currentStringStaticLayoutWidth
                            }
                        }
                    } else {  //普通行
                        height += currentLineStaticLayout.height
                        if (boundsAlign) {
                            drawAlignText(canvas, currentLineText, getBaseLine(mContentPaint, height), mContentPaint)
                        } else {
                            canvas.drawText(currentLineText, 0f, getBaseLine(mContentPaint, height), mContentPaint)
                        }
                        height += lineSpacing
                    }
                }
            }
        }
    }

    //绘制文本，两端分散对齐
    private fun drawAlignText(
        canvas: Canvas,
        lindText: String,
        lineY: Float,
        paint: TextPaint
    ) {
        var lineText = lindText
        var x = 0f
        if (lineText.length > 3 && lineText[0] == ' ' && lineText[1] == ' ') {
            val blanks = "  "
            canvas.drawText(blanks, x, lineY, paint)
            val bw = StaticLayout.getDesiredWidth(blanks, paint)
            x += bw
            lineText = lineText.substring(3)
        }
        val gapCount = lineText.length - 1
        var i = 0
        if (lineText.length > 2 && lineText[0].toInt() == 12288 && lineText[1].toInt() == 12288) {
            val substring = lineText.substring(0, 2)
            val cw = StaticLayout.getDesiredWidth(substring, paint)
            canvas.drawText(substring, x, lineY, paint)
            x += cw
            i += 2
        }
        val textWidth = paint.measureText(lineText)
        val d = (width - textWidth) / gapCount
        while (i < lineText.length) {
            val c = lineText[i].toString()
            val cw = StaticLayout.getDesiredWidth(c, paint)
            canvas.drawText(c, x, lineY, paint)
            x += cw + d
            i++
        }
    }

    //获取文本绘制的基准线
    private fun getBaseLine(paint: TextPaint, descent: Float): Float {
        val fm = paint.fontMetrics
        return descent - fm.descent

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            val x = it.x
            val y = it.y
            when (it.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (mOnTexClickListener != null) {
                        isDownInLabel = mLabelRectF.contains(x, y)
                        return true
                    } else {
                        isDownInLabel = false
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (isDownInLabel) {
                        if (mLabelRectF.contains(x, y)) {
                            mOnTexClickListener?.onLabelTextClick(isExpand)  //点击了标签
                            return true
                        } else {  //滑到其他区域了

                        }
                    } else {
                        mOnTexClickListener?.onContentTextClick(isExpand)
                        return true
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }

    //剔除文本中的一些特殊字符，特殊字符用空字符串来替换
    private fun getFilterText(text: String?): String {
        if (TextUtils.isEmpty(text)) {
            return ""
        }
        val p = Pattern.compile("\\s*|\t|\r|\n")
        val m = p.matcher(text)
        return m.replaceAll("")
    }

    //计算高度
    private fun calculateHeight(width: Int): Int {
        if (TextUtils.isEmpty(contentText)) {  //无内容
            return 0
        }
        if (width <= 0) {
            return 0
        }
        val staticLayout = getStaticLayout(contentText, mContentPaint, width)
        val lineCount = staticLayout.lineCount
        var height = 0f
        if (lineCount <= maxShowLines) {
            height = staticLayout.height + lineCount * lineSpacing - lineSpacing
        } else {
            if (isExpand) {  //已展开
                //计算展开时的高度
                for (i in 0 until lineCount) {
                    //获取当前行内容
                    val currentLineText = contentText.substring(staticLayout.getLineStart(i), staticLayout.getLineEnd(i))
                    val currentLineStaticLayout = getStaticLayout(currentLineText, mContentPaint, width)
                    if (i == lineCount - 1) {  //最后一行
                        val shrinkStaticLayout = getStaticLayout(labelShrinkText, mLabelPaint, width)
                        val shrinkWidth = shrinkStaticLayout.getLineWidth(0)
                        //主内容最后一行的宽度
                        val currentLineTextWidth = currentLineStaticLayout.getLineWidth(0)
                        //labelMarginLeft对于收起是生效的，但是labelMarginRight是不生效的
                        if (currentLineTextWidth + labelMarginLeft + shrinkWidth > width) {
                            //需要另起一行
                            height += currentLineStaticLayout.height + lineSpacing
                            height += shrinkStaticLayout.height //+ lineSpacing
                        } else {
                            //取最高的那个
                            height += max(currentLineStaticLayout.height, shrinkStaticLayout.height) //+ lineSpacing
                        }
                    } else {  //普通行
                        height += currentLineStaticLayout.height + lineSpacing
                    }
                }
            } else {  //已收起
                //计算收起时的高度
                for (i in 0 until maxShowLines) {
                    //获取当前行内容
                    val currentLineText = contentText.substring(staticLayout.getLineStart(i), staticLayout.getLineEnd(i))
                    val currentLineStaticLayout = getStaticLayout(currentLineText, mContentPaint, width)
                    if (i == maxShowLines - 1) {  //最后一行
                        val expandStaticLayout = getStaticLayout(labelShrinkText, mLabelPaint, width)
                        //计算最大行那一行的内容
                        height += max(currentLineStaticLayout.height, expandStaticLayout.height) //+ lineSpacing
                    } else {  //普通行
                        height += currentLineStaticLayout.height + lineSpacing
                    }
                }
            }
        }
        return height.toInt()
    }

    private fun getStaticLayout(text: String, paint: TextPaint, width: Int): StaticLayout =
        StaticLayout(
            text, 0, text.length, paint, width, Layout.Alignment.ALIGN_NORMAL,
            1f, 0f, false
        )

    fun setOnTextClickListener(listener: OnTextClickListener) {
        this.mOnTexClickListener = listener
    }

    interface OnTextClickListener {

        //点击内容文本时回调，isExpand：当前标签状态，返回true表示当前标签时展开的，false表示是收起的
        fun onContentTextClick(isExpand: Boolean)

        //点击展开/收起标签时回调，isExpand：当前标签状态，返回true表示当前标签时展开的，false表示是收起的
        fun onLabelTextClick(isExpand: Boolean)

    }

}