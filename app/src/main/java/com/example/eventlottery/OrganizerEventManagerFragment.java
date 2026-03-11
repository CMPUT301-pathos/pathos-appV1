package com.example.eventlottery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eventlottery.controller.OrganizeLotteryController;
import com.example.eventlottery.domain.WaitListRecord;
import com.example.eventlottery.firebase.FirestoreWaitListRepository;
import com.example.eventlottery.firebase.FirestoreNotificationLogRepository;
import com.example.eventlottery.service.PathosNotifyService;
import com.example.eventlottery.service.PathosRaffleService;

import java.util.List;

/**
 * Fragment for managing lottery draws on a specific event.
 *
 * Responsibilities:
 * - Run initial lottery draw
 * - Draw replacement when entrant declines/cancels
 *
 * User stories supported:
 * - US 01.05.01: Another chance to be chosen when someone declines
 * - US 02.05.02: Sample a specified number of attendees
 * - US 02.05.03: Draw a replacement applicant
 *
 * @author Dmitriy Limanets
 * @version 1.0
 */
public class OrganizerEventManagerFragment extends Fragment {

    private static final String ARG_EVENT_ID = "eventId";
    private static final String ARG_EVENT_NAME = "eventName";

    private String eventId;
    private String eventName;

    private OrganizeLotteryController lotteryController;
    private TextView tvEventName, tvDrawStatus;
    private EditText etDrawCount;
    private Button btnRunDraw, btnDrawReplacement;

    public static OrganizerEventManagerFragment newInstance(String eventId, String eventName) {
        OrganizerEventManagerFragment fragment = new OrganizerEventManagerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        args.putString(ARG_EVENT_NAME, eventName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventId = getArguments().getString(ARG_EVENT_ID);
            eventName = getArguments().getString(ARG_EVENT_NAME);
        }

        FirestoreWaitListRepository waitListRepo = new FirestoreWaitListRepository();
        PathosRaffleService raffleService = new PathosRaffleService(waitListRepo);
        PathosNotifyService notifyService = new PathosNotifyService(new FirestoreNotificationLogRepository());
        lotteryController = new OrganizeLotteryController(raffleService, notifyService);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_organizer_event_manage, container, false);

        tvEventName = root.findViewById(R.id.tv_event_name);
        tvDrawStatus = root.findViewById(R.id.tv_draw_status);
        etDrawCount = root.findViewById(R.id.et_draw_count);
        btnRunDraw = root.findViewById(R.id.btn_run_draw);
        btnDrawReplacement = root.findViewById(R.id.btn_draw_replacement);

        tvEventName.setText(eventName);

        btnRunDraw.setOnClickListener(v -> runInitialDraw());
        btnDrawReplacement.setOnClickListener(v -> runReplacementDraw());

        return root;
    }

    private void runInitialDraw() {
        String countStr = etDrawCount.getText().toString().trim();
        if (countStr.isEmpty()) {
            etDrawCount.setError("Enter a number");
            return;
        }

        int count;
        try {
            count = Integer.parseInt(countStr);
            if (count < 1) {
                etDrawCount.setError("Must be at least 1");
                return;
            }
        } catch (NumberFormatException e) {
            etDrawCount.setError("Invalid number");
            return;
        }

        btnRunDraw.setEnabled(false);
        btnRunDraw.setText("Drawing...");

        lotteryController.runInitialDraw(eventId, eventName, count,
                new OrganizeLotteryController.LotteryCallback() {
                    @Override
                    public void onSuccess(List<WaitListRecord> selected) {
                        btnRunDraw.setEnabled(true);
                        btnRunDraw.setText("Run Initial Draw");
                        tvDrawStatus.setText(selected.size() + " entrant(s) selected and notified");
                    }

                    @Override
                    public void onFailure(Exception e) {
                        btnRunDraw.setEnabled(true);
                        btnRunDraw.setText("Run Initial Draw");
                        Toast.makeText(requireContext(),
                                "Draw failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void runReplacementDraw() {
        btnDrawReplacement.setEnabled(false);
        btnDrawReplacement.setText("Drawing...");

        lotteryController.runReplacementDraw(eventId, eventName,
                new OrganizeLotteryController.LotteryCallback() {
                    @Override
                    public void onSuccess(List<WaitListRecord> selected) {
                        btnDrawReplacement.setEnabled(true);
                        btnDrawReplacement.setText("Draw Replacement");
                        if (selected.isEmpty()) {
                            tvDrawStatus.setText("No waiting entrants available for replacement");
                        } else {
                            tvDrawStatus.setText("1 replacement drawn and notified");
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        btnDrawReplacement.setEnabled(true);
                        btnDrawReplacement.setText("Draw Replacement");
                        Toast.makeText(requireContext(),
                                "Replacement draw failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
