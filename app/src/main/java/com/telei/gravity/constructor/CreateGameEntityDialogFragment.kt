package com.telei.gravity.constructor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.telei.gravity.R
import com.telei.gravity.game.GameEntity
import kotlinx.android.synthetic.main.dialog_create_game_entity.*

class CreateGameEntityDialogFragment : DialogFragment() {
    var onCreation: ((GameEntity) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.dialog_create_game_entity, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buttonAttractor.setOnClickListener {

        }
    }
}