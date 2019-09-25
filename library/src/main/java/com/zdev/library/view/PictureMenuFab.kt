package com.zdev.library.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.RelativeLayout
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.zdev.library.R
import com.zdev.library.presenter.PictureMenuPresenter
import com.zdev.library.presenter.PictureMenuPresenterImpl
import kotlinx.android.synthetic.main.view_image_picker_menu.view.*


/**
 * Created by Alberto Vecina Sánchez on 26/02/19.
 */
class PictureMenuFab : RelativeLayout, PictureMenuView {

    companion object {
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
        LayoutInflater.from(context).inflate(R.layout.view_image_picker_menu, this)

        fabMain.setOnClickListener { presenter.onMainButtonClick() }
        fabGallery.setOnClickListener { presenter.onGalleryButtonClick() }
        fabCamera.setOnClickListener { presenter.onCameraButtonClick() }

        if (attrs != null && context != null)
            initAttributes(context, attrs)
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
        val layoutParams = fab.layoutParams as LayoutParams
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
        launchCameraIntent(onTakePictureListener)
    }

    override fun requestPictureFromGallery() {
        launchGalleryIntent(onTakePictureListener)
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
        PicturePickerActivity.requestFromCamera(context) {
            onTakePictureListener?.invoke(it)
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    private fun launchGalleryIntent(onTakePictureListener: ((filePath: String) -> Unit)?) {
        PicturePickerActivity.requestFromGallery(context) {
            onTakePictureListener?.invoke(it)
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    fun setOnTakePictureListener(onTakePictureListener: (filePath: String) -> Unit) {
        this.onTakePictureListener = onTakePictureListener
    }

}