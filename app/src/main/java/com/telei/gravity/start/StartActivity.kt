package com.telei.gravity.start

import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.telei.gravity.BaseActivity
import com.telei.gravity.R
import com.telei.gravity.constructor.ConstructorActivity
import com.telei.gravity.game.GameActivity
import com.telei.gravity.levels.LevelsActivity
import com.telei.gravity.start
import kotlinx.android.synthetic.main.activity_start.*
import kotlinx.android.synthetic.main.activity_start_title_animated.*

class StartActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        gameTitle.setOnClickListener {
            start<StartActivityTitleAnimated>()
        }
        start.setOnClickListener {
            start<GameActivity>()
        }
        construct.setOnClickListener {
            start<ConstructorActivity>()
        }
        buttonLevels.setOnClickListener {
            start<LevelsActivity>()
        }
    }
}

class StartActivityTitleAnimated : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_title_animated)
        (gameTitleAnimated.drawable as AnimatedVectorDrawable).start()
    }
}