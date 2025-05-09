package com.example.doit;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class DeleteNoteDialog extends DialogFragment {

    public interface DeleteFunction {
        void onDelete();
    }

    private DeleteFunction deleteFunction;

    public DeleteNoteDialog() {}

    public DeleteNoteDialog(DeleteFunction deleteFunction) {
        this.deleteFunction = deleteFunction;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.are_u_sure)
                .setPositiveButton(R.string.delete, (dialogInterface, i) -> {
                    if (deleteFunction != null) deleteFunction.onDelete();
                    else dialogInterface.cancel();
                })
                .setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.cancel())
                .create();
    }
}
