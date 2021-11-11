package com.plooh.adssi.dial.data.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface BtcBlockRepository extends JpaRepository<BtcBlock, String> {
}
