package com.example.eventlottery;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * QrCodeFragment
 *
 * Shows a generated QR code for a given payload.
 * Used by US 02.01.01 after event creation.
 *
 * @author Kenneth Joseph
 * @version 1.0
 */
public class QrCodeFragment extends Fragment {

    private static final String ARG_PAYLOAD = "payload";

    public static QrCodeFragment newInstance(@NonNull String payload) {
        QrCodeFragment f = new QrCodeFragment();
        Bundle b = new Bundle();
        b.putString(ARG_PAYLOAD, payload);
        f.setArguments(b);
        return f;
    }

    public QrCodeFragment() { }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View root = inflater.inflate(R.layout.fragment_qr_code, container, false);

        ImageView qrImage = root.findViewById(R.id.iv_qr);
        TextView tvPayload = root.findViewById(R.id.tv_qr_payload);

        String payload = (getArguments() != null) ? getArguments().getString(ARG_PAYLOAD) : null;
        if (payload == null) payload = "eventId:missing";

        tvPayload.setText(payload);
        qrImage.setImageBitmap(makeQrBitmap(payload, 700));

        return root;
    }

    private Bitmap makeQrBitmap(String text, int sizePx) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, sizePx, sizePx);
            Bitmap bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
            for (int x = 0; x < sizePx; x++) {
                for (int y = 0; y < sizePx; y++) {
                    bmp.setPixel(x, y, matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }
            return bmp;
        } catch (WriterException e) {
            // fallback blank bitmap
            return Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
        }
    }
}