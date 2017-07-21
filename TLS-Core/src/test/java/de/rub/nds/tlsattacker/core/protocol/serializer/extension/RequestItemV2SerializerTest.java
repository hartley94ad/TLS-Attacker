/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2017 Ruhr University Bochum / Hackmanit GmbH
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlsattacker.core.protocol.serializer.extension;

import de.rub.nds.modifiablevariable.util.ArrayConverter;
import de.rub.nds.tlsattacker.core.protocol.message.extension.certificatestatusrequestitemv2.RequestItemV2;
import de.rub.nds.tlsattacker.core.protocol.message.extension.certificatestatusrequestitemv2.ResponderId;
import java.util.Arrays;
import static org.junit.Assert.assertArrayEquals;
import org.junit.Test;

/**
 *
 * @author Matthias Terlinde <matthias.terlinde@rub.de>
 */
public class RequestItemV2SerializerTest {

    private final RequestItemV2 item = new RequestItemV2(1, (byte) 0x0E, 7, Arrays.asList(new ResponderId(5,
            new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05 })), 3, new byte[] { 0x01, 0x02, 0x03 }, new byte[] { 0x00, 0x05,
            0x01, 0x02, 0x03, 0x04, 0x05 });
    private final byte[] expectedBytes = ArrayConverter.hexStringToByteArray("01000E0007000501020304050003010203");

    @Test
    public void testSerializer() {
        RequestItemV2Serializer serializer = new RequestItemV2Serializer(item);

        assertArrayEquals(expectedBytes, serializer.serialize());
    }
}
