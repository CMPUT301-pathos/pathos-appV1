package com.example.eventlottery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.FileProvider;

import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.data.WaitListRepository;
import com.example.eventlottery.domain.EventSummary;
import com.example.eventlottery.domain.UserProfile;
import com.example.eventlottery.domain.WaitListRecord;
import com.example.eventlottery.domain.WaitStatus;
import com.example.eventlottery.firebase.FirestoreProfileRepository;
import com.example.eventlottery.firebase.FirestoreWaitListRepository;
import com.example.eventlottery.service.PathosRaffleService;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * OrganizerEventDetailFragment
 *
 * Shows the organizer a list of entrants on the waitlist and accepted entrants for their event.
 * Allows the organizer to send notifications to entrants based on their current waitlist status
 *
 * User stories supported:
 * - US 02.06.01: View a list of all entrants on the waiting list
 * - US 02.06.02: View a list of all entrants who enrolled for the event
 * - US 02.07.01: Send notifications to all entrants on the waiting list
 * - US 02.07.02: Send notifications to all selected entrants
 *
 * @author Fawaz Mansoor, Edwin David
 * @version 1.2
 */
public class OrganizerEventDetailFragment extends Fragment {

    private static final String ARG_EVENT_ID = "eventId";
    private static final String ARG_EVENT_NAME = "eventName";

    private String eventId;
    private String eventName;

    private LinearLayout waitingContainer, invitedContainer, enrolledContainer, cancelledContainer;
    private TextView tvWaitingEmpty, tvInvitedEmpty, tvEnrolledEmpty, tvCancelledEmpty;

    private FirestoreWaitListRepository waitListRepository;
    private FirestoreProfileRepository profileRepository;
    private PathosRaffleService raffleService;

    /**
     * Factory method to create an organizer event detail fragment.
     *
     * @param event the event to display details for
     * @return configured fragment instance
     */
    public static OrganizerEventDetailFragment newInstance(EventSummary event) {
        OrganizerEventDetailFragment fragment = new OrganizerEventDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, event.getId());
        args.putString(ARG_EVENT_NAME, event.getName());
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Initializes fragment arguments and sets up repositories for managing event data.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventId = getArguments().getString(ARG_EVENT_ID);
            eventName = getArguments().getString(ARG_EVENT_NAME);
        }
        waitListRepository = new FirestoreWaitListRepository();
        profileRepository = new FirestoreProfileRepository();
        raffleService = new PathosRaffleService(waitListRepository);
    }

    /**
     * Inflates the organizer event detail layout and sets up entrant management UI.
     *
     * Displays containers for waiting, invited, enrolled, and cancelled entrants,
     * along with buttons for running lotteries, sending notifications, and exporting data.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_organizer_event_detail, container, false);

        TextView tvTitle = view.findViewById(R.id.tv_organizer_event_title);
        MaterialButton btnRunLottery = view.findViewById(R.id.btn_run_lottery);
        btnRunLottery.setOnClickListener(v -> showLotteryDrawDialog());
        waitingContainer = view.findViewById(R.id.container_waiting);
        invitedContainer = view.findViewById(R.id.container_invited);
        enrolledContainer = view.findViewById(R.id.container_enrolled);
        cancelledContainer = view.findViewById(R.id.container_cancelled);
        tvWaitingEmpty = view.findViewById(R.id.tv_waiting_empty);
        tvInvitedEmpty = view.findViewById(R.id.tv_invited_empty);
        tvEnrolledEmpty = view.findViewById(R.id.tv_enrolled_empty);
        tvCancelledEmpty = view.findViewById(R.id.tv_cancelled_empty);

        MaterialButton btnNotifyWaiting = view.findViewById(R.id.btn_notify_waiting);
        btnNotifyWaiting.setOnClickListener(v -> notifyWaitingEntrants());

        MaterialButton btnNotifySelected = view.findViewById(R.id.btn_notify_selected);
        btnNotifySelected.setOnClickListener(v -> notifySelectedEntrants());

        MaterialButton btnCancelAllInvited = view.findViewById(R.id.btn_cancel_all_invited);
        btnCancelAllInvited.setOnClickListener(v -> cancelAllInvited());

        MaterialButton btnExportCsv = view.findViewById(R.id.btn_export_csv);
        btnExportCsv.setOnClickListener(v -> exportEnrolledCsv());

        tvTitle.setText(eventName);

        loadEntrants();

        return view;
    }

    // US 02.07.01- Send a notification to all entrants currently on the waiting list
    /**
     * Sends a notification to all entrants currently on the waiting list.
     */
    private void notifyWaitingEntrants() {

       //get all waitlist records with status = WAITING
        waitListRepository.getRecordsByStatusAsync(eventId, WaitStatus.WAITING,
                new WaitListRepository.WaitListCallBack() {
                    @Override
                    public void onSuccess(List<WaitListRecord> records) {
                        if (getActivity() == null) return;
                        if (records.isEmpty()) {
                            Toast.makeText(getContext(), "No entrants on the waiting list.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // confirmation before sending notifications

                        new android.app.AlertDialog.Builder(requireContext())
                                .setTitle("Notify Waiting Entrants")
                                .setMessage("Send a notification to " + records.size() + " entrant(s) on the waiting list?")
                                .setPositiveButton("Send", (dialog, which) -> {
                                    for (WaitListRecord record : records) {

                                        // notification data to store in Firestore
                                        java.util.Map<String, Object> notifData = new java.util.HashMap<>();
                                        notifData.put("deviceId", record.getDeviceId());
                                        notifData.put("eventId", eventId);
                                        notifData.put("type", "WAITING");
                                        notifData.put("message", "You are on the waiting list for: " + eventName);
                                        notifData.put("timestamp", System.currentTimeMillis());
                                        notifData.put("read", false);

                                        // Add to firestore
                                        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                                .collection("notifications")
                                                .add(notifData);
                                    }
                                    Toast.makeText(getContext(),
                                            records.size() + " entrant(s) notified.", Toast.LENGTH_SHORT).show();
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        //  fragment is detached, stop
                        if (getActivity() == null) return;
                        Toast.makeText(getContext(), "Failed to load waiting entrants.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // US 02.07.02 -  Send a notification to all selected entrants
    /**
     * Sends a notification to all selected (invited) entrants.
     */
    private void notifySelectedEntrants() {
        waitListRepository.getRecordsByStatusAsync(eventId, WaitStatus.INVITED,
                new WaitListRepository.WaitListCallBack() {
                    @Override
                    public void onSuccess(List<WaitListRecord> records) {
                        if (getActivity() == null) return;
                        if (records.isEmpty()) {
                            Toast.makeText(getContext(), "No selected entrants to notify.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Show confirmation
                        new android.app.AlertDialog.Builder(requireContext())
                                .setTitle("Notify Selected Entrants")
                                .setMessage("Send a notification to " + records.size() + " selected entrant(s)?")
                                .setPositiveButton("Send", (dialog, which) -> {
                                    for (WaitListRecord record : records) {
                                        java.util.Map<String, Object> notifData = new java.util.HashMap<>();
                                        notifData.put("deviceId", record.getDeviceId());
                                        notifData.put("eventId", eventId);
                                        notifData.put("type", "INVITED");
                                        notifData.put("message", "You have been selected for: " + eventName + ". Please accept or decline your invitation.");
                                        notifData.put("timestamp", System.currentTimeMillis());
                                        notifData.put("read", false);
                                        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                                .collection("notifications")
                                                .add(notifData);
                                    }
                                    Toast.makeText(getContext(),
                                            records.size() + " entrant(s) notified.", Toast.LENGTH_SHORT).show();
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        if (getActivity() == null) return;
                        Toast.makeText(getContext(), "Failed to load selected entrants.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Marks all invited entrants as cancelled and sends them cancellation notifications.
     */
    private void cancelAllInvited() {
        waitListRepository.getRecordsByStatusAsync(eventId, WaitStatus.INVITED,
                new WaitListRepository.WaitListCallBack() {
                    @Override
                    public void onSuccess(List<WaitListRecord> records) {
                        if (getActivity() == null) return;
                        if (records.isEmpty()) {
                            Toast.makeText(getContext(), "No invited entrants to cancel.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        new android.app.AlertDialog.Builder(requireContext())
                                .setTitle("Cancel All Invited")
                                .setMessage("Cancel " + records.size() + " invited entrant(s) who haven't signed up? They will be notified.")
                                .setPositiveButton("Cancel All", (dialog, which) -> {
                                    for (WaitListRecord record : records) {
                                        waitListRepository.updateStatus(eventId, record.getDeviceId(), WaitStatus.CANCELLED);

                                        java.util.Map<String, Object> notifData = new java.util.HashMap<>();
                                        notifData.put("deviceId", record.getDeviceId());
                                        notifData.put("eventId", eventId);
                                        notifData.put("type", "CANCELLED");
                                        notifData.put("message", "Your invitation to " + eventName + " has been cancelled.");
                                        notifData.put("timestamp", System.currentTimeMillis());
                                        notifData.put("read", false);

                                        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                                .collection("notifications")
                                                .add(notifData);
                                    }

                                    Toast.makeText(getContext(),
                                            records.size() + " entrant(s) cancelled and notified.",
                                            Toast.LENGTH_SHORT).show();
                                    loadEntrants();
                                })
                                .setNegativeButton("Back", null)
                                .show();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        if (getActivity() == null) return;
                        Toast.makeText(getContext(), "Failed to load invited entrants.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Exports the list of enrolled entrants as a CSV file and shares it with the organizer.
     */
    private void exportEnrolledCsv() {
        waitListRepository.getRecordsByStatusAsync(eventId, WaitStatus.ACCEPTED,
                new WaitListRepository.WaitListCallBack() {
                    @Override
                    public void onSuccess(List<WaitListRecord> records) {
                        if (getActivity() == null) return;
                        if (records.isEmpty()) {
                            Toast.makeText(getContext(), "No enrolled entrants to export.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Look up profiles for all enrolled entrants
                        List<String[]> rows = new ArrayList<>();
                        final int[] remaining = {records.size()};

                        for (WaitListRecord record : records) {
                            profileRepository.getProfile(record.getDeviceId(), new ProfileRepository.ProfileCallback() {
                                @Override
                                public void onSuccess(UserProfile profile) {
                                    if (getActivity() == null) return;
                                    String name = "";
                                    String email = "";
                                    String phone = "";

                                    if (profile != null) {
                                        name = profile.getName() != null ? profile.getName() : "";
                                        email = profile.getEmail() != null ? profile.getEmail() : "";
                                        phone = profile.getPhoneNumber() != null ? profile.getPhoneNumber() : "";
                                    }

                                    rows.add(new String[]{name, email, phone, record.getDeviceId()});
                                    remaining[0]--;

                                    if (remaining[0] == 0) {
                                        writeCsvAndShare(rows);
                                    }
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    if (getActivity() == null) return;
                                    rows.add(new String[]{"", "", "", record.getDeviceId()});
                                    remaining[0]--;

                                    if (remaining[0] == 0) {
                                        writeCsvAndShare(rows);
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        if (getActivity() == null) return;
                        Toast.makeText(getContext(), "Failed to load enrolled entrants.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Writes the provided CSV rows to a file and shares it via the system
     * share sheet for email or file transfer.
     *
     * @param rows list of string arrays representing CSV rows
     */
    private void writeCsvAndShare(List<String[]> rows) {
        try {
            File cachePath = new File(requireContext().getCacheDir(), "csv_exports");
            if (!cachePath.exists()) cachePath.mkdirs();

            String fileName = eventName.replaceAll("[^a-zA-Z0-9]", "_") + "_enrolled.csv";
            File csvFile = new File(cachePath, fileName);

            FileWriter writer = new FileWriter(csvFile);
            writer.append("Name,Email,Phone,Device ID\n");

            for (String[] row : rows) {
                writer.append(escapeCsv(row[0])).append(",");
                writer.append(escapeCsv(row[1])).append(",");
                writer.append(escapeCsv(row[2])).append(",");
                writer.append(escapeCsv(row[3])).append("\n");
            }

            writer.flush();
            writer.close();

            android.net.Uri uri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    csvFile);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/csv");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, eventName + " — Enrolled Entrants");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent, "Export Enrolled List"));

        } catch (IOException e) {
            Toast.makeText(getContext(), "Failed to create CSV file.", Toast.LENGTH_SHORT).show();
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
    /**
     * Loads all entrants for this event and organizes them by status into
     * separate containers for display.
     */
    private void loadEntrants() {
        waitListRepository.getRecordsByEventAsync(eventId, new WaitListRepository.WaitListCallBack() {
            @Override
            public void onSuccess(List<WaitListRecord> records) {
                if (getActivity() == null) return;

                waitingContainer.removeAllViews();
                invitedContainer.removeAllViews();
                enrolledContainer.removeAllViews();
                cancelledContainer.removeAllViews();


                int waitingCount = 0;
                int invitedCount = 0;
                int enrolledCount = 0;
                int cancelledCount = 0;

                for (WaitListRecord record : records) {
                    switch (record.getStatus()) {
                        case WAITING:
                            waitingCount++;
                            addEntrantRow(waitingContainer, record.getDeviceId(), record.getStatus());
                            break;
                        case INVITED:
                            invitedCount++;
                            addEntrantRow(invitedContainer, record.getDeviceId(), record.getStatus());
                            break;
                        case ACCEPTED:
                            enrolledCount++;
                            addEntrantRow(enrolledContainer, record.getDeviceId(), record.getStatus());
                            break;
                        case CANCELLED:
                        case DECLINED:
                            cancelledCount++;
                            addEntrantRow(cancelledContainer, record.getDeviceId(), record.getStatus());
                            break;
                    }
                }

                tvWaitingEmpty.setVisibility(waitingCount == 0 ? View.VISIBLE : View.GONE);
                tvInvitedEmpty.setVisibility(invitedCount == 0 ? View.VISIBLE : View.GONE);
                tvEnrolledEmpty.setVisibility(enrolledCount == 0 ? View.VISIBLE : View.GONE);
                tvCancelledEmpty.setVisibility(cancelledCount == 0 ? View.VISIBLE : View.GONE);

            }

            @Override
            public void onFailure(Exception e) {
                if (getActivity() == null) return;
                Toast.makeText(getContext(), "Failed to load entrants", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Creates and displays an entrant row in the appropriate status container.
     *
     * Fetches the entrant's profile and displays their name along with status
     * and a remove button (if applicable).
     *
     * @param container the layout container to add the row to
     * @param deviceId device id of the entrant
     * @param status current waitlist status of the entrant
     */
    private void addEntrantRow(LinearLayout container, String deviceId, WaitStatus status) {
        View row = LayoutInflater.from(getContext())
                .inflate(R.layout.item_entrant_row, container, false);

        TextView tvName = row.findViewById(R.id.tv_entrant_name);
        TextView tvStatus = row.findViewById(R.id.tv_entrant_status);
        MaterialButton btnRemove = row.findViewById(R.id.btn_remove_entrant);

        tvName.setText(deviceId);
        tvStatus.setText(status.name());

        container.addView(row);

        profileRepository.getProfile(deviceId, new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(UserProfile profile) {
                if (getActivity() == null) return;
                if (profile != null && profile.getName() != null && !profile.getName().isEmpty()) {
                    tvName.setText(profile.getName());
                }
            }

            @Override
            public void onFailure(Exception e) { }
        });

        if (status == WaitStatus.WAITING || status == WaitStatus.INVITED) {
            btnRemove.setVisibility(View.VISIBLE);
            btnRemove.setOnClickListener(v -> {
                new android.app.AlertDialog.Builder(requireContext())
                        .setTitle("Remove Entrant")
                        .setMessage("Remove " + tvName.getText() + " from the waitlist? They will be notified.")
                        .setPositiveButton("Remove", (dialog, which) -> {
                            removeEntrant(container, row, deviceId);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        } else {
            btnRemove.setVisibility(View.GONE);
        }
    }

    /**
     * Removes an entrant from the event by marking them as cancelled
     * and sending them a cancellation notification.
     *
     * @param container the layout container holding the entrant row
     * @param row the entrant row view to remove from display
     * @param deviceId device id of the entrant being removed
     */
    private void removeEntrant(LinearLayout container, View row, String deviceId) {
        waitListRepository.updateStatus(eventId, deviceId, WaitStatus.CANCELLED);

        java.util.Map<String, Object> notifData = new java.util.HashMap<>();
        notifData.put("deviceId", deviceId);
        notifData.put("eventId", eventId);
        notifData.put("type", "CANCELLED");
        notifData.put("message", "You have been removed from the waitlist for: " + eventName);
        notifData.put("timestamp", System.currentTimeMillis());
        notifData.put("read", false);

        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("notifications")
                .add(notifData)
                .addOnSuccessListener(ref -> {
                    if (getActivity() == null) return;
                    Toast.makeText(getContext(),
                            "Entrant removed and notified.",
                            Toast.LENGTH_SHORT).show();
                    loadEntrants();    // ← reload all sections
                })
                .addOnFailureListener(e -> {
                    if (getActivity() == null) return;
                    Toast.makeText(getContext(),
                            "Removed but failed to send notification.",
                            Toast.LENGTH_SHORT).show();
                    loadEntrants();    // ← reload all sections
                });
    }

    /**
     * Shows a dialog allowing the organizer to enter the number of entrants
     * to draw for a lottery, then runs the initial lottery draw.
     */
    private void showLotteryDrawDialog() {
        android.widget.EditText etCount = new android.widget.EditText(requireContext());
        etCount.setHint("Number of entrants to draw");
        etCount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etCount.setPadding(48, 32, 48, 16);

        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Run Lottery Draw")
                .setMessage("How many entrants do you want to invite?")
                .setView(etCount)
                .setPositiveButton("Draw", (dialog, which) -> {
                    String input = etCount.getText().toString().trim();
                    if (input.isEmpty()) {
                        Toast.makeText(getContext(), "Enter a number", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int count = Integer.parseInt(input);
                    runLotteryDraw(count);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Runs a lottery draw selecting the specified number of entrants from
     * the waiting list and marking them as invited.
     *
     * @param count number of entrants to select
     */
    private void runLotteryDraw(int count) {
        raffleService.drawInitial(eventId, count, new PathosRaffleService.RaffleCallback() {
            @Override
            public void onDrawComplete(List<WaitListRecord> selected) {
                if (getActivity() == null) return;
                if (selected.isEmpty()) {
                    Toast.makeText(getContext(),
                            "No waiting entrants to draw from.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(),
                            selected.size() + " entrant(s) invited!",
                            Toast.LENGTH_SHORT).show();
                }
                loadEntrants();
            }

            @Override
            public void onFailure(Exception e) {
                if (getActivity() == null) return;
                Toast.makeText(getContext(),
                        "Draw failed: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}