package com.zdev.picturepickermenu.presenter

import com.zdev.picturepickermenu.view.PictureMenuView


/**
 * Created by Alberto Vecina Sánchez on 27/02/2019.
 */
class PictureMenuPresenterImpl constructor(var view: PictureMenuView) : PictureMenuPresenter() {

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