package com.android.widget.picGetterDialog

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.view.*
import androidx.annotation.FloatRange
import androidx.annotation.LayoutRes
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.android.universal.R
import com.android.util.BitmapUtil
import com.android.widget.ViewHolder
import com.yalantis.ucrop.UCrop
import java.io.File
import java.io.IOException

/**
 * Created by xuzhb on 2020/1/27
 * Desc:拍照或从相册选取选择框，提供一些基础的方法
 */
abstract class BasePicGetterDialog : DialogFragment() {

    companion object {
        private const val OPEN_CAMERA_REQUEST_CODE = 0x0001   //拍照
        private const val OPEN_GALLERY_REQUEST_CODE = 0x0002  //从相册选取
        private const val CAMERA_PERMISSION_REQUEST_CODE = 0x0100      //申请相机权限
        private const val READ_WRITE_PERMISSION_REQUEST_CODE = 0x0200  //申请读写权限
        private const val EXTRA_PIC_URL = "EXTRA_PIC_URL"
    }

    @LayoutRes
    protected var mLayoutId: Int = -1           //对话框的布局id
    private var mWidth: Int = 0                 //对话框的宽度
    private var mHeight: Int = 0                //对话框的高度
    private var mMargin: Int = 0                //对话框左右边距
    private var mDimAmount: Float = 0.5f        //背景透明度
    private var mAnimationStyle: Int = -1       //对话框出现消失的动画
    private var mCancelable: Boolean = true     //是否可点击取消
    private var mCropOptions: UCrop.Options? = null  //裁剪配置
    private var mMaxCropWidth: Int = 1080       //裁剪图片支持的最大宽度
    private var mMaxCropHeight: Int = 2400      //裁剪图片支持的最大高度
    private var mGravity: Int = Gravity.BOTTOM  //对话框显示的位置，默认底部
    private var mOnPicGetterListener: OnPicGetterListener? = null  //图片获取回调

    private var mCurrentPhotoPath: String? = null  //当前拍照的地址

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.DialogStyle)
        mLayoutId = getLayoutId()  //设置dialog布局
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(mLayoutId, container, false)
        convertView(ViewHolder(view), dialog)  //获取dialog布局的控件
        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(EXTRA_PIC_URL, mCurrentPhotoPath)
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        mCurrentPhotoPath = savedInstanceState?.getString(EXTRA_PIC_URL)
    }

    override fun onCancel(dialog: DialogInterface) {
        mOnPicGetterListener?.onCancel()
    }

    override fun onStart() {
        super.onStart()
        initParams()
    }

    //初始化参数
    private fun initParams() {
        dialog?.window?.let {
            val params = it.attributes
            params.dimAmount = mDimAmount
            params.gravity = mGravity
            //设置dialog宽度
            if (mWidth == 0) {
                params.width = context!!.resources.displayMetrics.widthPixels - 2 * mMargin
            } else {
                params.width = mWidth
            }
            //设置dialog高度
            if (mHeight == 0) {
                params.height = WindowManager.LayoutParams.WRAP_CONTENT
            } else {
                params.height = mHeight
            }
            //设置dialog动画
            if (mAnimationStyle != -1) {
                it.setWindowAnimations(mAnimationStyle)
            }
            it.attributes = params
        }
        isCancelable = mCancelable
    }

    //设置宽高
    fun setViewParams(width: Int, height: Int): BasePicGetterDialog {
        mWidth = width
        mHeight = height
        return this
    }

    //设置左右的边距
    fun setHorizontalMargin(margin: Int): BasePicGetterDialog {
        mMargin = margin
        return this
    }

    //设置背景昏暗度
    fun setDimAmount(@FloatRange(from = 0.0, to = 1.0) dimAmount: Float): BasePicGetterDialog {
        mDimAmount = dimAmount
        return this
    }

    //设置动画
    fun setAnimationStyle(@StyleRes animationStyle: Int): BasePicGetterDialog {
        mAnimationStyle = animationStyle
        return this
    }

    //设置Outside是否可点击
    fun setOutsideCancelable(cancelable: Boolean): BasePicGetterDialog {
        mCancelable = cancelable
        return this
    }

    //裁剪配置
    fun setCropOptions(options: UCrop.Options): BasePicGetterDialog {
        mCropOptions = options
        return this
    }

    //设置裁剪的图片支持的最大宽度和高度
    fun setMaxCropSize(width: Int, height: Int): BasePicGetterDialog {
        mMaxCropWidth = width
        mMaxCropHeight = height
        return this
    }

    //在底部显示
    fun show(manager: FragmentManager) {
        super.show(manager, BasePicGetterDialog::class.java.name)
    }

    //在中间显示
    fun showAtCenter(manager: FragmentManager) {
        mGravity = Gravity.CENTER
        super.show(manager, BasePicGetterDialog::class.java.name)
    }

    //监听图片获取事件
    fun setOnPicGetterListener(listener: OnPicGetterListener) {
        mOnPicGetterListener = listener
    }

    //拍照
    protected fun openCamera() {
        if (!hasCameraPermission()) {
            return
        }
        activity?.let {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(it.packageManager) != null) {
                val picName = "temp_" + System.currentTimeMillis()
                val picDir = it.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                mCurrentPhotoPath = ""
                try {
                    val photoFile = File.createTempFile(picName, ".jpg", picDir)
                    mCurrentPhotoPath = photoFile.getAbsolutePath()
                    val photoUri =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)  //Android 7.0后通过FileProvider共享文件，如系统照片
                            FileProvider.getUriForFile(
                                it,
                                it.applicationContext.applicationInfo.packageName + ".provider",
                                photoFile
                            )
                        else Uri.fromFile(photoFile)
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                    startActivityForResult(intent, OPEN_CAMERA_REQUEST_CODE)  //开启拍照
                } catch (e: IOException) {
                    e.printStackTrace()
                    mOnPicGetterListener?.onFailure(e.message ?: "创建文件失败，请检查手机内存空间")
                }
            }
        }
    }

    //从相册选取
    protected fun openGallery() {
        if (!hasReadWritePermission()) {
            return
        }
        activity?.let {
            val intent = Intent(Intent.ACTION_PICK, null)
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            startActivityForResult(intent, OPEN_GALLERY_REQUEST_CODE)  //开启相册
        }
    }

    //是否开启相机权限
    private fun hasCameraPermission(): Boolean {
        activity?.let {
            if (!isPermissionGranted(it, Manifest.permission.CAMERA)
                || !isPermissionGranted(it, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                || !isPermissionGranted(it, Manifest.permission.READ_EXTERNAL_STORAGE)
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {  //Android 6.0以后权限动态申请
                    requestPermissions(
                        arrayOf(
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ), CAMERA_PERMISSION_REQUEST_CODE
                    )
                }
                return false
            } else {
                return true
            }
        }
        return false
    }

    //是否开启读写权限
    private fun hasReadWritePermission(): Boolean {
        activity?.let {
            if (!isPermissionGranted(it, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                || !isPermissionGranted(it, Manifest.permission.READ_EXTERNAL_STORAGE)
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {  //Android 6.0以后权限动态申请
                    requestPermissions(
                        arrayOf(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ), READ_WRITE_PERMISSION_REQUEST_CODE
                    )
                }
                return false
            } else {
                return true
            }
        }
        return false
    }

    private fun isPermissionGranted(context: Context, permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    //获取dialog的布局Id
    abstract fun getLayoutId(): Int

    //处理dialog布局上的控件
    abstract fun convertView(holder: ViewHolder, dialog: Dialog?)

    //申请权限后回调
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {  //申请相机权限
            if (hasAllPermissions(grantResults)) {
                openCamera()
            } else {
                mOnPicGetterListener?.onFailure("相机权限获取失败")
            }
        }
        if (requestCode == READ_WRITE_PERMISSION_REQUEST_CODE) {  //申请读写权限
            if (hasAllPermissions(grantResults)) {
                openGallery()
            } else {
                mOnPicGetterListener?.onFailure("读写权限获取失败")
            }
        }
    }

    private fun hasAllPermissions(grantResults: IntArray): Boolean {
        for (i in grantResults.indices) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OPEN_CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            onCameraResult()
        }
        if (requestCode == OPEN_GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            onGalleryResult(data)
        }
        if (requestCode == UCrop.REQUEST_CROP) {
            onCropResult(resultCode, data)
        }
    }

    //拍照回调
    private fun onCameraResult() {
        if (TextUtils.isEmpty(mCurrentPhotoPath)) {
            mOnPicGetterListener?.onFailure("无法获取图片地址")
            return
        }
        openUCrop(Uri.fromFile(File(mCurrentPhotoPath)))
    }

    //相册采集回调
    private fun onGalleryResult(data: Intent?) {
        if (data == null) {
            mOnPicGetterListener?.onFailure("获取相册图片失败")
            return
        }
        val uri = data.data
        if (uri == null) {
            mOnPicGetterListener?.onFailure("无法获取图片地址")
            return
        }
        openUCrop(uri)
    }

    //启动剪裁页
    protected open fun openUCrop(uri: Uri) {
        activity?.let {
            val cacheDir = it.cacheDir
            val options = if (mCropOptions != null) mCropOptions!! else UCrop.Options()
            options.setShowCropFrame(false)
            UCrop.of(uri, Uri.fromFile(File(cacheDir, "${System.currentTimeMillis()}_cache.jpg")))
                .withOptions(options)
                .withMaxResultSize(mMaxCropWidth, mMaxCropHeight)
                .start(it, this)
        }
    }

    //图片裁剪回调
    private fun onCropResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (data == null) {
                mOnPicGetterListener?.onFailure("获取图片失败")
                return
            }
            val uri = UCrop.getOutput(data)
            if (uri == null) {
                mOnPicGetterListener?.onFailure("图片剪裁出错")
                return
            }
            val bitmap = BitmapUtil.bytes2Bitmap(BitmapUtil.compressByQuality(BitmapFactory.decodeFile(uri.path), 500))
            mOnPicGetterListener?.onSuccess(bitmap, uri.path)
            dismiss()
        } else if (resultCode == UCrop.RESULT_ERROR) {
            mOnPicGetterListener?.onFailure("图片剪裁出错")
        } else {
            mOnPicGetterListener?.onFailure("图片剪裁已取消")
        }
    }

}