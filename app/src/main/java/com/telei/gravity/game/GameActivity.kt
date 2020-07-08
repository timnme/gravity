package com.telei.gravity.game

import android.os.Bundle
import androidx.core.view.doOnLayout
import com.telei.gravity.BaseActivity

class GameActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val gameView = GameView(this)
        setContentView(gameView)
        gameView.doOnLayout {
            var id = 0
            gameView.gameData = GameData(
                aim = Aim(xR = 0.8f, yR = 0.8f),
                point = Point(xR = 0.3f, yR = 0.3f),
                attractors = listOf(
                    Attractor(
                        id = id++,
                        xR = 0.5f,
                        yR = 0.5f
                    )
                )
            )
        }
    }
}

