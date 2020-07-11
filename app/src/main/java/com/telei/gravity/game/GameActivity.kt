package com.telei.gravity.game

import android.os.Bundle
import androidx.core.view.doOnLayout
import com.telei.gravity.App
import com.telei.gravity.BaseActivity

class GameActivity : BaseActivity() {
    private lateinit var gameView: GameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gameView = GameView(this)
        setContentView(gameView)
        gameView.doOnLayout {
            gameView.playMode = true
            gameView.gameData = App.currentLevel
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

