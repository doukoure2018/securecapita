package io.getarrayus.securecapita.repository;

import io.getarrayus.securecapita.domain.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;


public interface CustomerRepository extends PagingAndSortingRepository<Customer, Long>, ListCrudRepository<Customer, Long> {
    Page<Customer> findByNameContaining(String name, Pageable pageable);

    @Query(value = "SELECT " +
            "(SELECT COUNT(*) FROM customer) AS totalCustomers, " +
            "(SELECT COUNT(*) FROM invoice) AS totalInvoices, " +
            "(SELECT ROUND(SUM(total), 2) FROM invoice) AS totalBilled",
            nativeQuery = true)
    List<Object[]> getRawStatistics();

}
