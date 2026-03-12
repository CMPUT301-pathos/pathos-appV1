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

    /**
     * Factory method to create a new instance of this fragment for a specific event.
     *
     * @param eventId Event identifier
     * @param eventName Event name
     * @return A new instance of OrganizerEventManagerFragment
     */
    public static OrganizerEventManagerFragment newInstance(String eventId, String eventName) {
        OrganizerEventManagerFragment fragment = new OrganizerEventManagerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        args.putString(ARG_EVENT_NAME, eventName);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Lifecycle method called when the fragment is created.
     *
     * Initializes:
     * - Event arguments (ID and name)
     * - Lottery controller with dependencies (waitlist repo, raffle service, notification service)
     *
     * @param savedInstanceState Optional saved state
     */
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

    /**
     * Lifecycle method called to inflate the fragment's UI.
     *
     * Initializes views, sets the event name, and hooks up draw buttons.
     *
     * @param inflater LayoutInflater to inflate views
     * @param container Parent container
     * @param savedInstanceState Optional saved state
     * @return Inflated root view
     */
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

    /**
     * Executes the initial lottery draw for the event.
     *
     * - Reads the number of entrants from {@link #etDrawCount}.
     * - Validates input.
     * - Disables the run button and shows "Drawing..." status.
     * - Calls {@link OrganizeLotteryController#runInitialDraw} to select entrants.
     * - Updates {@link #tvDrawStatus} and re-enables button on completion.
     */
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

    /**
     * Executes a replacement draw for the event.
     *
     * - Disables the replacement draw button and shows "Drawing..." status.
     * - Calls {@link OrganizeLotteryController#runReplacementDraw}.
     * - Updates {@link #tvDrawStatus} and re-enables the button on completion.
     */
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
