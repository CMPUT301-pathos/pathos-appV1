package com.example.eventlottery.service;

import com.example.eventlottery.data.EventRepository;
import com.example.eventlottery.domain.EventSummary;

import java.util.List;

/**
 * Service for QR code generation and scan resolution.
 *
 * Responsibilities:
 * - Decode a scanned QR payload into an eventId
 * - Validate the scanned code points to an existing event
 * - Support "scan → view details" flow
 *
 * User stories supported:
 * - US 01.06.01: View event details by scanning the promotional QR code
 *
 * @author Fawaz Mansoor
 * @version 1.0
 */
public class BeaconQrService {

    private final EventRepository eventRepository;

    /**
     * Constructs the QR service using the provided event repository.
     *
     * @param eventRepository repository used to resolve scanned event identifiers
     */
    public BeaconQrService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    /**
     * Decodes a raw QR scan result into an eventId.
     * The QR payload is expected to be the eventId directly.
     */
    public String decodeEventId(String qrPayload) {
        if (qrPayload == null || qrPayload.trim().isEmpty()) {
            return null;
        }
        String payload = qrPayload.trim();
        // Handle "eventId:xxxx" format from QrCodeGenerator
        if (payload.startsWith("eventId:")) {
            return payload.replace("eventId:", "");
        }
        // Handle "pathos://event/xxxx" format
        if (payload.startsWith("pathos://event/")) {
            return payload.replace("pathos://event/", "");
        }
        // Fallback: treat whole payload as eventId
        return payload;
    }

    /**
     * Resolves a scanned QR payload to an EventSummary.
     * Looks up the eventId in the repository and returns the matching event.
     */
    public void resolveQrScan(String qrPayload, ResolveCallback callback) {
        String eventId = decodeEventId(qrPayload);
        if (eventId == null) {
            callback.onFailure(new Exception("Invalid QR code"));
            return;
        }

        eventRepository.getAllEvents(new EventRepository.ListCallback() {
            @Override
            public void onSuccess(List<EventSummary> events) {
                for (EventSummary event : events) {
                    if (event.getId().equals(eventId)) {
                        callback.onSuccess(event);
                        return;
                    }
                }
                callback.onFailure(new Exception("Event not found for QR code"));
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    public interface ResolveCallback {
        /**
         * Called when a QR scan resolves to an existing event.
         *
         * @param event resolved event summary
         */
        void onSuccess(EventSummary event);

        /**
         * Called when QR resolution fails.
         *
         * @param e failure exception
         */
        void onFailure(Exception e);
    }
}
