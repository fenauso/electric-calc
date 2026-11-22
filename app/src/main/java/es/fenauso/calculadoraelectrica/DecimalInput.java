package es.fenauso.calculadoraelectrica;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.widget.EditText;

final class DecimalInput {
    private DecimalInput() {}

    static void configure(EditText input) {
        input.setKeyListener(DigitsKeyListener.getInstance("0123456789.,-"));
        input.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL
                | InputType.TYPE_NUMBER_FLAG_SIGNED);
        addLeadingZero(input);
    }

    static void addLeadingZero(EditText input) {
        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence text, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence text, int start, int before, int count) {}

            @Override public void afterTextChanged(Editable text) {
                int start = text.length() > 0 && text.charAt(0) == '-' ? 1 : 0;
                if (text.length() > start && (text.charAt(start) == '.' || text.charAt(start) == ',')) {
                    int cursor = input.getSelectionStart();
                    text.insert(start, "0");
                    if (cursor >= start) input.setSelection(Math.min(cursor + 1, text.length()));
                }
            }
        });
    }
}
