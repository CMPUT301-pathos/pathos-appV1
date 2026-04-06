package com.example.eventlottery;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlottery.data.EventRepository;
import com.example.eventlottery.domain.ChatMessage;
import com.example.eventlottery.domain.EventSummary;
import com.example.eventlottery.firebase.FirestoreEventRepository;
import com.example.eventlottery.ui.ChatMessageAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * AiAssistantFragment
 *
 * Loads live event data from Firestore and injects it into the Claude
 * system prompt so users can ask natural questions like
 * "what swimming events are available?" and get accurate answers.
 */
public class AiAssistantFragment extends Fragment {

    // -------------------------------------------------------------------------
    // IMPORTANT: Replace this with your real Anthropic API key.
    // Get one at https://console.anthropic.com
    // For production use BuildConfig or a backend proxy — never commit a real key.
    // -------------------------------------------------------------------------
    private static final String ANTHROPIC_API_KEY = "HELL_IDK";

    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String MODEL   = "claude-sonnet-4-20250514";
    //private static final String MODEL = "claude-3-haiku-20240307";

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private static final String BASE_SYSTEM_PROMPT =
            "You are a friendly assistant embedded in an event lottery app called Pathos. " +
                    "Keep answers concise (2-4 sentences). Be warm and helpful.\n\n" +
                    "How the app works:\n" +
                    "- Users browse events and join waitlists.\n" +
                    "- Organisers run lottery draws to randomly select entrants.\n" +
                    "- Statuses: WAITING (eligible), INVITED (selected, must accept/decline), " +
                    "ACCEPTED (enrolled), DECLINED, CANCELLED, NOT_SELECTED.\n" +
                    "- Private events are invite-only and not in the browse list.\n" +
                    "- Some events require sharing your location to join.\n" +
                    "- You need a completed profile (name + email) to join or create events.\n\n" +
                    "Use the live event data below to answer questions about available events. " +
                    "For lists use hyphens (-). No markdown headers or bold text.\n\n";

    private static final String[] SUGGESTIONS = {
            "What events are available?",
            "Any sports events?",
            "How does the lottery work?",
            "What does INVITED mean?",
            "How do I join an event?",
            "What is a private event?"
    };

    // Views
    private RecyclerView       recyclerMessages;
    private EditText           etInput;
    private ImageButton        btnSend;
    private View               typingIndicator;
    private View               chipsContainer;

    // State
    private ChatMessageAdapter      adapter;
    private final List<ChatMessage> messages            = new ArrayList<>();
    private final List<HistoryTurn> conversationHistory = new ArrayList<>();
    private final OkHttpClient      httpClient          = new OkHttpClient();
    private final Handler           mainHandler         = new Handler(Looper.getMainLooper());
    private boolean                 isLoading           = false;
    private String                  eventsContext       = "Event data is still loading...";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragmentaiassistant, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerMessages = view.findViewById(R.id.recycler_messages);
        etInput          = view.findViewById(R.id.et_chat_input);
        btnSend          = view.findViewById(R.id.btn_chat_send);
        typingIndicator  = view.findViewById(R.id.typing_indicator);
        chipsContainer   = view.findViewById(R.id.chips_container);

        ImageButton btnBack = view.findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v ->
                    requireActivity().getSupportFragmentManager().popBackStack());
        }

        setupRecyclerView();
        setupSuggestionChips(view);
        setupInput();

        addAiMessage("Hey! Loading event data from the app…");
        loadEventsFromFirestore();
    }

    // ── Firestore ─────────────────────────────────────────────────────────────

    private void loadEventsFromFirestore() {
        new FirestoreEventRepository().getAllEvents(new EventRepository.ListCallback() {
            @Override
            public void onSuccess(List<EventSummary> events) {
                eventsContext = buildEventsContext(events);
                mainHandler.post(() -> {
                    if (!messages.isEmpty()) {
                        messages.set(0, new ChatMessage(
                                "Hey! I've loaded " + events.size() + " event(s) from the app. " +
                                        "Try asking \"what events are available?\" or \"any sports events?\"",
                                false));
                        adapter.notifyItemChanged(0);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                eventsContext = "Could not load events at this time.";
                mainHandler.post(() -> {
                    if (!messages.isEmpty()) {
                        messages.set(0, new ChatMessage(
                                "Hey! I couldn't load event data right now, but I can still " +
                                        "answer questions about how the app works.", false));
                        adapter.notifyItemChanged(0);
                    }
                });
            }
        });
    }

    private String buildEventsContext(List<EventSummary> events) {
        if (events == null || events.isEmpty()) {
            return "There are currently no events available in the app.";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        StringBuilder sb = new StringBuilder("LIVE EVENTS (" + events.size() + " total):\n");
        int limit = Math.min(events.size(), 20);
        for (int i = 0; i < limit; i++) {
            EventSummary e = events.get(i);
            sb.append("- ").append(e.getName());
            if (!e.getCategory().isEmpty()) sb.append(" [").append(e.getCategory()).append("]");
            if (!e.getLocation().isEmpty()) sb.append(", ").append(e.getLocation());
            if (e.getEventDate() > 0) sb.append(", ").append(sdf.format(new Date(e.getEventDate())));
            sb.append(", open=").append(e.isRegistrationOpen() ? "yes" : "no");
            if (e.isPrivate()) sb.append(", private");
            sb.append("\n");
        }
        if (events.size() > 20) sb.append("...and ").append(events.size() - 20).append(" more.\n");
        return sb.toString();
    }

    // ── Setup ─────────────────────────────────────────────────────────────────

    private void setupRecyclerView() {
        adapter = new ChatMessageAdapter(messages);
        LinearLayoutManager lm = new LinearLayoutManager(requireContext());
        lm.setStackFromEnd(true);
        recyclerMessages.setLayoutManager(lm);
        recyclerMessages.setAdapter(adapter);
    }

    private void setupSuggestionChips(View root) {
        int[] chipIds = {
                R.id.chip_1, R.id.chip_2, R.id.chip_3,
                R.id.chip_4, R.id.chip_5, R.id.chip_6
        };
        for (int i = 0; i < chipIds.length && i < SUGGESTIONS.length; i++) {
            TextView chip = root.findViewById(chipIds[i]);
            if (chip == null) continue;
            chip.setText(SUGGESTIONS[i]);
            final String text = SUGGESTIONS[i];
            chip.setOnClickListener(v -> sendMessage(text));
        }
    }

    private void setupInput() {
        etInput.setOnEditorActionListener((v, actionId, event) -> {
            boolean isSend  = actionId == EditorInfo.IME_ACTION_SEND;
            boolean isEnter = event != null
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction()  == KeyEvent.ACTION_DOWN;
            if (isSend || isEnter) { handleSend(); return true; }
            return false;
        });
        btnSend.setOnClickListener(v -> handleSend());
    }

    // ── Sending ───────────────────────────────────────────────────────────────

    private void handleSend() {
        String text = etInput.getText().toString().trim();
        if (text.isEmpty() || isLoading) return;
        etInput.setText("");
        sendMessage(text);
    }

    private void sendMessage(String userText) {
        if (isLoading) return;
        if (chipsContainer != null) chipsContainer.setVisibility(View.GONE);

        addUserMessage(userText);
        conversationHistory.add(new HistoryTurn("user", userText));

        showTyping(true);
        isLoading = true;
        btnSend.setEnabled(false);

        callAnthropicApi();
    }

    // ── API ───────────────────────────────────────────────────────────────────

    private void callAnthropicApi() {
        try {
            String systemPrompt = BASE_SYSTEM_PROMPT + eventsContext;

            JSONArray msgs = new JSONArray();
            int start = Math.max(0, conversationHistory.size() - 20);
            for (int i = start; i < conversationHistory.size(); i++) {
                HistoryTurn t = conversationHistory.get(i);
                JSONObject m = new JSONObject();
                m.put("role", t.role);
                m.put("content", t.content);
                msgs.put(m);
            }

            JSONObject body = new JSONObject();
            body.put("model", MODEL);
            body.put("max_tokens", 1000);
            body.put("system", systemPrompt);
            body.put("messages", msgs);

            RequestBody requestBody = RequestBody.create(body.toString(), JSON);

            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("x-api-key", ANTHROPIC_API_KEY)
                    .addHeader("anthropic-version", "2023-06-01")
                    .addHeader("Content-Type", "application/json")
                    .post(requestBody)
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    mainHandler.post(() -> handleError(
                            "Connection failed. Please check your internet and try again."));
                }

                @Override
                public void onResponse(@NonNull Call call,
                                       @NonNull Response response) throws IOException {
                    String raw  = response.body() != null ? response.body().string() : "";
                    int    code = response.code();
                    boolean ok  = response.isSuccessful();
                    mainHandler.post(() -> handleResponse(raw, ok, code));
                }
            });

        } catch (JSONException e) {
            handleError("Failed to prepare your message. Please try again.");
        }
    }

    private void handleResponse(String body, boolean ok, int code) {
        showTyping(false);
        isLoading = false;
        btnSend.setEnabled(true);

        if (!ok) {
            if (code == 401) {
                addAiMessage("API key invalid or not set. Check AiAssistantFragment.java line 61.");
            } else if (code == 400) {
                // Log the actual error for debugging
                android.util.Log.e("AI", "400 error body: " + body);
                addAiMessage("Request error (400). The message could not be processed. " +
                        "Check Logcat for details.");
            } else {
                addAiMessage("Server error (" + code + "). Please try again in a moment.");
            }
            return;
        }

        try {
            JSONObject json   = new JSONObject(body);
            JSONArray content = json.getJSONArray("content");
            String reply      = content.getJSONObject(0).getString("text");
            conversationHistory.add(new HistoryTurn("assistant", reply));
            addAiMessage(reply);
        } catch (JSONException e) {
            addAiMessage("I received an unexpected response. Please try again.");
        }
    }

    private void handleError(String message) {
        showTyping(false);
        isLoading = false;
        btnSend.setEnabled(true);
        addAiMessage(message);
    }

    // ── Message helpers ───────────────────────────────────────────────────────

    private void addAiMessage(String text) {
        messages.add(new ChatMessage(text, false));
        notifyAndScroll();
    }

    private void addUserMessage(String text) {
        messages.add(new ChatMessage(text, true));
        notifyAndScroll();
    }

    private void notifyAndScroll() {
        adapter.notifyItemInserted(messages.size() - 1);
        recyclerMessages.smoothScrollToPosition(messages.size() - 1);
    }

    private void showTyping(boolean show) {
        if (typingIndicator != null)
            typingIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) recyclerMessages.post(() ->
                recyclerMessages.smoothScrollToPosition(messages.size() - 1));
    }

    private static class HistoryTurn {
        final String role;
        final String content;
        HistoryTurn(String role, String content) {
            this.role = role; this.content = content;
        }
    }
}