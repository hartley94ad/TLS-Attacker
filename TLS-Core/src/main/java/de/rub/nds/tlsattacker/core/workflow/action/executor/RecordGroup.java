/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2020 Ruhr University Bochum, Paderborn University,
 * and Hackmanit GmbH
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlsattacker.core.workflow.action.executor;

import de.rub.nds.tlsattacker.core.constants.ProtocolMessageType;
import de.rub.nds.tlsattacker.core.record.AbstractRecord;
import de.rub.nds.tlsattacker.core.record.Record;
import de.rub.nds.tlsattacker.core.record.RecordCryptoComputations;
import de.rub.nds.tlsattacker.core.state.TlsContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A RecordGroup comprises records which should be decrypted/processed together.
 * They share the following characteristics: 1. they are of the same type (blob
 * or real records); 2. they use the same cipher state (for DTLS, this means
 * they have the same epoch); 3. their content is of the same type.
 *
 * The class also provides functionality for grouping records into RecordGroups,
 * decrypting and parsing them.
 */
public class RecordGroup {

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Arranges a list of records into RecordGroups.
     */
    // one could implement this into a RecordSplitter class
    static final List<RecordGroup> generateRecordGroups(List<AbstractRecord> records) {
        List<RecordGroup> recordGroups = new LinkedList<>();
        if (records.isEmpty()) {
            return recordGroups;
        }

        RecordGroup group = new RecordGroup();
        recordGroups.add(group);
        for (AbstractRecord record : records) {
            if (!group.addRecord(record)) {
                group = new RecordGroup();
                recordGroups.add(group);
                group.addRecord(record);
            }
        }

        return recordGroups;
    }

    private final List<AbstractRecord> records;

    private RecordGroup() {
        records = new LinkedList<>();
    }

    private RecordGroup(List<AbstractRecord> records) {
        this.records = records;
    }

    public ProtocolMessageType getProtocolMessageType() {
        if (!records.isEmpty()) {
            return ProtocolMessageType.getContentType(records.get(0).getContentMessageType().getValue());
        }
        return null;
    }

    public Integer getDtlsEpoch() {
        if (!records.isEmpty()) {
            return getRecordEpoch(records.get(0));
        }
        return null;
    }

    private Integer getRecordEpoch(AbstractRecord record) {
        if (record instanceof Record && ((Record) record).getEpoch() != null) {
            return ((Record) record).getEpoch().getValue();
        }
        return null;
    }

    public List<AbstractRecord> getRecords() {
        return Collections.unmodifiableList(records);
    }

    public byte[] getCleanBytes() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        for (AbstractRecord record : getRecords()) {
            try {
                stream.write(record.getCleanProtocolMessageBytes().getValue());
            } catch (IOException ex) {
                LOGGER.warn("Could not write CleanProtocolMessage bytes to Array");
                LOGGER.debug(ex);
            }
        }
        return stream.toByteArray();
    }

    public void decryptRecords(TlsContext context) {
        for (AbstractRecord record : getRecords()) {
            context.getRecordLayer().decryptAndDecompressRecord(record);
        }
    }

    public void adjustContext(TlsContext context) {
        for (AbstractRecord record : getRecords()) {
            record.adjustContext(context);
        }
    }

    private boolean addRecord(AbstractRecord record) {
        boolean isFitting = false;
        if (records.isEmpty()) {
            isFitting = true;
        } else {
            if (Objects.equals(record.getContentMessageType(), getProtocolMessageType())
                    && record.getClass().equals(records.get(0).getClass())) {
                if (getDtlsEpoch() != null) {
                    if (Objects.equals(getRecordEpoch(record), getDtlsEpoch())) {
                        isFitting = true;
                    }
                } else {
                    isFitting = true;
                }
            }
        }

        if (isFitting) {
            records.add(record);
        }
        return isFitting;
    }

    public boolean areAllRecordsValid() {
        for (AbstractRecord record : records) {
            if (isRecordInvalid(record)) {
                return false;
            }
        }
        return true;
    }

    private boolean isRecordInvalid(AbstractRecord record) {
        if (record instanceof Record) {
            RecordCryptoComputations computations = ((Record) record).getComputations();
            if (computations != null
                    && (Objects.equals(computations.getMacValid(), Boolean.FALSE)
                            || Objects.equals(computations.getPaddingValid(), Boolean.FALSE) || Objects.equals(
                            computations.getAuthenticationTagValid(), Boolean.FALSE))) {
                return true;
            }
        }
        return false;
    }

    /**
     * If the parsed record group contains invalid records we need to seperate
     * them into smaller groups and only parse them one by one to make sure we
     * can respect invalidAsUnknown flags
     *
     * @return
     */
    public List<RecordGroup> splitIntoProcessableSubgroups() {
        List<RecordGroup> recordGroupList = new LinkedList<>();
        if (areAllRecordsValid()) {
            recordGroupList.add(this);
            return recordGroupList;
        } else {
            List<AbstractRecord> recordList = new LinkedList<>();
            Boolean valid = null;
            for (AbstractRecord record : recordList) {
                boolean tempValid = isRecordInvalid(record);
                if (valid == null || Objects.equals(tempValid, valid)) {
                    valid = tempValid;
                    recordList.add(record);
                } else {
                    recordGroupList.add(new RecordGroup(recordList));
                    valid = tempValid;
                    recordList = new LinkedList<>();
                    recordList.add(record);
                }
            }
            if (!recordList.isEmpty()) {
                recordGroupList.add(new RecordGroup(recordList));
            }
        }
        return recordGroupList;

    }
}
