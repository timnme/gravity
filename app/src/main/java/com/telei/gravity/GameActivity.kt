package com.telei.gravity

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
}

enum class GameObject {
    BLACK_HOLE, WORMHOLE
}

class GameActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(GameView(this))
    }
}

class ConstructorActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ConstructorView(this).apply {
            showObjectSelection = {
                SelectObjectDialogFragment().apply {
                    onSelection = ::onObjectSelected
                }.show(supportFragmentManager, "dialog")
            }
        })
    }
}