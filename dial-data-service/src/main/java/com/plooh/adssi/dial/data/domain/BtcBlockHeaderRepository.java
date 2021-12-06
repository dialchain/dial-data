package com.plooh.adssi.dial.data.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface BtcBlockHeaderRepository extends JpaRepository<BtcBlockHeader, String> {

    List<BtcBlockHeader> findByHeightBetween(int startHeight, int endHeight);

    List<BtcBlockHeader> findByTimeGreaterThanEqual(int startTime, Pageable pageable);

    @Query(value = "SELECT b FROM BtcBlockHeader b left join b.txIds as txId where txId =:txId AND b.height IS NOT NULL")
    Optional<BtcBlockHeader> findByTxId(@Param("txId") String txId);

}