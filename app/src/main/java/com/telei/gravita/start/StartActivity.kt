package com.telei.gravita.start

import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.telei.gravita.BaseActivity
import com.telei.gravita.R
import com.telei.gravita.constructor.ConstructorActivity
import com.telei.gravita.game.GameActivity
import com.telei.gravita.levels.LevelsActivity
import com.telei.gravita.start
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