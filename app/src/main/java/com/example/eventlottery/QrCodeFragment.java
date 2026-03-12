package com.example.eventlottery;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * QrCodeFragment
 *
 * Shows a generated QR code for a given payload.
 * Used by US 02.01.01 after event creation.
 * Responsibilities:
 * - Generate and display QR code for a payload (e.g., event ID).
 * - Show the payload text under the QR code.
 * - Allow user to share the QR code image via Android sharing intents.
 * @author Kenneth Joseph
 * @version 1.1
 * @see: fragment_qr_code.xml, CreateEventFragment.java
 */

public class QrCodeFragment extends Fragment {

    private static final String ARG_PAYLOAD = "payload";

    private String payload;
    private Bitmap qrBitmap;

    public QrCodeFragment() { }

    /**
     * Factory method to create a new instance of QrCodeFragment.
     *
     * @param payload The string to encode as a QR code
     * @return QrCodeFragment instance with arguments set
     */
    public static QrCodeFragment newInstance(String payload) {
        QrCodeFragment fragment = new QrCodeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PAYLOAD, payload);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Lifecycle method called to inflate fragment layout and initialize UI.
     *
     * Inflates layout, binds views, generates QR code bitmap, and wires share button.
     *
     * @param inflater LayoutInflater to inflate views
     * @param container Parent view container
     * @param savedInstanceState Optional saved state
     * @return Inflated root view
     */
    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View root = inflater.inflate(R.layout.fragment_qr_code, container, false);

        ImageView ivQr = root.findViewById(R.id.iv_qr_code);
        TextView tvPayload = root.findViewById(R.id.tv_qr_payload);
        MaterialButton btnShareQr = root.findViewById(R.id.btn_share_qr);

        if (getArguments() != null) {
            payload = getArguments().getString(ARG_PAYLOAD, "");
        } else {
            payload = "";
        }

        tvPayload.setText(payload);

        try {
            qrBitmap = com.example.eventlottery.util.QrCodeGenerator.generate(payload, 800);
            ivQr.setImageBitmap(qrBitmap);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Failed to generate QR code", Toast.LENGTH_LONG).show();
        }

        btnShareQr.setOnClickListener(v -> shareQrCode());

        return root;
    }

    /**
     * Shares the generated QR code image via Android sharing intents.
     *
     * Saves the QR code bitmap to a temporary file and launches a chooser to share it.
     * Handles exceptions and shows a Toast if sharing fails.
     */
    private void shareQrCode() {
        if (qrBitmap == null) {
            Toast.makeText(requireContext(), "QR code not available", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File cachePath = new File(requireContext().getCacheDir(), "shared_qr");
            if (!cachePath.exists()) {
                cachePath.mkdirs();
            }

            File file = new File(cachePath, "event_qr.png");
            FileOutputStream stream = new FileOutputStream(file);
            qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.flush();
            stream.close();

            android.net.Uri contentUri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    file
            );

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/png");
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Scan this QR code to view the event.");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent, "Share QR Code"));
        } catch (IOException e) {
            Toast.makeText(requireContext(), "Failed to share QR code", Toast.LENGTH_LONG).show();
        }
    }
}