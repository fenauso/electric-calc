package es.fenauso.calculadoraelectrica;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import java.util.function.Consumer;

final class CalculatorTabs {
    private static final int NAVY = 0xff16324f;

    private CalculatorTabs() {}

    static LinearLayout tools(Activity activity, Runnable showHelp) {
        LinearLayout row = new LinearLayout(activity);
        LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(-1, -2);
        layout.topMargin = dp(activity, 8);
        row.setLayoutParams(layout);
        addTool(activity, row, "ⓘ AYUDA", showHelp, dp(activity, 5));
        addTool(activity, row, "Glosario", () -> Glossary.show(activity), 0);
        return row;
    }

    private static void addTool(Activity activity, LinearLayout row, String caption, Runnable action, int marginEnd) {
        Button button = new Button(activity);
        button.setAllCaps(false);
        button.setText(caption);
        button.setTextSize(13);
        button.setTextColor(activity.getColor(R.color.blue));
        button.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);
        button.setGravity(Gravity.CENTER);
        button.setPadding(dp(activity, 8), dp(activity, 11), dp(activity, 8), dp(activity, 11));
        GradientDrawable background = new GradientDrawable();
        background.setColor(0xfff5f8ff);
        background.setCornerRadius(dp(activity, 10));
        button.setBackground(background);
        button.setOnClickListener(view -> action.run());
        LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(0, dp(activity, 48), 1);
        layout.setMarginEnd(marginEnd);
        row.addView(button, layout);
    }

    static View surround(Activity activity, View content, ProModule selected, Consumer<ProModule> onSelection) {
        LinearLayout screen = new LinearLayout(activity);
        screen.setOrientation(LinearLayout.VERTICAL);
        screen.setBackgroundColor(0xfff7f9fc);
        screen.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            return insets.consumeSystemWindowInsets();
        });

        HorizontalScrollView strip = new HorizontalScrollView(activity);
        strip.setHorizontalScrollBarEnabled(true);
        strip.setScrollbarFadingEnabled(false);
        strip.setBackgroundColor(Color.WHITE);
        LinearLayout tabs = new LinearLayout(activity);
        tabs.setGravity(Gravity.CENTER_VERTICAL);
        tabs.setPadding(dp(activity, 8), dp(activity, 4), dp(activity, 8), dp(activity, 5));
        strip.addView(tabs);
        View active = addTab(activity, tabs, "Conductores", "Calculadora de conductores y protección",
                selected == null, () -> onSelection.accept(null));
        for (ProModule module : ProModule.values()) {
            boolean isSelected = module == selected;
            View tab = addTab(activity, tabs, title(module), module.title, isSelected, () -> onSelection.accept(module));
            if (isSelected) active = tab;
        }
        View selectedTab = active;
        strip.post(() -> strip.scrollTo(selectedTab.getLeft() - (strip.getWidth() - selectedTab.getWidth()) / 2, 0));

        screen.addView(strip, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        screen.addView(content, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        return screen;
    }

    private static View addTab(Activity activity, LinearLayout row, String label, String fullLabel,
                               boolean selected, Runnable action) {
        Button button = new Button(activity);
        button.setAllCaps(false);
        button.setTextSize(14);
        button.setText(label);
        button.setContentDescription(fullLabel);
        button.setSelected(selected);
        button.setSingleLine(true);
        button.setTextColor(selected ? Color.WHITE : NAVY);
        button.setPadding(dp(activity, 14), 0, dp(activity, 14), 0);
        GradientDrawable background = new GradientDrawable();
        background.setColor(selected ? NAVY : 0xffeef3f9);
        background.setCornerRadius(dp(activity, 10));
        button.setBackground(background);
        if (!selected) button.setOnClickListener(view -> action.run());
        LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(activity, 48));
        layout.setMarginEnd(dp(activity, 5));
        row.addView(button, layout);
        return button;
    }

    private static String title(ProModule module) {
        return switch (module) {
            case RESISTANCE -> "Resistencias";
            case REACTIVE -> "Reactiva";
            case CONDUIT -> "Tubos";
            case ENERGY -> "Consumo";
            case PV_PROJECT -> "Proyecto FV";
            case SOLAR -> "Strings FV";
            case PV_PROTECTION -> "Protecciones FV";
            case DC -> "Cable CC";
            case BATTERY -> "Baterías";
            case EV -> "Recarga VE";
        };
    }

    private static int dp(Activity activity, int value) {
        return Math.round(value * activity.getResources().getDisplayMetrics().density);
    }
}
