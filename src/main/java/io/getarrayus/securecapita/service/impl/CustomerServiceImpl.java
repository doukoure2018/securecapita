package io.getarrayus.securecapita.service.impl;


import io.getarrayus.securecapita.domain.Customer;
import io.getarrayus.securecapita.domain.Invoice;
import io.getarrayus.securecapita.dto.InvoiceDto;
import io.getarrayus.securecapita.dto.Stats;
import io.getarrayus.securecapita.repository.CustomerRepository;
import io.getarrayus.securecapita.repository.InvoiceRepository;
import io.getarrayus.securecapita.repository.UserRepository;
import io.getarrayus.securecapita.service.CustomerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.springframework.data.domain.PageRequest.of;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private  final InvoiceRepository invoiceRepository;
    private final ModelMapper mapper;

    @Override
    public Customer createCustomer(Customer customer) {
        customer.setCreatedAt(new Date());
        return customerRepository.save(customer);
    }

    @Override
    public Customer updateCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    @Override
    public Page<Customer> getCustomers(int page, int size) {
        return customerRepository.findAll(of(page, size));
    }

    @Override
    public Iterable<Customer> getCustomers() {
        return customerRepository.findAll();
    }

    @Override
    public Customer getCustomer(Long id) {
        return customerRepository.findById(id).get();
    }

    @Override
    public Page<Customer> searchCustomers(String name, int page, int size) {
        return customerRepository.findByNameContaining(name, of(page, size));
    }


    @Override
    public Invoice createInvoice(InvoiceDto invoiceDto) {
        Invoice invoice=mapper.map(invoiceDto,Invoice.class);
        invoice.setInvoiceNumber(randomAlphanumeric(8).toUpperCase());
        Optional<Customer> customer=customerRepository.findById(invoiceDto.getCustomer_id());
        invoice.setCustomer(customer.get());
        return invoiceRepository.save(invoice);
    }

    @Override
    public Page<Invoice> getInvoices(int page, int size) {
        return invoiceRepository.findAll(of(page, size));
    }

    @Override
    public void addInvoiceToCustomer(Long id, Invoice invoice) {
        invoice.setInvoiceNumber(randomAlphanumeric(8).toUpperCase());
        Customer customer = customerRepository.findById(id).get();
        invoice.setCustomer(customer);
        invoiceRepository.save(invoice);
    }

    @Override
    public Invoice getInvoice(Long id) {
        return invoiceRepository.findById(id).get();
    }

    @Override
    public Stats getStats() {
        List<Object[]> result = customerRepository.getRawStatistics();
        // Assuming the result contains one row with three columns
        Object[] data = result.get(0);
        // Handle null values with default values
        int totalCustomers = data[0] != null ? ((Number) data[0]).intValue() : 0;
        int totalInvoices = data[1] != null ? ((Number) data[1]).intValue() : 0;
        double totalBilled = data[2] != null ? ((Number) data[2]).doubleValue() : 0.0;
        return new Stats(totalCustomers, totalInvoices, totalBilled);
    }


}
