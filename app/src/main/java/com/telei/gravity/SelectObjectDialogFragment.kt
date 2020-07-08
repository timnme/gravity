package com.telei.gravity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_select_object.*

class SelectObjectDialogFragment : DialogFragment() {
    var onSelection: ((GameObject) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.dialog_select_object, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buttonBlackHole.setOnClickListener {
            onSelection?.invoke(GameObject.BLACK_HOLE)
        }
    }
}