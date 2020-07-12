package com.telei.gravita.levels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.doOnLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.telei.gravita.App
import com.telei.gravita.BaseActivity
import com.telei.gravita.R
import com.telei.gravita.game.GameActivity
import com.telei.gravita.game.GameView
import com.telei.gravita.game.levels
import com.telei.gravita.start
import kotlinx.android.synthetic.main.activity_levels.*
import kotlinx.android.synthetic.main.item_level.view.*

class LevelsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_levels)
        levels = true
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
    private var levels: List<Level> = emptyList(),
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

        fun bind(level: Level, pos: Int) {
            itemView.textViewLevel.text = (pos + 1).toString()
            itemView.gameView.doOnLayout {
                (it as GameView).level = level
            }
        }
    }
}

