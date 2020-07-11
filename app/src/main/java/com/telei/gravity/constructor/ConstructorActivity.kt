package com.telei.gravity.constructor

import android.os.Bundle
import com.telei.gravity.App
import com.telei.gravity.BaseActivity
import com.telei.gravity.R
import com.telei.gravity.game.*
import com.telei.gravity.start
import kotlinx.android.synthetic.main.activity_constructor.*

class ConstructorActivity : BaseActivity() {
    private lateinit var constructorView: ConstructorView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_constructor)
        constructorView = constructor

        constructorView.onCreate = {
            CreateGameEntityDialogFragment().apply {
                onCreated = {
                    when (it) {
                        is Aim, is Point -> Unit
                        is Attractor -> showAttractorEdit(it, true)
                        is Portal -> constructorView.onPortalEnterCreated()
                    }
                }
            }.show(supportFragmentManager, "createDialog")
        }
        constructorView.onEdit = {
            when (it) {
                is Aim, is Point -> Unit
                is Attractor -> showAttractorEdit(it, false)
                is Portal -> Unit
            }
        }

        buttonTry.setOnClickListener {
            App.currentLevel = constructor.construct()
            start<GameActivity>()
        }
        buttonSave.setOnClickListener {
            App.levels.add(constructor.construct().clone())
            App.saveLevels()
            finish()
        }
    }

    private fun showAttractorEdit(attractor: Attractor, new: Boolean) {
        EditAttractorDialogFragment().apply {
            this.new = new
            edited = attractor
            onEdited = {
                if (new) {
                    constructorView.onCreated(attractor)
                } else {
                    constructorView.onEdited(attractor)
                }
            }
            onDeleted = {
                constructorView.onDeleted(attractor)
            }
        }.show(supportFragmentManager, "editAttractorDialog")
    }
}