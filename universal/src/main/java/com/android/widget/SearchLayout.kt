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
import com.android.util.SizeUtil

/**
 * Created by xuzhb on 2019/10/20
 * Desc:搜索框
 */
class SearchLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, @AttrRes defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    companion object {
        private val DEFAULT_SEARCH_TEXT = ""
        private val DEFAULT_SEARCH_HINT = ""
        private val DEFAULT_SEARCH_TEXT_SIZE = SizeUtil.dp2px(15f)
        private val DEFAULT_SEARCH_TEXT_COLOR = Color.BLACK
        private val DEFAULT_SEARCH_TEXT_COLOR_HINT = Color.parseColor("#C4C4C4")
        private val DEFAULT_SEARCH_INPUT_TYPE = InputType.TYPE_CLASS_TEXT
    }

    var searchText: String = DEFAULT_SEARCH_TEXT  //EditText的文本
        set(value) {
            field = value
            mSearchEt.setText(value)
        }
        get() = mSearchEt.text.toString()
    var searchHint: String = DEFAULT_SEARCH_HINT  //EditText的Hint文本
        set(value) {
            field = value
            mSearchEt.hint = value
        }
    var searchTextSize: Float = DEFAULT_SEARCH_TEXT_SIZE  //EditText的字体大小
        set(value) {
            field = value
            mSearchEt.setTextSize(TypedValue.COMPLEX_UNIT_PX, value)
        }
    var searchTextColor: Int = DEFAULT_SEARCH_TEXT_COLOR  //EditText的文本颜色
        set(value) {
            field = value
            mSearchEt.setTextColor(value)
        }
    var searchTextColorHint: Int = DEFAULT_SEARCH_TEXT_COLOR_HINT  //EditText的Hint文本颜色
        set(value) {
            field = value
            mSearchEt.setHintTextColor(value)
        }
    var searchInputType: Int = DEFAULT_SEARCH_INPUT_TYPE  //EditText的输入类型
        set(value) {
            field = value
            mSearchEt.inputType = value
        }

    private var mSearchEt: EditText
    private var mClearIv: ImageView

    init {
        val layout = LayoutInflater.from(context).inflate(R.layout.layout_search, this)
        mSearchEt = layout.findViewById(R.id.search_et)
        mClearIv = layout.findViewById(R.id.clear_iv)

        val ta = context.obtainStyledAttributes(attrs, R.styleable.SearchLayout)
        searchText = ta.getString(R.styleable.SearchLayout_searchText) ?: DEFAULT_SEARCH_TEXT
        searchHint = ta.getString(R.styleable.SearchLayout_searchHint) ?: DEFAULT_SEARCH_HINT
        searchTextSize = ta.getDimension(R.styleable.SearchLayout_searchTextSize, DEFAULT_SEARCH_TEXT_SIZE)
        searchTextColor = ta.getColor(R.styleable.SearchLayout_searchTextColor, DEFAULT_SEARCH_TEXT_COLOR)
        searchTextColorHint = ta.getColor(R.styleable.SearchLayout_searchTextColorHint, DEFAULT_SEARCH_TEXT_COLOR_HINT)
        searchInputType = ta.getInt(R.styleable.SearchLayout_searchInputType, DEFAULT_SEARCH_INPUT_TYPE)
        ta.recycle()

        with(mSearchEt) {
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {

                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    with(mClearIv) {
                        if (TextUtils.isEmpty(s)) {
                            visibility = View.INVISIBLE
                        } else {
                            visibility = View.VISIBLE
                        }
                    }
                    mOnTextChangedListener?.invoke(s, start, before, count)
                }

            })
            inputType = searchInputType
            setText(searchText)
            setHint(searchHint)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, searchTextSize)
            setTextColor(searchTextColor)
            setHintTextColor(searchTextColorHint)
        }

        mClearIv.setOnClickListener {
            mSearchEt.setText("")
        }
    }

    //监听输入框文字变化
    private var mOnTextChangedListener: ((s: CharSequence?, start: Int, before: Int, count: Int) -> Unit)? = null

    fun setOnTextChangedListener(listener: (s: CharSequence?, start: Int, before: Int, count: Int) -> Unit) {
        this.mOnTextChangedListener = listener
    }

}