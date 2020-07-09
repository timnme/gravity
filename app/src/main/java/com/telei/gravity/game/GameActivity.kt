package com.telei.gravity.game

import android.os.Bundle
import androidx.core.view.doOnLayout
import com.telei.gravity.BaseActivity
import com.telei.gravity.Files
import kotlinx.android.synthetic.main.activity_start.view.*

class GameActivity : BaseActivity() {
    private lateinit var gameView: GameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gameView = GameView(this)
        setContentView(gameView)
        gameView.doOnLayout {
            gameView.gameData = Files.gameData
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

