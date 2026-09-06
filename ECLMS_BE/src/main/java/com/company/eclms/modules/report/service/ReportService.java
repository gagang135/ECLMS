package com.company.eclms.modules.report.service;

import java.io.ByteArrayInputStream;

public interface ReportService {
    ByteArrayInputStream generateContractsExcelReport();
    ByteArrayInputStream generateContractsPdfReport();
    String generateContractsCsvReport();
}
