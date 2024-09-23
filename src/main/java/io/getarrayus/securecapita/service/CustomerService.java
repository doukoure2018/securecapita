package io.getarrayus.securecapita.service;

import io.getarrayus.securecapita.domain.Customer;
import io.getarrayus.securecapita.domain.Invoice;
import io.getarrayus.securecapita.dto.InvoiceDto;
import io.getarrayus.securecapita.dto.Stats;
import org.springframework.data.domain.Page;

public interface CustomerService {

    // Customer functions
    Customer createCustomer(Customer customer);
    Customer updateCustomer(Customer customer);
    Page<Customer> getCustomers(int page, int size);
    Iterable<Customer> getCustomers();
    Customer getCustomer(Long id);
    Page<Customer> searchCustomers(String name, int page, int size);

    // Invoice functions
    Invoice createInvoice(InvoiceDto invoiceDto);
    Page<Invoice> getInvoices(int page, int size);
    void addInvoiceToCustomer(Long id, Invoice invoice);
    Invoice getInvoice(Long id);
    Stats getStats();
}
