package com.telei.gravity.constructor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.telei.gravity.R
import kotlinx.android.synthetic.main.dialog_select_object.*

class SelectObjectDialogFragment : DialogFragment() {
    var onSelection: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.dialog_select_object, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buttonBlackHole.setOnClickListener {
            onSelection?.invoke()
        }
    }
}