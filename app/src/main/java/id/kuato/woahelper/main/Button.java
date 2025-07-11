package id.kuato.woahelper.main;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.google.android.material.card.MaterialCardView;

import id.kuato.woahelper.R;
import id.kuato.woahelper.databinding.ButtonBinding;

public class Button extends MaterialCardView {
    private final ButtonBinding layout;
    private final Context context;

    public Button(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        layout = ButtonBinding.inflate(LayoutInflater.from(context), this, true);
        this.context = context;
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.Button, 0, 0);
        if (attrs == null) return;
        String title = a.getString(R.styleable.Button_title), subtitle = a.getString(R.styleable.Button_subtitle);
        int imageId = a.getResourceId(R.styleable.Button_image, R.drawable.ic_disk);
        layout.title.setText(title);
        layout.subtitle.setText(subtitle);
        layout.image.setImageResource(imageId);
    }

    public void setTitle(@StringRes int stringId) {
        layout.title.setText(context.getString(stringId));
    }

    public void setTitle(String title) {
        layout.title.setText(title);
    }

    public void setSubtitle(@StringRes int stringId) {
        layout.subtitle.setText(context.getString(stringId));
    }

    public void setSubtitle(String subtitle) {
        layout.subtitle.setText(subtitle);
    }
}
