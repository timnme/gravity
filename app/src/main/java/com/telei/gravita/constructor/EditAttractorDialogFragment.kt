package com.telei.gravita.constructor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.telei.gravita.R
import com.telei.gravita.game.Attractor
import kotlinx.android.synthetic.main.dialog_edit_attractor.*

class EditAttractorDialogFragment : DialogFragment() {
    var new = false
    lateinit var edited: Attractor
    lateinit var onEdited: () -> Unit
    lateinit var onDeleted: () -> Unit

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.dialog_edit_attractor, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        seekBar.max = Attractor.MAX_F.toInt()
        seekBar.progress = edited.f - 1
        buttonSave.setOnClickListener {
            edited.f = seekBar.progress + 1
            onEdited()
            dismiss()
        }
        buttonDelete.isVisible = !new
        buttonDelete.setOnClickListener {
            onDeleted()
            dismiss()
        }
    }
}