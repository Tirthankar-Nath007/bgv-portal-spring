package com.tvscs.bgv.service;

public interface PdfService {
    byte[] generateVerificationReport(String verificationId, Long verifierId);
}
