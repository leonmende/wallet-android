package com.mycelium.wapi.wallet;

import com.google.common.base.Optional;
import com.mrd.bitlib.util.Sha256Hash;
import com.mycelium.wapi.wallet.coins.GenericAssetInfo;
import com.mycelium.wapi.wallet.coins.Value;

import java.io.Serializable;
import java.util.List;

public interface GenericTransaction extends Serializable {
    class GenericOutput implements Serializable {
        final GenericAddress genericAddress;
        final Value value;
        final boolean isCoinbase;

        public GenericOutput(GenericAddress genericAddress, Value value, boolean isCoinbase) {
            this.genericAddress = genericAddress;
            this.value = value;
            this.isCoinbase = isCoinbase;
        }

        public GenericAddress getAddress() {
            return genericAddress;
        }

        public Value getValue() {
            return value;
        }

        public boolean isCoinbase() {
            return isCoinbase;
        }
    }

    class GenericInput extends GenericOutput{
        public GenericInput(GenericAddress genericAddress, Value value, boolean isCoinbase) {
            super(genericAddress, value, isCoinbase);
        }
    }

    GenericAssetInfo getType();

    Sha256Hash getId();
    String getHashAsString();
    byte[] getHashBytes();
    byte[] getTxBytes();

    int getHeight();

    int getConfirmations();

    long getTimestamp();
    void setTimestamp(long timestamp);

    boolean isQueuedOutgoing();
    Optional<ConfirmationRiskProfileLocal> getConfirmationRiskProfile();

    Value getFee();

    List<GenericInput> getInputs();
    List<GenericOutput> getOutputs();
    GenericAddress getDestinationAddress();

    /**
     * Amount transferred to the corresponding account.
     * Can be positive (funds arrived to the account) or negative (funds are sent from the account)
     */
    Value getTransferred();

    boolean isIncoming();

    int getRawSize();
}
