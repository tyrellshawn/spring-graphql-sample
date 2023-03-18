package com.example.demo.payments.billing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.yaml.snakeyaml.events.Event;

import java.util.Date;
import java.util.UUID;

import java.util.UUID;
import java.util.Date;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Bill {
    private UUID billId;
    private BillType billType;
    private BillStatus billStatus;
    private Float billAmount;
    private Date billDate;
    private Date billDueDate;
    private Date billPaidDate;
    private Float billPaidAmount;
    private boolean billPaidStatus;
    private BillPaidMethod billPaidMethod;
    private String billPaidReference;

    // constructor, getters, and setters
}
