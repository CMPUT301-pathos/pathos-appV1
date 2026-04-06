package com.example.eventlottery.util;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

/**
 * Generates QR code Bitmap from a string payload.
 *
 * @author Kenneth Joseph
 * @version 1.0
 */
public class QrCodeGenerator {

    /**
     * Generates a QR code bitmap for the given payload.
     *
     * @param payload string content to encode into the QR code
     * @param sizePx width and height of the resulting bitmap in pixels
     * @return bitmap containing the generated QR code
     * @throws Exception if QR encoding fails
     */
    public static Bitmap generate(String payload, int sizePx) throws Exception {
        BitMatrix matrix = new MultiFormatWriter().encode(payload, BarcodeFormat.QR_CODE, sizePx, sizePx);
        Bitmap bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.RGB_565);

        for (int x = 0; x < sizePx; x++) {
            for (int y = 0; y < sizePx; y++) {
                bmp.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }
        return bmp;
    }
}