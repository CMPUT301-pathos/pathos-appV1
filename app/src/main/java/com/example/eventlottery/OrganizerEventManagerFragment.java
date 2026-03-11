package com.example.eventlottery;

import android.app.AlertDialog;
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
import com.example.eventlottery.domain.WaitStatus;
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
 * - View list of all chosen (invited) entrants
 * - View list of all entrants on the waiting list
 *
 * User stories supported:
 * - US 01.05.01: Another chance to be chosen when someone declines
 * - US 02.02.01: View the list of entrants who joined the event waiting list
 * - US 02.05.02: Sample a specified number of attendees
 * - US 02.05.03: Draw a replacement applicant
 * - US 02.06.01: View a list of all chosen entrants who are invited to apply
 *
 * @author Dmitriy Limanets, Edwin David
 * @version 1.2
 */
public class OrganizerEventManagerFragment extends Fragment {

    private static final String ARG_EVENT_ID = "eventId";
    private static final String ARG_EVENT_NAME = "eventName";

    private String eventId;
    private String eventName;

    private OrganizeLotteryController lotteryController;

    //let other methods access waitlist data
    private FirestoreWaitListRepository waitListRepo;
    private TextView tvEventName, tvDrawStatus;
    private EditText etDrawCount;
    private Button btnRunDraw, btnDrawReplacement, btnViewInvited, btnViewWaiting;

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

        //reused in showEntrantsByStatus()
        waitListRepo = new FirestoreWaitListRepository();

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
        btnViewInvited = root.findViewById(R.id.btn_view_invited);
        btnViewWaiting = root.findViewById(R.id.btn_view_waiting);

        tvEventName.setText(eventName);

        btnRunDraw.setOnClickListener(v -> runInitialDraw());
        btnDrawReplacement.setOnClickListener(v -> runReplacementDraw());

        // US 02.06.01: when clicked displays entrants who are invited for the event
        btnViewInvited.setOnClickListener(v -> showEntrantsByStatus(WaitStatus.INVITED, "Invited Entrants"));

        // US 02.02.01: when clicked displays all entrants who joined the waiting list
        btnViewWaiting.setOnClickListener(v -> showEntrantsByStatus(WaitStatus.WAITING, "Waiting List"));

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

    private void showEntrantsByStatus(WaitStatus status, String title) {
        // Query Firestore
        waitListRepo.getRecordsByStatusAsync(eventId, status,
                new com.example.eventlottery.data.WaitListRepository.WaitListCallBack() {
                    @Override
                    public void onSuccess(java.util.List<com.example.eventlottery.domain.WaitListRecord> records) {

                        // when no entrants match this status
                        if (records.isEmpty()) {
                            new AlertDialog.Builder(requireContext())
                                    .setTitle(title).setMessage("No entrants found.").setPositiveButton("OK", null).show();
                            return;
                        }

                        // string with all device IDs
                        StringBuilder sb = new StringBuilder();
                        for (com.example.eventlottery.domain.WaitListRecord r : records) {
                            sb.append("• ").append(r.getDeviceId()).append("\n");
                        }
                        new AlertDialog.Builder(requireContext())
                                .setTitle(title + " (" + records.size() + ")").setMessage(sb.toString().trim()).setPositiveButton("OK", null).show();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(requireContext(),
                                "Failed to load list: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
