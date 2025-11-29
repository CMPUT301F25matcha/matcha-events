package com.example.lotterysystemproject.utils;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * Utility class for generating QR codes for event data and check-in purposes.
 */
public class QRCodeGenerator {

    private static final String TAG = "QRCodeGenerator";

    /**
     * Generates a QR code bitmap from the given text.
     *
     * @param text   The text to encode in the QR code.
     * @param width  Desired width of the QR code bitmap.
     * @param height Desired height of the QR code bitmap.
     * @return A Bitmap containing the generated QR code, or null if an error occurs.
     */
    public static Bitmap generateQRCode(String text, int width, int height) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, width, height);
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Generates encoded promotional QR data for an event.
     *
     * @param eventId   The unique ID of the event.
     * @return Encoded promo data string.
     */
    @Nullable
    public static String generatePromoData(String eventId) {
        if (eventId == null || eventId.trim().isEmpty()) {
            Log.e(TAG, "Cannot generate promo data: eventId is null or empty");
            return null;
        }

        // Use custom URI scheme (works immediately)
        return new Uri.Builder()
                .scheme("lotterysystem")
                .authority("event")
                .appendQueryParameter("eventId", eventId)
                .build()
                .toString();
    }

    public static String extractEventId(String raw) {
        try {
            Uri uri = Uri.parse(raw);

            if (!"lotterysystem".equals(uri.getScheme())) {
                return null;
            }

            return uri.getQueryParameter("eventId");
        } catch (Exception e) {
            return null;
        }
    }




}
