package com.example.eventlottery.domain;

/**
 * Enum representing the possible states of an entrant's participation
 * in an event waiting list.
 *
 * States:
 * - WAITING: entrant has joined the waiting list
 * - INVITED: entrant has been selected by the lottery
 * - ACCEPTED: entrant has accepted the invitation
 * - DECLINED: entrant has declined the invitation
 * - CANCELLED: entrant has been cancelled
 *
 * @author Fawaz Mansoor
 * @version 1.0
 */
public enum WaitStatus {
    WAITING,
    INVITED,
    ACCEPTED,
    DECLINED,
    CANCELLED
}