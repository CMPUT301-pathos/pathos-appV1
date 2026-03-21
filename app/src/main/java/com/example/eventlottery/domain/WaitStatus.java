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
 * - NOT_SELECTED: entrant participated in a raffle draw but was not chosen
 *
 * Semantic note:
 * - DECLINED means the entrant was invited and then chose not to attend
 * - NOT_SELECTED means the entrant was never invited in that raffle round
 *
 * @author Fawaz Mansoor
 * @version 1.1
 */
public enum WaitStatus {
    WAITING,
    INVITED,
    ACCEPTED,
    DECLINED,
    CANCELLED,
    NOT_SELECTED
}