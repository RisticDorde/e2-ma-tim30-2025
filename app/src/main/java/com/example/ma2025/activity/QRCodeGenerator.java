package com.example.ma2025.activity;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class QRCodeGenerator {

    /**
     * Generiše QR kod iz teksta
     * @param text Tekst koji treba enkodovati u QR kod
     * @param width Širina QR koda u pikselima
     * @param height Visina QR koda u pikselima
     * @return Bitmap sa QR kodom ili null ako dođe do greške
     */
    public static Bitmap generateQRCode(String text, int width, int height) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

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
     * Generiše QR kod sa default dimenzijama (512x512)
     * @param text Tekst koji treba enkodovati
     * @return Bitmap sa QR kodom
     */
    public static Bitmap generateQRCode(String text) {
        return generateQRCode(text, 512, 512);
    }

    /**
     * Generiše QR kod sa custom bojama
     * @param text Tekst koji treba enkodovati
     * @param width Širina QR koda
     * @param height Visina QR koda
     * @param foregroundColor Boja QR koda (default: crna)
     * @param backgroundColor Boja pozadine (default: bela)
     * @return Bitmap sa QR kodom
     */
    public static Bitmap generateQRCode(String text, int width, int height,
                                        int foregroundColor, int backgroundColor) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? foregroundColor : backgroundColor);
                }
            }

            return bitmap;

        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }
}
