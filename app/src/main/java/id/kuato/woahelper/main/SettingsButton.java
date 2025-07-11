package id.kuato.woahelper.main;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import id.kuato.woahelper.R;
import id.kuato.woahelper.databinding.SettingsButtonBinding;

public class SettingsButton extends FrameLayout {
    private final SettingsButtonBinding layout;
    private final Context context;

    public SettingsButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        layout = SettingsButtonBinding.inflate(LayoutInflater.from(context), this, true);
        this.context = context;
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SettingsButton, 0, 0);
        if (null == attrs) return;
        String text = a.getString(R.styleable.SettingsButton_text);
        layout.text.setText(text);
        setCornerRadius();
        setOnChangeListener((state) -> {
        });
    }

    void setOnChangeListener(OnChangeClickListener onChangeListener) {
        layout.switchButton.setOnCheckedChangeListener((v, state) -> {
            setCornerRadius();
            onChangeListener.onClick(state);
        });
    }

    public void setChecked(boolean state) {
        layout.switchButton.setChecked(state);
    }

    private int getColor() {
        return (layout.switchButton.isChecked()) ? 0xFF196500 : 0xFF870002;
    }

    private void setCornerRadius() {
        GradientDrawable background = new GradientDrawable();
        background.setColor(getColor());
        background.setCornerRadius(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5.0f, context.getResources().getDisplayMetrics()));
        layout.getRoot().setBackground(background);
    }

    public interface OnChangeClickListener {
        void onClick(boolean state);
    }
}
