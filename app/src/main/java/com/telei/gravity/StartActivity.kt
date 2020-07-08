package com.telei.gravity

import android.content.Intent
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_start_title_animated.*
import kotlinx.android.synthetic.main.activity_start.*

class StartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        start.setOnClickListener {
            startActivity(Intent(this, GameActivity::class.java))
        }
        construct.setOnClickListener {
            startActivity(Intent(this, ConstructorActivity::class.java))
        }
    }
}

class StartActivityTitleAnimated : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_title_animated)
        (sssss.drawable as AnimatedVectorDrawable).start()
    }
}