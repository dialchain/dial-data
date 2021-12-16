package com.plooh.adssi.dial.data.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

@Repository
@Transactional
public interface BtcAddressRepository extends JpaRepository<BtcAddress, UUID> {

    List<BtcAddress> findByAddress(String address, Pageable pageable);

}
