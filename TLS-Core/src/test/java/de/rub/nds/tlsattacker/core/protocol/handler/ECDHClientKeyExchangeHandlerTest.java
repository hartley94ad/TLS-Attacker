/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2017 Ruhr University Bochum / Hackmanit GmbH
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlsattacker.core.protocol.handler;

import de.rub.nds.tlsattacker.core.protocol.handler.ECDHClientKeyExchangeHandler;
import de.rub.nds.tlsattacker.core.protocol.message.DHEServerKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ECDHClientKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.parser.ECDHClientKeyExchangeParser;
import de.rub.nds.tlsattacker.core.protocol.preparator.ECDHClientKeyExchangePreparator;
import de.rub.nds.tlsattacker.core.protocol.serializer.ECDHClientKeyExchangeSerializer;
import de.rub.nds.tlsattacker.core.workflow.TlsContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Robert Merget - robert.merget@rub.de
 */
public class ECDHClientKeyExchangeHandlerTest {

    private ECDHClientKeyExchangeHandler handler;
    private TlsContext context;

    public ECDHClientKeyExchangeHandlerTest() {
    }

    @Before
    public void setUp() {
        context = new TlsContext();
        handler = new ECDHClientKeyExchangeHandler(context);

    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getParser method, of class ECDHClientKeyExchangeHandler.
     */
    @Test
    public void testGetParser() {
        assertTrue(handler.getParser(new byte[1], 0) instanceof ECDHClientKeyExchangeParser);
    }

    /**
     * Test of getPreparator method, of class ECDHClientKeyExchangeHandler.
     */
    @Test
    public void testGetPreparator() {
        assertTrue(handler.getPreparator(new ECDHClientKeyExchangeMessage()) instanceof ECDHClientKeyExchangePreparator);
    }

    /**
     * Test of getSerializer method, of class ECDHClientKeyExchangeHandler.
     */
    @Test
    public void testGetSerializer() {
        assertTrue(handler.getSerializer(new ECDHClientKeyExchangeMessage()) instanceof ECDHClientKeyExchangeSerializer);
    }

    /**
     * Test of adjustTLSContext method, of class ECDHClientKeyExchangeHandler.
     */
    @Test
    public void testAdjustTLSContext() {
        ECDHClientKeyExchangeMessage message = new ECDHClientKeyExchangeMessage();
        message.prepareComputations();
        message.getComputations().setPremasterSecret(new byte[] { 0, 1, 2, 3 });
        message.getComputations().setMasterSecret(new byte[] { 4, 5, 6 });
        handler.adjustTLSContext(message);
        assertArrayEquals(new byte[] { 0, 1, 2, 3 }, context.getPreMasterSecret());
        assertArrayEquals(new byte[] { 4, 5, 6 }, context.getMasterSecret());
    }
}