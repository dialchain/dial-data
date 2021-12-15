-- btc_address
CREATE INDEX address_idx ON btc_address (ADDRESS);
CREATE INDEX txId_idx ON btc_address (TX_ID);
CREATE INDEX blockId_idx ON btc_address (BLOCK_ID);
