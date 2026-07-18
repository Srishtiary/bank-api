package com.bank.bank_api.repository;

import com.bank.bank_api.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByAccountIdOrderByTimestampDesc(Long accountId, Pageable pageable);
}
