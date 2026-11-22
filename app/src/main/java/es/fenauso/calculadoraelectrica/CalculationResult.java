package es.fenauso.calculadoraelectrica;

import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

final class CalculationResult {
    static final int ALERT = 0xffa42318;
    private static final int HEADING = 0xff16324f;

    private CalculationResult() {}

    static CharSequence format(String message) {
        SpannableStringBuilder out = new SpannableStringBuilder();
        for (String raw : message.split("\\n", -1)) {
            String line = raw.trim();
            if (line.isEmpty()) continue;
            if (out.length() > 0) out.append("\n\n");
            int start = out.length();
            if (line.startsWith("# ")) {
                out.append(line.substring(2));
                out.setSpan(new ForegroundColorSpan(HEADING), start, out.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                bold(out, start, out.length());
                continue;
            }
            if (line.startsWith("~ ") || line.startsWith("Pendiente")
                    || line.startsWith("Comprueba") || line.startsWith("Confirma")) {
                out.append(line.startsWith("~ ") ? line.substring(2) : line);
                out.setSpan(new StyleSpan(Typeface.ITALIC), start, out.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                continue;
            }
            boolean alert = line.startsWith("! ");
            if (alert) line = FieldIssues.message(line);
            out.append(line);
            if (alert) {
                out.setSpan(new ForegroundColorSpan(ALERT), start, out.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            int colon = line.indexOf(':');
            if (colon > 0) bold(out, start, start + colon + 1);
        }
        return out;
    }

    private static void bold(SpannableStringBuilder text, int start, int end) {
        text.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
}
