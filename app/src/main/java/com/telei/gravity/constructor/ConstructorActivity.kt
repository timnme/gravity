package com.telei.gravity.constructor

import android.os.Bundle
import com.telei.gravity.BaseActivity
import com.telei.gravity.Files
import com.telei.gravity.R
import com.telei.gravity.game.GameActivity
import com.telei.gravity.start
import kotlinx.android.synthetic.main.activity_constructor.*

class ConstructorActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_constructor)
        buttonReady.setOnClickListener {
            Files.gameData = constructor.construct()
            start<GameActivity>()
        }
    }
}