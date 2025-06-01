package id.kuato.woahelper.main;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.Objects;

import id.kuato.woahelper.R;
import id.kuato.woahelper.widgets.WidgetActivity;

public class Dlg {
    public static Dialog dialog;
    static MaterialButton yes, no, dismiss;
    static ImageView icon;
    static ProgressBar bar;
    static TextView text;
    private static Context ctx;

    public interface OnButtonClick {
        void execute();
    }

    public static void dialogLoading() {
        setCancelable(false);
        clearButtons();
        setText(ctx.getString(R.string.please_wait));
    }

    public static void show(Context context, String text) {
        if (dialog != null && dialog.isShowing()) { dialog.dismiss(); }
        ctx = context;
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        yes = dialog.findViewById(R.id.yes);
        no = dialog.findViewById(R.id.no);
        dismiss = dialog.findViewById(R.id.dismiss);
        Dlg.text = dialog.findViewById(R.id.messages);
        icon = dialog.findViewById(R.id.icon);
        bar = dialog.findViewById(R.id.progress);

        setText(text);
        setCancelable(true);
        MainActivity.showBlur();
        dialog.setOnDismissListener(v -> {
            MainActivity.hideBlur(true);
            if (WidgetActivity.active) ((AppCompatActivity) MainActivity.context).finish();
        });

        dialog.show();
    }
    public static void show(Context context, @StringRes int stringId) {
        show(context, context.getString(stringId));

    }
    public static void show(Context context, @StringRes int stringId, @DrawableRes int resId) {
        show(context, context.getString(stringId), resId);
    }
    public static void show(Context context, String text, @DrawableRes int resId) {
        show(context, text);
        setIcon(resId);
    }

    public static void close() { dialog.dismiss(); }

    public static void clearButtons() {
        yes.setVisibility(View.GONE);
        no.setVisibility(View.GONE);
        dismiss.setVisibility(View.GONE);
    }

    public static void setCancelable(Boolean state) { dialog.setCancelable(state); }

    public static void dismissButton() { setDismiss(R.string.dismiss, Dlg::close); setCancelable(true);}
    public static void setDismiss(@StringRes int stringId, OnButtonClick onButtonClick) {
        setButton(dismiss, ctx.getString(stringId), onButtonClick);
    }
    public static void setNo(@StringRes int stringId, OnButtonClick onButtonClick) {
        setButton(no, ctx.getString(stringId), onButtonClick);
    }
    public static void setYes(@StringRes int stringId, OnButtonClick onButtonClick) {
        setButton(yes, ctx.getString(stringId), onButtonClick);
    }
    private static void setButton(MaterialButton button, String text, OnButtonClick onButtonClick) {
        button.setVisibility(View.VISIBLE);
        button.setText(text);
        button.setOnClickListener(v -> { onButtonClick.execute(); });
    }
    public static void setText(@StringRes int stringId) {
        setText(ctx.getString(stringId));
    }
    public static void setText(String text) {
        Dlg.text.setText(text);
    }

    public static void setIcon(@DrawableRes int resId) {
        icon.setVisibility(View.VISIBLE);
        icon.setImageResource(resId);
    }
    public static void hideIcon() {
        icon.setVisibility(View.GONE);
    }
    public static void setBar(int progress, boolean animate) {
        bar.setVisibility(View.VISIBLE);
        bar.setProgress(progress, animate);
    }
    public static void setBar(int progress) {
       setBar(progress, true);
    }
    public static void hideBar() {
        bar.setVisibility(View.GONE);
    }
    public static void onCancel(OnButtonClick event) {
        dialog.setOnCancelListener(v -> {
            event.execute();
            close();
        });
    }

}
