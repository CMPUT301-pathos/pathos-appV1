package com.example.eventlottery;

import com.example.eventlottery.domain.WaitListRecord;
import com.example.eventlottery.domain.WaitStatus;

import org.junit.Test;
import static org.junit.Assert.*;

public class ExampleUnitTest {

    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    // US 01.05.02 - Entrant can accept an invitation
    @Test
    public void testAcceptInvitation_changesStatusToAccepted() {
        WaitListRecord record = new WaitListRecord("event123", "device123");
        record.setStatus(WaitStatus.INVITED);
        record.acceptInvitation();
        assertEquals(WaitStatus.ACCEPTED, record.getStatus());
    }

    @Test
    public void testAcceptInvitation_failsIfNotInvited() {
        WaitListRecord record = new WaitListRecord("event123", "device123");
        // Status is WAITING by default, not INVITED
        assertThrows(IllegalStateException.class, () -> {
            record.acceptInvitation();
        });
    }

    @Test
    public void testNewRecord_defaultStatusIsWaiting() {
        WaitListRecord record = new WaitListRecord("event123", "device123");
        assertEquals(WaitStatus.WAITING, record.getStatus());
    }

    @Test
    public void testDeclineInvitation_changesStatusToDeclined() {
        WaitListRecord record = new WaitListRecord("event123", "device123");
        record.setStatus(WaitStatus.INVITED);
        record.declineInvitation();
        assertEquals(WaitStatus.DECLINED, record.getStatus());
    }
}