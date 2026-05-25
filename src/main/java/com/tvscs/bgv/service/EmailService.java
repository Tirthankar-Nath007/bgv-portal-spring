package com.tvscs.bgv.service;

import com.tvscs.bgv.domain.entity.Appeal;
import com.tvscs.bgv.domain.entity.Verifier;

public interface EmailService {
    void sendAppealNotification(Appeal appeal, Verifier verifier);
    void sendBlockNotification(Verifier verifier, String employeeId, int attemptCount);
    void sendAppealResponse(Appeal appeal, String verifierEmail);
}
