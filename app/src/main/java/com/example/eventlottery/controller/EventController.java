package com.example.eventlottery.controller;

import com.example.eventlottery.data.EventRepository;
import com.example.eventlottery.domain.EventSummary;

import java.util.List;

public class EventController {

    private final EventRepository eventRepository;

    public EventController(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public void loadAllEvents(EventRepository.ListCallback callback) {
        eventRepository.getAllEvents(callback);
    }
}
