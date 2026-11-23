package es.fenauso.calculadoraelectrica;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

abstract class PdfResultActivity extends Activity {
    private static final int CREATE_PDF = 410;
    private static final String PENDING_PDF = "pendingResultPdf";
    private Bundle pendingPdf;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (state != null) pendingPdf = state.getBundle(PENDING_PDF);
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putBundle(PENDING_PDF, pendingPdf);
    }

    final void saveResultPdf(String title, CharSequence result) {
        pendingPdf = new Bundle();
        pendingPdf.putString("title", title);
        pendingPdf.putCharSequence("result", result);
        Intent chooseFile = new Intent(Intent.ACTION_CREATE_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType("application/pdf")
                .putExtra(Intent.EXTRA_TITLE, "Resultado - " + title + ".pdf");
        try {
            startActivityForResult(chooseFile, CREATE_PDF);
        } catch (ActivityNotFoundException e) {
            pendingPdf = null;
            Toast.makeText(this, "No se encontró un selector para guardar el PDF.", Toast.LENGTH_LONG).show();
        }
    }

    @Override protected void onActivityResult(int request, int status, Intent data) {
        super.onActivityResult(request, status, data);
        if (request != CREATE_PDF) return;
        Bundle report = pendingPdf;
        pendingPdf = null;
        Uri destination = data == null ? null : data.getData();
        if (status != RESULT_OK || destination == null || report == null) return;

        ContentResolver resolver = getApplicationContext().getContentResolver();
        new Thread(() -> {
            try {
                ByteArrayOutputStream pdf = new ByteArrayOutputStream();
                ResultPdf.write(pdf, report.getString("title"), report.getCharSequence("result"));
                try (OutputStream output = resolver.openOutputStream(destination, "wt")) {
                    if (output == null) throw new IOException("No se pudo abrir el destino");
                    pdf.writeTo(output);
                }
                showPdfStatus("PDF guardado.");
            } catch (IOException | SecurityException e) {
                showPdfStatus("No se pudo guardar el PDF. Elige otro destino y vuelve a intentarlo.");
            }
        }, "guardar-resultado-pdf").start();
    }

    private void showPdfStatus(String message) {
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show());
    }
}
