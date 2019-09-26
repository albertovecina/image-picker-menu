package com.zdev.picturepickermenu.presenter

import com.zdev.picturepickermenu.view.PicturePickerView


/**
 * Created by Alberto Vecina Sánchez on 27/02/2019.
 */
class PicturePickerPresenterImpl constructor(var view: PicturePickerView) : PicturePickerPresenter() {

    private var isOpen: Boolean = false

    override fun onMainButtonClick() {
        switchMenuStatus()
    }

    override fun onGalleryButtonClick() {
        view.requestPictureFromGallery()
        switchMenuStatus()
    }

    override fun onCameraButtonClick() {
        view.requestPictureFromCamera()
        switchMenuStatus()
    }

    private fun switchMenuStatus() {
        if (isOpen) {
            isOpen = false
            view.closeMenu()
        } else {
            isOpen = true
            view.showMenu()
        }
    }

}