package com.matthewrussell.trchunkbrowser.app

import com.matthewrussell.trchunkbrowser.app.mainview.MainView
import tornadofx.App
import tornadofx.launch

class MainApp : App(MainView::class)

fun main(args: Array<String>) {
    launch<MainApp>()
}