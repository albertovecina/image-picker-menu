package com.zdev.library.view

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.provider.MediaStore
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.core.content.FileProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.single.BasePermissionListener
import com.zdev.library.R
import com.zdev.library.presenter.PictureMenuPresenter
import com.zdev.library.presenter.PictureMenuPresenterImpl
import com.zdev.library.utils.FileUtils
import com.zdev.rentspace.view.utils.extensions.getAbsolutePath
import kotlinx.android.synthetic.main.view_image_picker_menu.view.*


/**
 * Created by Alberto Vecina Sánchez on 26/02/19.
 */
class PictureMenuFab : RelativeLayout, PictureMenuView {

    companion object {
        private const val REQUEST_CODE_TAKE_PHOTO_FROM_CAMERA = 10
        private const val REQUEST_CODE_TAKE_PHOTO_FROM_GALLERY = 11

        const val ORIENTATION_LEFT = 0
        const val ORIENTATION_TOP = 1
    }

    constructor(context: Context?) : super(context) {
        init(context)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }


    private var margin: Int = 0
    private var orientation: Int = ORIENTATION_TOP
    private var originalHeight: Int = 0

    private var showAnimatorSet: AnimatorSet = AnimatorSet()
    private var hideAnimatorSet: AnimatorSet = AnimatorSet()

    private var onTakePictureListener: ((filePath: String) -> Unit)? = null

    var presenter: PictureMenuPresenter = PictureMenuPresenterImpl(this)

    private fun init(context: Context?, attrs: AttributeSet? = null) {
        if (context is BaseActivity) {
            LayoutInflater.from(context).inflate(R.layout.view_image_picker_menu, this)

            if (attrs != null)
                initAttributes(context, attrs)

            fabMain.setOnClickListener { presenter.onMainButtonClick() }
            fabGallery.setOnClickListener { presenter.onGalleryButtonClick() }
            fabCamera.setOnClickListener { presenter.onCameraButtonClick() }
        }
    }

    private fun initAttributes(context: Context, attrs: AttributeSet) {
        gravity = Gravity.BOTTOM or Gravity.END

        var typedArray = context.obtainStyledAttributes(attrs, R.styleable.PictureMenuFab)
        val iconResId = typedArray.getResourceId(R.styleable.PictureMenuFab_icon, 0)
        orientation = typedArray.getInt(R.styleable.PictureMenuFab_orientation, ORIENTATION_TOP)
        margin = typedArray.getDimensionPixelSize(R.styleable.PictureMenuFab_buttonMargin, 0)
        typedArray.recycle()

        typedArray = context.obtainStyledAttributes(attrs, R.styleable.FloatingActionButton)
        val fabSize = typedArray.getInt(R.styleable.FloatingActionButton_fabSize, 0)
        typedArray.recycle()

        fabMain.setImageResource(iconResId)
        configureFab(fabMain, margin, fabSize)
        configureFab(fabGallery, margin, fabSize)
        configureFab(fabCamera, margin, fabSize)
    }

    private fun configureFab(fab: FloatingActionButton, margin: Int, size: Int) {
        val layoutParams = fab.layoutParams as RelativeLayout.LayoutParams
        layoutParams.setMargins(margin, margin, margin, margin)
        fab.layoutParams = layoutParams
        fab.size = size
    }

    override fun showMenu() {
        originalHeight = fabMain.height + 2 * margin

        val fabSize: Int = fabCamera.height + 2 * margin

        if (hideAnimatorSet.isRunning)
            hideAnimatorSet.removeAllListeners()

        var translation = ObjectAnimator.ofFloat(fabCamera, "translationY", -fabSize.toFloat())
        if (orientation == ORIENTATION_LEFT)
            translation = ObjectAnimator.ofFloat(fabCamera, "translationX", -fabSize.toFloat())

        showAnimatorSet.playTogether(
            ObjectAnimator.ofFloat(fabCamera, "alpha", 0f, 1f),
            translation
        )
        showAnimatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                if (orientation == ORIENTATION_LEFT)
                    layoutParams.width = originalHeight * 2
                else
                    layoutParams.height = originalHeight * 2
                requestLayout()
            }
        })
        showAnimatorSet.start()
        showGalleryFab()
    }

    override fun closeMenu() {
        if (showAnimatorSet.isRunning)
            showAnimatorSet.removeAllListeners()

        var translation = ObjectAnimator.ofFloat(fabCamera, "translationY", 0f)
        if (orientation == ORIENTATION_LEFT)
            translation = ObjectAnimator.ofFloat(fabCamera, "translationX", 0f)

        hideAnimatorSet.playTogether(
            ObjectAnimator.ofFloat(fabCamera, "alpha", 1f, 0f),
            translation
        )
        hideAnimatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                if (orientation == ORIENTATION_LEFT)
                    layoutParams.width = originalHeight
                else
                    layoutParams.height = originalHeight
                requestLayout()
            }
        })
        hideAnimatorSet.start()
        showMainFab()
    }

    override fun requestPictureFromCamera() {
        Dexter.withActivity(context as Activity)
            .withPermission(Manifest.permission.CAMERA)
            .withListener(object : BasePermissionListener() {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    launchCameraIntent(onTakePictureListener)
                }
            }).check()
    }

    override fun requestPictureFromGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            Dexter.withActivity(context as Activity)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(object : BasePermissionListener() {
                    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                        launchGalleryIntent(onTakePictureListener)
                    }
                }).check()
        } else {
            launchGalleryIntent(onTakePictureListener)
        }
    }

    private fun showGalleryFab() {
        fabMain.hide()
        fabGallery.show()
    }

    private fun showMainFab() {
        fabGallery.hide()
        fabMain.show()
    }

    private fun launchCameraIntent(onTakePictureListener: ((filePath: String) -> Unit)?) {
        with(context as BaseActivity) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val imageFile = FileUtils.createImageFile(this)
            val imageUri = FileProvider.getUriForFile(
                this,
                packageName,
                imageFile
            )
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            if (intent.resolveActivity(packageManager) != null)
                startActivityForResult(
                    intent,
                    REQUEST_CODE_TAKE_PHOTO_FROM_CAMERA,
                    object : BaseActivity.OnActivityResultListener {
                        override fun onResult(data: Intent?) {
                            onTakePictureListener?.invoke(imageFile.absolutePath)
                        }
                    })
        }
    }

    private fun launchGalleryIntent(onTakePictureListener: ((filePath: String) -> Unit)?) {
        with(context as BaseActivity) {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            if (intent.resolveActivity(packageManager) != null)
                startActivityForResult(
                    intent,
                    REQUEST_CODE_TAKE_PHOTO_FROM_GALLERY,
                    object : BaseActivity.OnActivityResultListener {
                        override fun onResult(data: Intent?) {
                            onTakePictureListener?.invoke(
                                data?.data?.getAbsolutePath(context) ?: ""
                            )
                        }
                    })
        }
    }

    fun setOnTakePictureListener(onTakePictureListener: (filePath: String) -> Unit) {
        this.onTakePictureListener = onTakePictureListener
    }

}