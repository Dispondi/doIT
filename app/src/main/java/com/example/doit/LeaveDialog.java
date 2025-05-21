package com.example.doit;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class LeaveDialog extends DialogFragment {

    public interface LeaveFunction {
        void onYes();
    }

    private LeaveDialog.LeaveFunction leaveFunction;

    public LeaveDialog() {}

    public LeaveDialog(LeaveDialog.LeaveFunction leaveFunction) {
        this.leaveFunction = leaveFunction;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.are_u_sure)
                .setMessage("Приложение будет закрыто")
                .setPositiveButton("Да", (dialogInterface, i) -> {
                    if (leaveFunction != null) leaveFunction.onYes();
                    else dialogInterface.cancel();
                })
                .setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.cancel())
                .create();
    }
}
