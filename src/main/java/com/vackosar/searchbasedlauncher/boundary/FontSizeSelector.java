package com.vackosar.searchbasedlauncher.boundary;

import android.app.Activity;
import android.content.Intent;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.annotations.Expose;
import com.google.inject.Inject;
import com.vackosar.searchbasedlauncher.R;
import com.vackosar.searchbasedlauncher.entity.FontSize;
import com.vackosar.searchbasedlauncher.entity.SelectAction;

import roboguice.context.event.OnCreateEvent;
import roboguice.event.Observes;
import roboguice.inject.ContextSingleton;
import roboguice.inject.InjectView;

@ContextSingleton
public class FontSizeSelector extends SelectAction<FontSize> {

    public static final FontSize DEFAULT = FontSize.pt9;

    @Inject private Activity activity;
    @Expose private FontSize fontSize = DEFAULT;
    @InjectView(R.id.viewAnimator) private ViewGroup viewGroup;

    @SuppressWarnings("unused")
    public void onCreate(@Observes OnCreateEvent onCreate) {
        load();
        setSizeToTextChilds(viewGroup);
    }

    @Override
    protected Enum<FontSize> getSelected() {
        return fontSize;
    }

    @Override
    public void setSelected(Enum<FontSize> selected) {
        this.fontSize = (FontSize) selected;
        save();
        activity.finish();
        activity.startActivity(new Intent(activity, activity.getClass()));
    }

    @Override
    public String getId() {
        return getClass().getName();
    }

    @Override
    public String getName() {
        return "Text Size";
    }

    public void setSize(TextView textView) {
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PT, getSize());
    }

    public int getSize() {
        return fontSize.getSize();
    }

    public void setSizeToTextChilds(ViewGroup parent) {
        for (int i = parent.getChildCount() - 1; i >= 0; i--) {
            final View child = parent.getChildAt(i);
            if (child instanceof ViewGroup) {
                setSizeToTextChilds((ViewGroup) child);
            } else {
                if (child != null) {
                    if (child instanceof TextView) {
                        setSize((TextView) child);
                    }
                }
            }
        }
    }
}