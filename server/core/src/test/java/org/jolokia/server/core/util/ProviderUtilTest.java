package org.jolokia.server.core.util;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author roland
 * @since 17.12.13
 */
public class ProviderUtilTest {

    @Test
    public void extractProviders() throws MalformedObjectNameException {

        String[] data = {
                "java.lang:type=memory", null,"java.lang:type=memory",
                "proxy@java.lang:type=memory", "proxy", "java.lang:type=memory",
                "spring@:name=myBean","spring",":name=myBean",
                "spring@spring@bla:type=blub","spring","spring@bla:type=blub"
        };
        for (int i = 0; i < data.length; i += 3) {
            for (ProviderUtil.ProviderObjectNamePair pair : new ProviderUtil.ProviderObjectNamePair[]
                    {
                        ProviderUtil.extractProvider(data[i]),
                        ProviderUtil.extractProvider(new ObjectName(data[i]))
                    }) {
                assertEquals(pair.getProvider(), data[i + 1], "Provider expected: " + data[i + 1]);
                assertEquals(pair.getObjectName(), new ObjectName(data[i + 2]), "Objectname exepcted: " + data[i + 1]);
            }
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class,expectedExceptionsMessageRegExp = ".*not be null.*")
    public void nullName() throws MalformedObjectNameException {
        ProviderUtil.extractProvider((String) null);
    }


    @Test
    public void matchesProvider() throws MalformedObjectNameException {
        String[] data = {
                null,"java.lang:type=memory","true",
                "proxy","java.lang:type=memory","false",
                "proxy","proxy@java.lang:type=memory","true",
                "spring","spring@proxy@java.lang:type=memory","true",
                null,":type=memory","true",
                "spring",":type=memory","false"
        };
        for (int i = 0; i < data.length; i += 3) {
            assertEquals(ProviderUtil.matchesProvider(data[i], new ObjectName(data[i + 1])), Boolean.parseBoolean(data[i + 2]),
                         data[i+1] + " matches provider " + data[i] + ": " + data[i+2]);
        }
    }
}
