package com.example.eventlottery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.data.WaitListRepository;
import com.example.eventlottery.domain.EventSummary;
import com.example.eventlottery.domain.UserProfile;
import com.example.eventlottery.domain.WaitListRecord;
import com.example.eventlottery.domain.WaitStatus;
import com.example.eventlottery.firebase.FirestoreEventRepository;
import com.example.eventlottery.firebase.FirestoreProfileRepository;
import com.example.eventlottery.firebase.FirestoreWaitListRepository;
import com.example.eventlottery.service.DeviceIdentityService;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * DashboardFragment
 *
 * Entrant landing page. Shows the AI assistant entry card at the top,
 * followed by upcoming accepted events and current waitlist entries.
 *
 * User stories supported:
 * - US 01.02.03: View history of events registered for
 * - US 01.05.04: View waiting list counts
 *
 * @author Kenneth Joseph, Fawaz Mansoor
 * @version 2.2
 * @see FirestoreWaitListRepository
 * @see AiAssistantFragment
 */
public class DashboardFragment extends Fragment {

    private LinearLayout containerUpcomingEvents;
    private LinearLayout containerWaitlist;
    private TextView     tvUpcomingEmpty;
    private TextView     tvWaitlistEmpty;

    private FirestoreWaitListRepository waitListRepo;
    private FirestoreEventRepository    eventRepo;
    private String                      deviceId;

    public DashboardFragment() {}

    /**
     * Inflates the dashboard fragment layout.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    /**
     * Initializes dashboard UI state and loads the current user's profile and events.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        containerUpcomingEvents = view.findViewById(R.id.containerUpcomingEvents);
        containerWaitlist       = view.findViewById(R.id.containerWaitlist);
        tvUpcomingEmpty         = view.findViewById(R.id.tvUpcomingEmpty);
        tvWaitlistEmpty         = view.findViewById(R.id.tvWaitlistEmpty);

        deviceId     = DeviceIdentityService.getDeviceId(requireContext());
        waitListRepo = new FirestoreWaitListRepository();
        eventRepo    = new FirestoreEventRepository();

        // ── Profile photo ──────────────────────────────────────────
        de.hdodenhof.circleimageview.CircleImageView ivProfilePhoto =
                view.findViewById(R.id.ivProfilePhoto);
        new FirestoreProfileRepository().getProfile(deviceId,
                new ProfileRepository.ProfileCallback() {
                    @Override
                    public void onSuccess(UserProfile profile) {
                        if (getActivity() == null) return;
                        if (profile != null
                                && profile.getProfilePhotoUri() != null
                                && !profile.getProfilePhotoUri().isEmpty()) {
                            com.bumptech.glide.Glide.with(requireContext())
                                    .load(profile.getProfilePhotoUri())
                                    .placeholder(R.drawable.ic_profile_placeholder_forstyledlayout)
                                    .into(ivProfilePhoto);
                        }
                    }
                    @Override public void onFailure(Exception e) {}
                });

        // ── AI Assistant card (optional — only wired if card exists in layout) ──
        View aiCard = view.findViewById(R.id.card_ai_assistant);
        if (aiCard != null) {
            aiCard.setOnClickListener(v ->
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new AiAssistantFragment())
                            .addToBackStack(null)
                            .commit()
            );
        }

        loadDashboard();
    }

    // ── Load waitlist records ──────────────────────────────────────

    /**
     * Loads the current user's waitlist records and renders dashboard cards.
     *
     * Accepted events become upcoming event cards, while waiting entries
     * become waitlist cards with a leave option.
     */
    private void loadDashboard() {
        waitListRepo.getRecordsForDevice(deviceId, new WaitListRepository.WaitListCallBack() {
            @Override
            public void onSuccess(List<WaitListRecord> records) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    containerUpcomingEvents.removeAllViews();
                    containerWaitlist.removeAllViews();

                    int[] upcomingCount = {0};
                    int[] waitlistCount = {0};

                    if (records.isEmpty()) {
                        tvUpcomingEmpty.setVisibility(View.VISIBLE);
                        tvWaitlistEmpty.setVisibility(View.VISIBLE);
                        return;
                    }

                    for (WaitListRecord record : records) {
                        if (record.getStatus() == WaitStatus.ACCEPTED) {
                            upcomingCount[0]++;
                            addUpcomingEventCard(record);
                        } else if (record.getStatus() == WaitStatus.WAITING) {
                            waitlistCount[0]++;
                            addWaitlistCard(record);
                        }
                    }

                    if (upcomingCount[0] == 0) tvUpcomingEmpty.setVisibility(View.VISIBLE);
                    if (waitlistCount[0] == 0) tvWaitlistEmpty.setVisibility(View.VISIBLE);
                });
            }

            @Override
            public void onFailure(Exception e) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    tvUpcomingEmpty.setVisibility(View.VISIBLE);
                    tvWaitlistEmpty.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    /**
     * Creates and displays a card for an upcoming accepted event.
     *
     * The card includes event metadata and navigates to the detail screen.
     */
    private void addUpcomingEventCard(WaitListRecord record) {
        eventRepo.getEventById(record.getEventId(),
                new FirestoreEventRepository.EventByIdCallback() {
                    @Override
                    public void onResult(EventSummary event) {
                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(() -> {
                            String name     = event != null ? event.getName() : record.getEventId();
                            String desc     = event != null ? event.getDescription() : "";
                            String date     = event != null && event.getEventDate() > 0
                                    ? "📅 " + new SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                                    .format(new Date(event.getEventDate())) : "";
                            String location = event != null && !event.getLocation().isEmpty()
                                    ? "📍 " + event.getLocation() : "";
                            String meta     = location.isEmpty() ? date : location + "  " + date;

                            View card = LayoutInflater.from(requireContext())
                                    .inflate(R.layout.item_dashboard_event,
                                            containerUpcomingEvents, false);

                            ((TextView) card.findViewById(R.id.tvEventName)).setText(name);
                            ((TextView) card.findViewById(R.id.tvEventDesc)).setText(desc);
                            ((TextView) card.findViewById(R.id.tvEventDate)).setText(meta);

                            card.setOnClickListener(v -> navigateToDetail(event));

                            MaterialButton btnViewDetails = card.findViewById(R.id.btnViewDetails);
                            btnViewDetails.setOnClickListener(v -> navigateToDetail(event));

                            containerUpcomingEvents.addView(card);
                            tvUpcomingEmpty.setVisibility(View.GONE);
                        });
                    }
                });
    }

    /**
     * Opens the event detail fragment for the selected event.
     *
     * @param event event summary to display
     */
    private void navigateToDetail(EventSummary event) {
        if (event == null || getActivity() == null) return;
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, EventDetailFragment.newInstance(
                        event.getId(),
                        event.getName(),
                        event.getDescription(),
                        event.getOrganizerDeviceId(),
                        event.getPosterUrl(),
                        event.isRequiresGeolocation(),
                        event.getCoOrganizerIds()
                ))
                .addToBackStack(null)
                .commit();
    }

    /**
     * Creates and displays a card for a waiting list entry.
     *
     * The card allows the user to leave the waitlist for the event.
     */
    private void addWaitlistCard(WaitListRecord record) {
        eventRepo.getEventById(record.getEventId(),
                new FirestoreEventRepository.EventByIdCallback() {
                    @Override
                    public void onResult(EventSummary event) {
                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(() -> {
                            String name = event != null ? event.getName() : record.getEventId();

                            View card = LayoutInflater.from(requireContext())
                                    .inflate(R.layout.item_dashboard_waitlist,
                                            containerWaitlist, false);

                            ((TextView) card.findViewById(R.id.tvWaitlistEventName)).setText(name);

                            MaterialButton btnLeave = card.findViewById(R.id.btnLeaveWaitlist);
                            btnLeave.setOnClickListener(v ->
                                    waitListRepo.removeFromWaitList(
                                            record.getEventId(), deviceId,
                                            new WaitListRepository.OperationCallback() {
                                                @Override
                                                public void onSuccess() {
                                                    if (getActivity() == null) return;
                                                    getActivity().runOnUiThread(() -> {
                                                        containerWaitlist.removeView(card);
                                                        if (containerWaitlist.getChildCount() == 0)
                                                            tvWaitlistEmpty.setVisibility(View.VISIBLE);
                                                    });
                                                }
                                                @Override public void onFailure(Exception e) {}
                                            })
                            );

                            containerWaitlist.addView(card);
                            tvWaitlistEmpty.setVisibility(View.GONE);
                        });
                    }
                });
    }
}