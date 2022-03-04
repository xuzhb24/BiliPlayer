package com.android.widget

import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.annotation.AttrRes
import com.android.universal.R
import com.android.util.LayoutParamsUtil
import com.android.util.SizeUtil

/**
 * Created by xuzhb on 2019/11/21
 * Desc:带删除按钮的输入框
 */
class InputLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, @AttrRes defStyleAttrs: Int = 0
) : RelativeLayout(context, attrs, defStyleAttrs) {

    companion object {
        private val DEFAULT_TEXT = ""
        private val DEFAULT_TEXT_HINT = ""
        private val DEFAULT_TEXT_TYPE = InputType.TYPE_CLASS_TEXT
        private val DEFAULT_TEXT_SIZE = SizeUtil.sp2px(15f)
        private val DEFAULT_TEXT_COLOR = Color.BLACK
        private val DEFAULT_TEXT_COLOR_HINT = Color.parseColor("#a3a3a3")
        private val DEFAULT_MULTI_LINE = true
        private val DEFAULT_EDITABLE = true
        private val DEFAULT_SHOW_DIVIDER_LINE = true
        private val DEFAULT_DIVIDER_COLOR = Color.parseColor("#dbdbdb")
        private val DEFAULT_DIVIDER_HEIGHT = SizeUtil.dp2px(1f)
    }

    var inputText: String = DEFAULT_TEXT  //EditText输入文本
        set(value) {
            field = value
            mInputEt.setText(value)
        }
        get() = mInputEt.text.toString()

    var inputTextHint: String = DEFAULT_TEXT_HINT  //EditText未输入时的hint文本
        set(value) {
            field = value
            mInputEt.hint = value
        }

    var inputTextType: Int = DEFAULT_TEXT_TYPE  //EditText输入类型
        set(value) {
            field = value
            setInputType(multiLine, value)
        }

    var inputTextSize = DEFAULT_TEXT_SIZE  //EditText字体大小
        set(value) {
            field = value
            mInputEt.setTextSize(TypedValue.COMPLEX_UNIT_PX, value)
        }

    var inputTextColor = DEFAULT_TEXT_COLOR  //EditText字体颜色
        set(value) {
            field = value
            mInputEt.setTextColor(value)
        }

    var inputTextColorHint = DEFAULT_TEXT_COLOR_HINT  //EditText的hint文本字体颜色
        set(value) {
            field = value
            mInputEt.setHintTextColor(value)
        }

    var multiLine = DEFAULT_MULTI_LINE  //是否支持输入多行文本，默认支持
        set(value) {
            field = value
            setInputType(value, inputTextType)
        }

    var editable = DEFAULT_EDITABLE  //是否可编辑，默认可以
        set(value) {
            field = value
            mInputEt.isEnabled = value
            mClearIv.visibility = if (value && !TextUtils.isEmpty(inputText)) View.VISIBLE else View.GONE
        }

    var showDividerLine = DEFAULT_SHOW_DIVIDER_LINE  //是否显示下划线
        set(value) {
            field = value
            mDivierLine.visibility = if (showDividerLine) View.VISIBLE else View.GONE
        }

    var dividerColor = DEFAULT_DIVIDER_COLOR  //下划线颜色
        set(value) {
            field = value
            mDivierLine.setBackgroundColor(dividerColor)
        }

    var dividerHeight = DEFAULT_DIVIDER_HEIGHT  //下划线高度
        set(value) {
            field = value
            LayoutParamsUtil.setHeight(mDivierLine, dividerHeight.toInt())
        }

    //获取输入框对应的EditText
    fun getEditText(): EditText = mInputEt

    private var mInputEt: EditText
    private var mClearIv: ImageView
    private var mDivierLine: View

    init {
        val layout = LayoutInflater.from(context).inflate(R.layout.layout_input, this)
        mInputEt = layout.findViewById(R.id.input_et)
        mClearIv = layout.findViewById(R.id.clear_iv)
        mDivierLine = layout.findViewById(R.id.divider_line)
        attrs?.let {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.InputLayout)
            inputText = ta.getString(R.styleable.InputLayout_inputText) ?: DEFAULT_TEXT
            inputTextHint = ta.getString(R.styleable.InputLayout_inputTextHint) ?: DEFAULT_TEXT_HINT
            inputTextType = ta.getInt(R.styleable.InputLayout_inputTextType, DEFAULT_TEXT_TYPE)
            inputTextSize = ta.getDimension(R.styleable.InputLayout_inputTextSize, DEFAULT_TEXT_SIZE)
            inputTextColor = ta.getColor(R.styleable.InputLayout_inputTextColor, DEFAULT_TEXT_COLOR)
            inputTextColorHint = ta.getColor(R.styleable.InputLayout_inputTextColorHint, DEFAULT_TEXT_COLOR_HINT)
            multiLine = ta.getBoolean(R.styleable.InputLayout_multiLine, DEFAULT_MULTI_LINE)
            editable = ta.getBoolean(R.styleable.InputLayout_editable, DEFAULT_EDITABLE)
            showDividerLine = ta.getBoolean(R.styleable.InputLayout_showDividerLine, DEFAULT_SHOW_DIVIDER_LINE)
            dividerColor = ta.getColor(R.styleable.InputLayout_dividerColor, DEFAULT_DIVIDER_COLOR)
            dividerHeight = ta.getDimension(R.styleable.InputLayout_dividerHeight, DEFAULT_DIVIDER_HEIGHT)
            ta.recycle()
        }

        with(mInputEt) {
            setText(inputText)
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    mClearIv.visibility = if (editable && !TextUtils.isEmpty(s)) View.VISIBLE else View.GONE
                    mOnTextChangedListener?.invoke(s, start, before, count)
                }
            })
            hint = inputTextHint
            setInputType(multiLine, inputTextType)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, inputTextSize)
            setTextColor(inputTextColor)
            setHintTextColor(inputTextColorHint)
            isEnabled = editable
//            setOnFocusChangeListener { v, hasFocus ->
//                if (hasFocus) {
//                    mDivierLine.setBackgroundColor(Color.parseColor("#db4b3c"))
//                } else {
//                    mDivierLine.setBackgroundColor(Color.parseColor("#a3a3a3"))
//                }
//            }
        }

        with(mClearIv) {
            visibility = if (editable && !TextUtils.isEmpty(inputText)) View.VISIBLE else View.GONE
            setOnClickListener {
                mInputEt.setText("")
                mInputEt.requestFocus()
                mOnTextClearListener?.invoke()
            }
        }

        with(mDivierLine) {
            visibility = if (showDividerLine) View.VISIBLE else View.GONE
            setBackgroundColor(dividerColor)
            LayoutParamsUtil.setHeight(mDivierLine, dividerHeight.toInt())
        }
    }

    //支持输入多行文本
    private fun setInputType(multiLine: Boolean, inputType: Int) {
        mInputEt.inputType = if (multiLine && inputType == InputType.TYPE_CLASS_TEXT)
            inputType or InputType.TYPE_TEXT_FLAG_MULTI_LINE else inputType
    }

    private var mOnTextChangedListener: ((s: CharSequence?, start: Int, before: Int, count: Int) -> Unit)? = null

    //输入文字变化时回调
    fun setOnTextChangedListener(listener: (s: CharSequence?, start: Int, before: Int, count: Int) -> Unit) {
        this.mOnTextChangedListener = listener
    }

    private var mOnTextClearListener: (() -> Unit)? = null

    //清空文本时回调
    fun setOnTextClearListener(onTextClearListener: (() -> Unit)) {
        this.mOnTextClearListener = onTextClearListener
    }

}