package com.company.eclms.modules.report.service.impl;

import com.company.eclms.modules.contract.entity.Contract;
import com.company.eclms.modules.contract.repository.ContractRepository;
import com.company.eclms.modules.report.service.ReportService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ContractRepository contractRepository;

    @Override
    @Transactional(readOnly = true)
    public ByteArrayInputStream generateContractsExcelReport() {
        List<Contract> contracts = contractRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Contracts Report");

            // Header Row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Contract ID", "Contract Name", "Vendor Name", "Department Name", "Status", "Start Date", "End Date", "Risk Score", "Version"};
            for (int col = 0; col < headers.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(headers[col]);
            }

            int rowIdx = 1;
            for (Contract contract : contracts) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(contract.getId().toString());
                row.createCell(1).setCellValue(contract.getName());
                row.createCell(2).setCellValue(contract.getVendor().getName());
                row.createCell(3).setCellValue(contract.getDepartment().getName());
                row.createCell(4).setCellValue(contract.getStatus());
                row.createCell(5).setCellValue(contract.getStartDate() != null ? contract.getStartDate().toString() : "");
                row.createCell(6).setCellValue(contract.getEndDate() != null ? contract.getEndDate().toString() : "");
                row.createCell(7).setCellValue(contract.getRiskScore() != null ? contract.getRiskScore().doubleValue() : 0.0);
                row.createCell(8).setCellValue(contract.getVersionString());
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            log.error("Failed to generate Excel report", e);
            throw new RuntimeException("Excel report generation failed: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ByteArrayInputStream generateContractsPdfReport() {
        List<Contract> contracts = contractRepository.findAll();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Document document = new Document(PageSize.A4.rotate());
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Document Title
            Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("ECLMS - Contracts Executive Report", fontHeader);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Table Structure
            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100f);
            table.setWidths(new float[]{2.5f, 2f, 2f, 1.5f, 1.5f, 1.5f, 1f, 1f});

            // Headers
            String[] headers = {"Contract Name", "Vendor", "Department", "Status", "Start Date", "End Date", "Risk", "Ver"};
            Font fontTableHead = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, fontTableHead));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            // Rows
            Font fontRow = FontFactory.getFont(FontFactory.HELVETICA, 9);
            for (Contract contract : contracts) {
                table.addCell(new Phrase(contract.getName(), fontRow));
                table.addCell(new Phrase(contract.getVendor().getName(), fontRow));
                table.addCell(new Phrase(contract.getDepartment().getName(), fontRow));
                table.addCell(new Phrase(contract.getStatus(), fontRow));
                table.addCell(new Phrase(contract.getStartDate() != null ? contract.getStartDate().toString() : "", fontRow));
                table.addCell(new Phrase(contract.getEndDate() != null ? contract.getEndDate().toString() : "", fontRow));
                table.addCell(new Phrase(contract.getRiskScore() != null ? contract.getRiskScore().toString() : "0.0", fontRow));
                table.addCell(new Phrase(contract.getVersionString(), fontRow));
            }

            document.add(table);
            document.close();
        } catch (DocumentException e) {
            log.error("Failed to generate PDF report", e);
            throw new RuntimeException("PDF report generation failed: " + e.getMessage(), e);
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    @Override
    @Transactional(readOnly = true)
    public String generateContractsCsvReport() {
        List<Contract> contracts = contractRepository.findAll();
        StringBuilder csv = new StringBuilder();

        // Write headers
        csv.append("Contract ID,Contract Name,Vendor Name,Department Name,Status,Start Date,End Date,Risk Score,Version\n");

        for (Contract contract : contracts) {
            csv.append("\"").append(contract.getId()).append("\",")
               .append("\"").append(escapeCsv(contract.getName())).append("\",")
               .append("\"").append(escapeCsv(contract.getVendor().getName())).append("\",")
               .append("\"").append(escapeCsv(contract.getDepartment().getName())).append("\",")
               .append("\"").append(contract.getStatus()).append("\",")
               .append("\"").append(contract.getStartDate() != null ? contract.getStartDate().toString() : "").append("\",")
               .append("\"").append(contract.getEndDate() != null ? contract.getEndDate().toString() : "").append("\",")
               .append("\"").append(contract.getRiskScore() != null ? contract.getRiskScore().toString() : "0.00").append("\",")
               .append("\"").append(contract.getVersionString()).append("\"\n");
        }

        return csv.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\"", "\"\"");
    }
}
