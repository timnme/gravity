package com.telei.gravity.start

import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.telei.gravity.R
import com.telei.gravity.constructor.ConstructorActivity
import com.telei.gravity.game.GameActivity
import com.telei.gravity.start
import kotlinx.android.synthetic.main.activity_start.*
import kotlinx.android.synthetic.main.activity_start_title_animated.*

class StartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        findViewById<ImageView>(R.id.gameTitle).setOnClickListener {
            start<StartActivityTitleAnimated>()
        }
        start.setOnClickListener {
            start<GameActivity>()
        }
        construct.setOnClickListener {
            start<ConstructorActivity>()
        }
    }
}

class StartActivityTitleAnimated : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_title_animated)
        (gameTitleAnimated.drawable as AnimatedVectorDrawable).start()
    }
}