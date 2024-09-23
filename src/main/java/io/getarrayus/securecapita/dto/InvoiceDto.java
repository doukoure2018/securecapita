package io.getarrayus.securecapita.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.getarrayus.securecapita.domain.Customer;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

import static jakarta.persistence.GenerationType.IDENTITY;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class InvoiceDto {
    private Long id;
    private String invoiceNumber;
    private String services;
    private Date date;
    private String status;
    private double total;
    private Long customer_id;
}
