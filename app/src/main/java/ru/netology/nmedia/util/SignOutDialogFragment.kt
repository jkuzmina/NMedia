package ru.netology.nmedia.util

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth

class SignOutDialogFragment (val auth: AppAuth) : DialogFragment() {
    @Override
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder.setMessage(getString(R.string.dialog_signOut))
                .setPositiveButton(
                    R.string.sign_out,
                    { dialog, id ->
                        auth.removeAuth()
                        findNavController().navigateUp()
                    })
                .setNegativeButton(getString(R.string.dialog_cancel),
                    { dialog, id ->
                        // User cancelled the dialog
                    })
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}