package es.fenauso.calculadoraelectrica;

import android.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import java.util.function.Supplier;

final class FieldHelp {
    private FieldHelp() {}

    static void bind(TextView label, View control, String title, Supplier<String> message) {
        if (control.getId() == View.NO_ID) control.setId(View.generateViewId());
        label.setLabelFor(control.getId());
        label.setMinHeight(Math.round(40 * label.getResources().getDisplayMetrics().density));
        label.setGravity(Gravity.CENTER_VERTICAL);
        setTitle(label, title);
        label.setOnClickListener(view -> new AlertDialog.Builder(label.getContext())
                .setTitle((String) label.getTag())
                .setMessage(message.get())
                .setPositiveButton("Entendido", null)
                .show());
    }

    static void setTitle(TextView label, String title) {
        label.setTag(title);
        setCaption(label, title);
    }

    static void setCaption(TextView label, String caption) {
        label.setText(caption + "\u00a0ⓘ");
        label.setContentDescription(caption + ". Ayuda del campo");
    }
}
