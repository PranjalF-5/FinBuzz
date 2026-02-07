package com.mybank.bankingapp.repository;

import com.mybank.bankingapp.model.FundTransferReq;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FundTransferRepository extends JpaRepository<FundTransferReq, String> {
}
