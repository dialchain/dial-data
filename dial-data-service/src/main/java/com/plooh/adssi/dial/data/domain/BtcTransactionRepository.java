package com.plooh.adssi.dial.data.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface BtcTransactionRepository extends JpaRepository<BtcTransaction, UUID> {

    Optional<BtcTransaction> findByTxId(String txId);

}
