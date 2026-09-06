package com.company.eclms.common.search;

import java.util.List;
import java.util.UUID;

public interface ContractSearchService {
    void indexContract(UUID contractId, String name, String content, String vendorName, String status, String ocrText);
    void deleteIndex(UUID contractId);
    List<UUID> searchContractIds(String searchTerm);
}
