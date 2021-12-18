/*
 * Copyright by the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.plooh.adssi.dial.data.repository;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import javax.annotation.Nullable;

import org.bitcoinj.core.Block;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.SPVBlockStore;
import org.fusesource.leveldbjni.JniDBFactory;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBFactory;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.WriteBatch;

/**
 * An SPV block store that writes every header it sees to a
 * <a href="https://github.com/fusesource/leveldbjni">LevelDB</a>.
 * This allows for fast lookup of block headers by block hash at the expense of
 * more costly inserts and higher disk
 * usage than the {@link SPVBlockStore}. If all you want is a regular wallet you
 * don't need this class: it exists for
 * specialised applications where you need to quickly verify a standalone SPV
 * proof.
 */
public class DialBtcBlockStore {
    // private static final byte[] CHAIN_HEAD_KEY = "chainhead".getBytes();

    private final Context context;
    private DB db;
    // private final ByteBuffer buffer =
    // ByteBuffer.allocate(StoredBlock.COMPACT_SERIALIZED_SIZE);
    private final File path;

    /** Creates a LevelDB SPV block store using the JNI/C++ version of LevelDB. */
    public DialBtcBlockStore(Context context, File directory) throws BlockStoreException {
        this(context, directory, JniDBFactory.factory);
    }

    /**
     * Creates a LevelDB SPV block store using the given factory, which is useful if
     * you want a pure Java version.
     */
    public DialBtcBlockStore(Context context, File directory, DBFactory dbFactory) throws BlockStoreException {
        this.context = context;
        this.path = directory;
        Options options = new Options();
        options.createIfMissing();

        try {
            tryOpen(directory, dbFactory, options);
        } catch (IOException e) {
            try {
                dbFactory.repair(directory, options);
                tryOpen(directory, dbFactory, options);
            } catch (IOException e1) {
                throw new BlockStoreException(e1);
            }
        }
    }

    private synchronized void tryOpen(File directory, DBFactory dbFactory, Options options)
            throws IOException {
        db = dbFactory.open(directory, options);
        initStoreIfNeeded();
    }

    private synchronized void initStoreIfNeeded() {
        Block genesis = context.getParams().getGenesisBlock().cloneAsHeader();
        if (db.get(genesis.getHash().getBytes()) != null)
            return;// Already initialised.
        db.put(genesis.getHash().getBytes(), genesis.unsafeBitcoinSerialize());
    }

    public synchronized void put(byte[] key, byte[] value) {
        db.put(key, value);
    }

    @Nullable
    public synchronized Optional<byte[]> get(byte[] key) {
        return Optional.ofNullable(db.get(key));
    }

    public synchronized void close() throws BlockStoreException {
        try {
            db.close();
        } catch (IOException e) {
            throw new BlockStoreException(e);
        }
    }

    /**
     * Erases the contents of the database (but NOT the underlying files themselves)
     * and then reinitialises with the genesis block.
     */
    public synchronized void reset() throws BlockStoreException {
        try {
            try (WriteBatch batch = db.createWriteBatch(); DBIterator it = db.iterator()) {
                it.seekToFirst();
                while (it.hasNext())
                    batch.delete(it.next().getKey());
                db.write(batch);
            }
            initStoreIfNeeded();
        } catch (IOException e) {
            throw new BlockStoreException(e);
        }
    }

    public synchronized void destroy() throws IOException {
        JniDBFactory.factory.destroy(path, new Options());
    }

    public NetworkParameters getParams() {
        return context.getParams();
    }
}
