/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2020 Ruhr University Bochum, Paderborn University,
 * and Hackmanit GmbH
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlsattacker.util;

public class FixedTimeProvider extends TimeProvider {

    private long fixedTime;

    public FixedTimeProvider(long fixedTime) {
        this.fixedTime = fixedTime;
    }

    @Override
    public long getTime() {
        return fixedTime;
    }

    public void setFixedTime(long fixedTime) {
        this.fixedTime = fixedTime;
    }

}
