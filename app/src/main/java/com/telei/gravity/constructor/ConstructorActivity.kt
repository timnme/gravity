package com.telei.gravity.constructor

import android.os.Bundle
import com.telei.gravity.BaseActivity

class ConstructorActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ConstructorView(this).apply {
            showObjectSelection = {
                SelectObjectDialogFragment().apply {

                }.show(supportFragmentManager, "dialog")
            }
        })
    }
}