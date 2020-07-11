package com.telei.gravity.levels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.doOnLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.telei.gravity.App
import com.telei.gravity.BaseActivity
import com.telei.gravity.R
import com.telei.gravity.game.GameActivity
import com.telei.gravity.game.GameData
import com.telei.gravity.game.GameView
import com.telei.gravity.start
import kotlinx.android.synthetic.main.activity_levels.*
import kotlinx.android.synthetic.main.item_level.view.*

class LevelsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_levels)

        val adapter = LevelAdapter(App.levels) {
            App.currentLevel = App.levels[it]
            start<GameActivity>()
        }
        recyclerView.layoutManager =
            GridLayoutManager(this, 3, RecyclerView.VERTICAL, false)
        recyclerView.adapter = adapter
    }

}

private class LevelAdapter(
    private var levels: List<GameData> = emptyList(),
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<LevelAdapter.LevelHolder>() {

    override fun getItemCount(): Int = levels.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LevelHolder =
        LevelHolder(parent)

    override fun onBindViewHolder(holder: LevelHolder, position: Int) {
        holder.bind(levels[position], position)
    }

    private inner class LevelHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_level, parent, false)
    ) {
        init {
            itemView.setOnClickListener {
                onClick(adapterPosition)
            }
        }

        fun bind(level: GameData, pos: Int) {
            itemView.textViewLevel.text = (pos + 1).toString()
            itemView.gameView.doOnLayout {
                (it as GameView).gameData = level
            }
        }
    }
}

