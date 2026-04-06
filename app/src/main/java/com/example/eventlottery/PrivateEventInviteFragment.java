package com.example.eventlottery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.eventlottery.domain.UserProfile;
import com.example.eventlottery.domain.WaitListRecord;
import com.example.eventlottery.domain.WaitStatus;
import com.example.eventlottery.firebase.FirestoreProfileRepository;
import com.example.eventlottery.firebase.FirestoreWaitListRepository;
import com.google.android.material.button.MaterialButton;

import java.util.List;

/**
 * PrivateEventInviteFragment
 *
 * Allows an organizer to search for entrants by name, email, or phone number
 * and invite them to a private event's waiting list.
 *
 * User stories supported:
 * - US 02.07.02: Organizer invites specific entrants to a private event
 *
 * @author Fawaz Mansoor
 * @version 1.0
 */
public class PrivateEventInviteFragment extends Fragment {

    private static final String ARG_EVENT_ID = "eventId";
    private static final String ARG_EVENT_NAME = "eventName";

    private String eventId;
    private String eventName;

    private EditText etSearch;
    private LinearLayout containerResults;
    private TextView tvResultsEmpty;
    private FirestoreProfileRepository profileRepo;
    private FirestoreWaitListRepository waitListRepo;

    /**
     * Factory method to create a private event invitation fragment.
     *
     * @param eventId Firestore id of the private event
     * @param eventName display name of the event
     * @return configured fragment instance
     */
    public static PrivateEventInviteFragment newInstance(String eventId, String eventName) {
        PrivateEventInviteFragment fragment = new PrivateEventInviteFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        args.putString(ARG_EVENT_NAME, eventName);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Initializes fragment arguments and sets up repositories.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventId = getArguments().getString(ARG_EVENT_ID);
            eventName = getArguments().getString(ARG_EVENT_NAME);
        }
        profileRepo = new FirestoreProfileRepository();
        waitListRepo = new FirestoreWaitListRepository();
    }

    /**
     * Inflates the private event invite layout and sets up the search interface.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_private_event_invite, container, false);

        TextView tvTitle = view.findViewById(R.id.tv_invite_title);
        tvTitle.setText("Invite to: " + eventName);

        etSearch = view.findViewById(R.id.et_invite_search);
        containerResults = view.findViewById(R.id.container_invite_results);
        tvResultsEmpty = view.findViewById(R.id.tv_invite_results_empty);
        MaterialButton btnSearch = view.findViewById(R.id.btn_invite_search);

        btnSearch.setOnClickListener(v -> {
            String query = etSearch.getText().toString().trim();
            if (query.isEmpty()) {
                Toast.makeText(getContext(), "Enter a name, email, or phone number",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            searchEntrants(query);
        });

        return view;
    }

    /**
     * Searches for user profiles matching the search query.
     *
     * Results can match by name, email, or phone number.
     *
     * @param query search string to match against profile information
     */
    private void searchEntrants(String query) {
        containerResults.removeAllViews();
        tvResultsEmpty.setVisibility(View.GONE);

        profileRepo.searchProfiles(query, new FirestoreProfileRepository.SearchCallback() {
            @Override
            public void onSuccess(List<UserProfile> profiles) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    if (profiles.isEmpty()) {
                        tvResultsEmpty.setVisibility(View.VISIBLE);
                        return;
                    }
                    for (UserProfile profile : profiles) {
                        addResultCard(profile);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(),
                                "Search failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
            }
        });
    }

    /**
     * Creates and displays a result card for a found user profile.
     *
     * The card includes the user's name, email, and phone number (if available),
     * along with an invite button.
     *
     * @param profile user profile to display
     */
    private void addResultCard(UserProfile profile) {
        View card = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_invite_result, containerResults, false);

        ((TextView) card.findViewById(R.id.tv_invite_name)).setText(profile.getName());
        ((TextView) card.findViewById(R.id.tv_invite_email)).setText(profile.getEmail());

        String phone = profile.getPhoneNumber();
        TextView tvPhone = card.findViewById(R.id.tv_invite_phone);
        if (phone != null && !phone.isEmpty()) {
            tvPhone.setText(phone);
            tvPhone.setVisibility(View.VISIBLE);
        } else {
            tvPhone.setVisibility(View.GONE);
        }

        MaterialButton btnInvite = card.findViewById(R.id.btn_invite_user);
        btnInvite.setOnClickListener(v -> inviteEntrant(profile, btnInvite));

        containerResults.addView(card);
    }

    /**
     * Invites a user to the private event by adding them to the waitlist
     * with INVITED status.
     *
     * @param profile user profile to invite
     * @param btnInvite the button that triggered the invite
     */
    private void inviteEntrant(UserProfile profile, MaterialButton btnInvite) {
        btnInvite.setEnabled(false);
        btnInvite.setText("Inviting...");

        WaitListRecord record = new WaitListRecord(eventId, profile.getDeviceId());
        record.setStatus(WaitStatus.INVITED);

        waitListRepo.addToWaitList(record, new com.example.eventlottery.data.WaitListRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    btnInvite.setText("Invited ✓");
                    Toast.makeText(getContext(),
                            profile.getName() + " invited!", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFailure(Exception e) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    btnInvite.setEnabled(true);
                    btnInvite.setText("Invite");
                    Toast.makeText(getContext(),
                            "Failed to invite: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
