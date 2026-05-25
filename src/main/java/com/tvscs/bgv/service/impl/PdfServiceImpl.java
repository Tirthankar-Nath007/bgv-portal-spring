package com.tvscs.bgv.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.tvscs.bgv.domain.dto.ComparisonResultDto;
import com.tvscs.bgv.domain.entity.Employee;
import com.tvscs.bgv.domain.entity.VerificationRecord;
import com.tvscs.bgv.domain.entity.Verifier;
import com.tvscs.bgv.exception.ResourceNotFoundException;
import com.tvscs.bgv.repository.EmployeeRepository;
import com.tvscs.bgv.repository.VerificationRecordRepository;
import com.tvscs.bgv.repository.VerifierRepository;
import com.tvscs.bgv.service.PdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PdfServiceImpl implements PdfService {

    private final VerificationRecordRepository recordRepository;
    private final VerifierRepository verifierRepository;
    private final EmployeeRepository employeeRepository;
    private final ObjectMapper objectMapper;

    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 16, Font.BOLD, Color.WHITE);
    private static final Font HEADER_FONT = new Font(Font.HELVETICA, 11, Font.BOLD);
    private static final Font BODY_FONT = new Font(Font.HELVETICA, 10);
    private static final Font BODY_BOLD = new Font(Font.HELVETICA, 10, Font.BOLD);
    private static final Color HEADER_COLOR = new Color(0, 100, 60);
    private static final Color MATCH_COLOR = new Color(39, 174, 96);
    private static final Color MISMATCH_COLOR = new Color(192, 57, 43);
    private static final Color ROW_EVEN = new Color(245, 248, 245);

    public PdfServiceImpl(VerificationRecordRepository recordRepository, VerifierRepository verifierRepository, EmployeeRepository employeeRepository, ObjectMapper objectMapper) {
        this.recordRepository = recordRepository;
        this.verifierRepository = verifierRepository;
        this.employeeRepository = employeeRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public byte[] generateVerificationReport(String verificationId, Long verifierId) {
        VerificationRecord record = recordRepository.findByVerificationIdIgnoreCase(verificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Verification not found: " + verificationId));

        if (verifierId != null && !record.getVerifierId().equals(verifierId)) {
            throw new ResourceNotFoundException("Verification not found");
        }

        Verifier verifier = verifierRepository.findById(record.getVerifierId()).orElse(null);
        Employee employee = employeeRepository.findByEmployeeIdIgnoreCase(record.getEmployeeId()).orElse(null);
        List<ComparisonResultDto> results = parseResults(record.getComparisonResults());

        Document doc = new Document(PageSize.A4, 40, 40, 60, 40);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();
            addContent(doc, record, verifier, employee, results);
            doc.close();
        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        }
        return out.toByteArray();
    }

    private void addContent(Document doc, VerificationRecord record, Verifier verifier,
                            Employee employee, List<ComparisonResultDto> results) throws Exception {
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd MMM yyyy");

        // Header banner
        PdfPTable banner = new PdfPTable(1);
        banner.setWidthPercentage(100);
        PdfPCell bannerCell = new PdfPCell(new Phrase("TVSC CREDIT SOLUTIONS\nEMPLOYMENT VERIFICATION REPORT", TITLE_FONT));
        bannerCell.setBackgroundColor(HEADER_COLOR);
        bannerCell.setPadding(14);
        bannerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        bannerCell.setBorder(Rectangle.NO_BORDER);
        banner.addCell(bannerCell);
        doc.add(banner);
        doc.add(Chunk.NEWLINE);

        // Verification metadata
        doc.add(sectionTitle("Verification Details"));
        doc.add(infoTable(new String[][]{
                {"Verification ID", record.getVerificationId()},
                {"Verified On", record.getCreatedAt() != null ? record.getCreatedAt().format(dateFmt) : "N/A"},
                {"Verified By", verifier != null ? verifier.getCompanyName() : "N/A"},
                {"Verifier Email", verifier != null ? verifier.getEmail() : "N/A"}
        }));
        doc.add(Chunk.NEWLINE);

        // Employee details
        if (employee != null) {
            doc.add(sectionTitle("Employee Information"));
            doc.add(infoTable(new String[][]{
                    {"Employee ID", employee.getEmployeeId()},
                    {"Full Name", employee.getFullName()},
                    {"Entity / Company", employee.getBusiness() != null ? employee.getBusiness() : "N/A"},
                    {"Department", employee.getDepartment() != null ? employee.getDepartment() : "N/A"},
                    {"Designation", employee.getDesignation() != null ? employee.getDesignation() : "N/A"},
                    {"Date of Joining", employee.getDateOfJoining() != null ? employee.getDateOfJoining().format(dateFmt) : "N/A"},
                    {"Last Working Day", employee.getDateOfLeaving() != null ? employee.getDateOfLeaving().format(dateFmt) : "N/A"}
            }));
            doc.add(Chunk.NEWLINE);
        }

        // Comparison table
        doc.add(sectionTitle("Field-by-Field Comparison"));
        PdfPTable compTable = new PdfPTable(4);
        compTable.setWidthPercentage(100);
        compTable.setWidths(new float[]{2f, 2.5f, 2.5f, 1.5f});
        addTableHeader(compTable, new String[]{"Field", "Submitted Value", "Official Record", "Result"});

        int i = 0;
        for (ComparisonResultDto r : results) {
            Color bg = (i % 2 == 0) ? Color.WHITE : ROW_EVEN;
            addComparisonRow(compTable, r, bg);
            i++;
        }
        doc.add(compTable);
        doc.add(Chunk.NEWLINE);

        // Summary
        doc.add(sectionTitle("Verification Summary"));
        String statusLabel = switch (record.getOverallStatus()) {
            case "matched" -> "PERFECT MATCH";
            case "partial_match" -> "PARTIAL MATCH";
            default -> "SIGNIFICANT MISMATCH";
        };
        Color statusColor = "matched".equals(record.getOverallStatus()) ? MATCH_COLOR : MISMATCH_COLOR;
        Paragraph summary = new Paragraph();
        summary.add(new Chunk("Overall Status: ", BODY_BOLD));
        Font statusFont = new Font(Font.HELVETICA, 12, Font.BOLD, statusColor);
        summary.add(new Chunk(statusLabel + "  (" + record.getMatchScore() + "% match)", statusFont));
        doc.add(summary);
        doc.add(Chunk.NEWLINE);

        // Footer
        Paragraph footer = new Paragraph(
                "This is a system-generated report. Generated by BGV Portal.",
                new Font(Font.HELVETICA, 8, Font.ITALIC, Color.GRAY));
        footer.setAlignment(Element.ALIGN_CENTER);
        doc.add(footer);
    }

    private Paragraph sectionTitle(String title) {
        Paragraph p = new Paragraph(title, new Font(Font.HELVETICA, 12, Font.BOLD, HEADER_COLOR));
        p.setSpacingBefore(6);
        p.setSpacingAfter(4);
        return p;
    }

    private PdfPTable infoTable(String[][] rows) throws Exception {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2f, 4f});
        for (String[] row : rows) {
            PdfPCell label = new PdfPCell(new Phrase(row[0], BODY_BOLD));
            label.setBackgroundColor(new Color(240, 244, 240));
            label.setPadding(5);
            label.setBorderColor(Color.LIGHT_GRAY);
            PdfPCell value = new PdfPCell(new Phrase(row[1] != null ? row[1] : "N/A", BODY_FONT));
            value.setPadding(5);
            value.setBorderColor(Color.LIGHT_GRAY);
            table.addCell(label);
            table.addCell(value);
        }
        return table;
    }

    private void addTableHeader(PdfPTable table, String[] headers) {
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE)));
            cell.setBackgroundColor(HEADER_COLOR);
            cell.setPadding(6);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }

    private void addComparisonRow(PdfPTable table, ComparisonResultDto r, Color bg) {
        String fieldLabel = humanize(r.getField());
        Color resultColor = r.isMatch() ? MATCH_COLOR : MISMATCH_COLOR;
        String resultLabel = r.isMatch() ? "✓ Match" : "✗ Mismatch";
        if ("not_provided".equals(r.getMatchType())) resultLabel = "Not Provided";

        for (String val : new String[]{fieldLabel, nullSafe(r.getVerifierValue()), nullSafe(r.getCompanyValue())}) {
            PdfPCell cell = new PdfPCell(new Phrase(val, BODY_FONT));
            cell.setBackgroundColor(bg);
            cell.setPadding(5);
            cell.setBorderColor(Color.LIGHT_GRAY);
            table.addCell(cell);
        }
        PdfPCell resultCell = new PdfPCell(new Phrase(resultLabel, new Font(Font.HELVETICA, 9, Font.BOLD, resultColor)));
        resultCell.setBackgroundColor(bg);
        resultCell.setPadding(5);
        resultCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        resultCell.setBorderColor(Color.LIGHT_GRAY);
        table.addCell(resultCell);
    }

    private String humanize(String field) {
        if (field == null) return "";
        return switch (field) {
            case "employeeId" -> "Employee ID";
            case "name" -> "Full Name";
            case "entityName" -> "Entity / Company";
            case "dateOfJoining" -> "Date of Joining";
            case "dateOfLeaving" -> "Date of Leaving";
            case "designation" -> "Designation";
            case "exitReason" -> "Exit Reason";
            default -> field;
        };
    }

    private String nullSafe(String s) {
        return s != null ? s : "—";
    }

    private List<ComparisonResultDto> parseResults(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
