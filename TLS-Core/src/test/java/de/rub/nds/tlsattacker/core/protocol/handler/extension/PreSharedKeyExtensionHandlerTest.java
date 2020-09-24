package de.rub.nds.tlsattacker.core.protocol.handler.extension;

import de.rub.nds.tlsattacker.core.connection.InboundConnection;
import de.rub.nds.tlsattacker.core.constants.CipherSuite;
import de.rub.nds.tlsattacker.core.protocol.message.extension.PreSharedKeyExtensionMessage;
import de.rub.nds.tlsattacker.core.protocol.message.extension.psk.PSKIdentity;
import de.rub.nds.tlsattacker.core.protocol.message.extension.psk.PskSet;
import de.rub.nds.tlsattacker.core.protocol.parser.extension.PWDClearExtensionParser;
import de.rub.nds.tlsattacker.core.protocol.parser.extension.PreSharedKeyExtensionParser;
import de.rub.nds.tlsattacker.core.protocol.preparator.extension.PreSharedKeyExtensionPreparator;
import de.rub.nds.tlsattacker.core.protocol.serializer.extension.PreSharedKeyExtensionSerializer;
import de.rub.nds.tlsattacker.core.state.TlsContext;
import de.rub.nds.tlsattacker.core.workflow.chooser.Chooser;
import de.rub.nds.tlsattacker.transport.ConnectionEndType;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class PreSharedKeyExtensionHandlerTest {

    private PreSharedKeyExtensionHandler handler;
    private TlsContext context;
    private PskSet pskSet1;
    private PskSet pskSet2;
    private List<PskSet> pskSetList;

    @Before
    public void setUp()
    {
        context = new TlsContext();

        pskSet1 = new PskSet(new byte[] { 0x00 }, new byte[] { 0x00 }, "0", new byte[] { 0x00 }, CipherSuite.TLS_DHE_PSK_WITH_AES_128_CBC_SHA);
        pskSet2 = new PskSet(new byte[] { 0x01}, new byte[] { 0x01 }, "1", new byte[] { 0x01 }, CipherSuite.TLS_DHE_PSK_WITH_3DES_EDE_CBC_SHA);
        List<PskSet> pskSetList = new ArrayList<PskSet>();
        pskSetList.add(pskSet1);
        pskSetList.add(pskSet2);
        context.setPskSets(pskSetList);

        handler = new PreSharedKeyExtensionHandler(context);
    }

    @Test
    public void testAdjustTlsContext()
    {
        int selectedIdentity = 1;
        PreSharedKeyExtensionMessage msg = new PreSharedKeyExtensionMessage();
        msg.setSelectedIdentity(selectedIdentity);
        handler.adjustTLSContext(msg);

        assertArrayEquals(context.getPsk(), pskSet2.getPreSharedKey());//context.getChooser().getPskSets().get(selectedIdentity).getPreSharedKey());
        assertTrue(context.getSelectedIdentityIndex() == selectedIdentity);
    }

    @Test
    public void testAdjustTlsContextWithoutSelectedIdentity()
    {
        PreSharedKeyExtensionMessage msg = new PreSharedKeyExtensionMessage();
        handler.adjustTLSContext(msg);

        assertArrayEquals(context.getEarlyDataPSKIdentity(), pskSet1.getPreSharedKeyIdentity());//context.getChooser().getPskSets().get(0).getPreSharedKeyIdentity());
        assertArrayEquals(context.getEarlyDataCipherSuite().getByteValue(), pskSet1.getCipherSuite().getByteValue());//context.getChooser().getPskSets().get(0).getCipherSuite().getByteValue());
    }

    @Test
    public void testAdjustTlsContextServerEndType()
    {
        context.setConnection(new InboundConnection());
        PreSharedKeyExtensionMessage msg = new PreSharedKeyExtensionMessage();

        PSKIdentity id1 = new PSKIdentity();
        PSKIdentity id2 = new PSKIdentity();
        id1.setIdentity(new byte[] { 0x03 });
        id2.setIdentity(new byte[] { 0x01});

        List<PSKIdentity> identityList = new ArrayList<PSKIdentity>();
        identityList.add(id1);
        identityList.add(id2);
        msg.setIdentities(identityList);

        handler.adjustTLSContext(msg);

        assertArrayEquals(context.getPsk(), pskSet2.getPreSharedKey());
        assertArrayEquals(context.getEarlyDataCipherSuite().getByteValue(), pskSet2.getCipherSuite().getByteValue());
        assertEquals(context.getSelectedIdentityIndex(), 1);
    }

    @Test
    public void testGetParser()
    {
        assertTrue(handler.getParser(new byte[0], 0) instanceof PreSharedKeyExtensionParser);
    }

    @Test
    public void testGetPreparator()
    {
        assertTrue(handler.getPreparator(new PreSharedKeyExtensionMessage()) instanceof PreSharedKeyExtensionPreparator);
    }

    @Test
    public void testGetSerializer()
    {
        assertTrue(handler.getSerializer(new PreSharedKeyExtensionMessage()) instanceof PreSharedKeyExtensionSerializer);
    }
}
