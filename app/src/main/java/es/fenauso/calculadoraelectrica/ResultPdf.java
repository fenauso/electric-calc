package es.fenauso.calculadoraelectrica;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.text.StaticLayout;
import android.text.TextPaint;

import java.io.IOException;
import java.io.OutputStream;

final class ResultPdf {
    private static final int WIDTH = 595;
    private static final int HEIGHT = 842;
    private static final int MARGIN = 46;
    private static final int CONTENT_TOP = 105;
    private static final int CONTENT_BOTTOM = HEIGHT - 58;
    private static final int NAVY = 0xff16324f;

    private ResultPdf() {}

    static void write(OutputStream output, String title, CharSequence result) throws IOException {
        TextPaint bodyPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        bodyPaint.setTextSize(11);
        bodyPaint.setColor(0xff182230);
        StaticLayout body = StaticLayout.Builder.obtain(
                result, 0, result.length(), bodyPaint, WIDTH - 2 * MARGIN).build();

        PdfDocument pdf = new PdfDocument();
        try {
            int pageNumber = 0;
            for (int first = 0; first < body.getLineCount();) {
                int startY = body.getLineTop(first);
                int next = first + 1;
                while (next < body.getLineCount()
                        && body.getLineTop(next + 1) - startY <= CONTENT_BOTTOM - CONTENT_TOP) {
                    next++;
                }

                PdfDocument.Page page = pdf.startPage(new PdfDocument.PageInfo.Builder(WIDTH, HEIGHT, ++pageNumber).create());
                try {
                    Canvas canvas = page.getCanvas();
                    drawPageFrame(canvas, title, pageNumber);
                    int endY = body.getLineTop(next);
                    canvas.clipRect(MARGIN, CONTENT_TOP, WIDTH - MARGIN, CONTENT_TOP + endY - startY);
                    canvas.translate(MARGIN, CONTENT_TOP - startY);
                    body.draw(canvas);
                } finally {
                    pdf.finishPage(page);
                }
                first = next;
            }
            pdf.writeTo(output);
        } finally {
            pdf.close();
        }
    }

    private static void drawPageFrame(Canvas canvas, String title, int pageNumber) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        canvas.drawColor(Color.WHITE);
        paint.setColor(NAVY);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextSize(10);
        canvas.drawText("Cálculo Eléctrico PRO", MARGIN, 49, paint);
        paint.setTextSize(18);
        canvas.drawText(title, MARGIN, 76, paint);
        paint.setColor(0xffcdd8e4);
        canvas.drawLine(MARGIN, 89, WIDTH - MARGIN, 89, paint);
        canvas.drawLine(MARGIN, HEIGHT - 48, WIDTH - MARGIN, HEIGHT - 48, paint);
        paint.setColor(NAVY);
        paint.setTypeface(Typeface.DEFAULT);
        paint.setTextSize(9);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("Página " + pageNumber, WIDTH - MARGIN, HEIGHT - 33, paint);
    }
}
