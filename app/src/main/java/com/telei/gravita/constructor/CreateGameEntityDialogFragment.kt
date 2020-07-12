package com.telei.gravita.constructor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.telei.gravita.R
import com.telei.gravita.game.Attractor
import com.telei.gravita.game.Body
import com.telei.gravita.game.Portal
import kotlinx.android.synthetic.main.dialog_create_game_entity.*

class CreateGameEntityDialogFragment : DialogFragment() {
    lateinit var onCreated: (Body) -> Unit

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.dialog_create_game_entity, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buttonAttractor.setOnClickListener {
            onCreated(Attractor(attracting = true))
            dismiss()
        }
        buttonRepulsor.setOnClickListener {
            onCreated(Attractor(attracting = false))
            dismiss()
        }
        buttonPortal.setOnClickListener {
            onCreated(Portal())
            dismiss()
        }
    }
}