package com.example.eventlottery;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.eventlottery.domain.EventSummary;
import com.example.eventlottery.service.DeviceIdentityService;
import com.example.eventlottery.service.PosterService;
import com.google.android.material.button.MaterialButton;

/**
 * EditEventFragment
 *
 * Allows an organizer to update or remove the poster image for an existing event.
 *
 * Responsibilities:
 * - Display the current event poster
 * - Allow organizer to select a new poster from device gallery
 * - Upload new poster to Firebase Storage and update Firestore
 * - Allow organizer to remove the existing poster
 *
 * User stories supported:
 * - US 02.04.02: Organizer can update an event poster
 *
 * @author Fawaz Mansoor
 * @version 1.0
 * @see PosterService
 */
public class EditEventFragment extends Fragment {

    private static final String ARG_EVENT_ID = "eventId";
    private static final String ARG_EVENT_NAME = "eventName";
    private static final String ARG_POSTER_URL = "posterUrl";

    private String eventId;
    private String eventName;
    private String currentPosterUrl;

    private ImageView ivPosterPreview;
    private MaterialButton btnSelectPoster;
    private MaterialButton btnSavePoster;
    private MaterialButton btnRemovePoster;

    private Uri selectedPosterUri;
    private PosterService posterService;
    private ActivityResultLauncher<PickVisualMediaRequest> pickPosterLauncher;

    public static EditEventFragment newInstance(EventSummary event) {
        EditEventFragment fragment = new EditEventFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, event.getId());
        args.putString(ARG_EVENT_NAME, event.getName());
        args.putString(ARG_POSTER_URL, event.getPosterUrl());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventId = getArguments().getString(ARG_EVENT_ID);
            eventName = getArguments().getString(ARG_EVENT_NAME);
            currentPosterUrl = getArguments().getString(ARG_POSTER_URL);
        }
        posterService = new PosterService();

        pickPosterLauncher = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        selectedPosterUri = uri;
                        ivPosterPreview.setImageURI(uri);
                        btnSavePoster.setEnabled(true);
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_event, container, false);

        ivPosterPreview = view.findViewById(R.id.iv_edit_poster_preview);
        btnSelectPoster = view.findViewById(R.id.btn_edit_select_poster);
        btnSavePoster = view.findViewById(R.id.btn_edit_save_poster);
        btnRemovePoster = view.findViewById(R.id.btn_edit_remove_poster);

        // Load current poster if exists
        if (currentPosterUrl != null && !currentPosterUrl.isEmpty()) {
            Glide.with(this).load(currentPosterUrl).into(ivPosterPreview);
            btnRemovePoster.setVisibility(View.VISIBLE);
        } else {
            btnRemovePoster.setVisibility(View.GONE);
        }

        btnSavePoster.setEnabled(false);

        btnSelectPoster.setOnClickListener(v ->
                pickPosterLauncher.launch(
                        new PickVisualMediaRequest.Builder()
                                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                                .build()
                )
        );

        btnSavePoster.setOnClickListener(v -> savePoster());

        btnRemovePoster.setOnClickListener(v -> removePoster());

        return view;
    }

    private void savePoster() {
        if (selectedPosterUri == null) return;

        btnSavePoster.setEnabled(false);
        btnSavePoster.setText("UPLOADING...");

        String deviceId = DeviceIdentityService.getDeviceId(requireContext());

        posterService.updatePoster(eventId, deviceId, selectedPosterUri,
                new PosterService.PosterCallback() {
                    @Override
                    public void onSuccess(String posterUrl) {
                        if (getActivity() == null) return;
                        Toast.makeText(getContext(), "Poster updated!", Toast.LENGTH_SHORT).show();
                        btnSavePoster.setText("SAVE POSTER");
                        btnSavePoster.setEnabled(false);
                        btnRemovePoster.setVisibility(View.VISIBLE);
                        currentPosterUrl = posterUrl;
                    }

                    @Override
                    public void onFailure(Exception e) {
                        if (getActivity() == null) return;
                        Toast.makeText(getContext(),
                                "Failed to update poster: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                        btnSavePoster.setText("SAVE POSTER");
                        btnSavePoster.setEnabled(true);
                    }
                });
    }

    private void removePoster() {
        btnRemovePoster.setEnabled(false);

        posterService.deletePoster(eventId, new PosterService.PosterCallback() {
            @Override
            public void onSuccess(String posterUrl) {
                if (getActivity() == null) return;
                Toast.makeText(getContext(), "Poster removed.", Toast.LENGTH_SHORT).show();
                ivPosterPreview.setImageResource(android.R.drawable.ic_menu_gallery);
                btnRemovePoster.setVisibility(View.GONE);
                btnSavePoster.setEnabled(false);
                currentPosterUrl = null;
                selectedPosterUri = null;
            }

            @Override
            public void onFailure(Exception e) {
                if (getActivity() == null) return;
                Toast.makeText(getContext(),
                        "Failed to remove poster: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
                btnRemovePoster.setEnabled(true);
            }
        });
    }
}
