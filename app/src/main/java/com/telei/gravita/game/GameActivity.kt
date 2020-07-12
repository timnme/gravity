package com.telei.gravita.game

import android.os.Bundle
import androidx.core.view.doOnLayout
import com.telei.gravita.App
import com.telei.gravita.BaseActivity

class GameActivity : BaseActivity() {
    private lateinit var gameView: GameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        levels = false
        gameView = GameView(this)
        setContentView(gameView)
        gameView.doOnLayout {
            gameView.playMode = true
            gameView.level = App.currentLevel
        }
    }

    override fun onStart() {
        super.onStart()
        gameView.start()
    }

    override fun onStop() {
        super.onStop()
        gameView.stop()
    }
}

