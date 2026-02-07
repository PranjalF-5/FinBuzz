package com.mybank.bankingapp.controller;

import com.mybank.bankingapp.model.ResponseMessage;
import com.mybank.bankingapp.model.FundTransferReq;
import com.mybank.bankingapp.service.FundTransferService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/fundTransfers")
public class FundTransferController {

    private final FundTransferService fundTransferService;

    public FundTransferController(FundTransferService fundTransferService) {
        this.fundTransferService = fundTransferService;
    }

    @PostMapping
    public ResponseEntity<ResponseMessage<FundTransferReq>> createFundTransfer(@Valid @RequestBody FundTransferReq fundTransfer) {
        FundTransferReq request = fundTransferService.initiateFundTransfer(fundTransfer);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ResponseMessage.success("Fund transfer request accepted, transfer ID: " + request.getId(), request));
    }

    @GetMapping("/{transferId}")
    public ResponseEntity<ResponseMessage<FundTransferReq>> getTransferStatus(@PathVariable String transferId) {
        Optional<FundTransferReq> request = fundTransferService.getTransferRequest(transferId);
        return request.map(r -> ResponseEntity.ok(ResponseMessage.success("Transfer status retrieved", r)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ResponseMessage.error("Transfer ID " + transferId + " not found", null)));
    }
}
