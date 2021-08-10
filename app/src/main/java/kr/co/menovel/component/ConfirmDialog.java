package kr.co.menovel.component;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import kr.co.menovel.R;

public class ConfirmDialog extends Dialog {

    private TextView txtDialogTitle, txtDialogContent;
    private TextView btnDialogCancel, btnDialogConfirm;

    public ConfirmDialog(Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_confirm);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setCancelable(false);

        txtDialogTitle = findViewById(R.id.txt_dialog_title);
        txtDialogContent = findViewById(R.id.txt_dialog_content);
        btnDialogCancel = findViewById(R.id.btn_dialog_cancel);
        btnDialogConfirm = findViewById(R.id.btn_dialog_confirm);

        btnDialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public void hideTitle() {
        txtDialogTitle.setVisibility(View.GONE);
    }

    public void setAlertMode() {
        btnDialogCancel.setVisibility(View.GONE);
    }

    public void setTitle(String title) {
        txtDialogTitle.setText(title);
    }

    public void setTitle(int titleID) {
        txtDialogTitle.setText(titleID);
    }

    public void setContent(String content) {
        txtDialogContent.setText(content);
    }

    public void setContent(int contentID) {
        txtDialogContent.setText(contentID);
    }

    public void setBtnCancelText(String cancelText) {
        btnDialogCancel.setText(cancelText);
    }

    public void setBtnCancelText(int cancelID) {
        btnDialogCancel.setText(cancelID);
    }

    public void setBtnConfirmText(String confirmText) {
        btnDialogConfirm.setText(confirmText);
    }

    public void setBtnConfirmText(int confirmID) {
        btnDialogConfirm.setText(confirmID);
    }

    public void setConfirmListener(View.OnClickListener listener) {
        btnDialogConfirm.setOnClickListener(listener);
    }

    public void setCancelListener(View.OnClickListener listener) {
        btnDialogCancel.setOnClickListener(listener);
    }
}
