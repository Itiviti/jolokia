package org.jolokia.server.core.osgi;

/*
 * Copyright 2009-2011 Roland Huss
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import java.lang.instrument.Instrumentation;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.servlet.*;

import org.easymock.EasyMock;
import org.jolokia.server.core.config.ConfigKey;
import org.jolokia.server.core.config.StaticConfiguration;
import org.jolokia.server.core.detector.ServerDetectorLookup;
import org.jolokia.server.core.restrictor.AllowAllRestrictor;
import org.jolokia.server.core.service.api.JolokiaService;
import org.jolokia.server.core.service.api.LogHandler;
import org.jolokia.test.util.HttpTestUtil;
import org.osgi.framework.*;
import org.osgi.framework.Filter;
import org.osgi.service.log.LogService;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.*;

/**
 * @author roland
 * @since 02.09.11
 */
public class OsgiAgentServletTest {

    private ServletConfig config;
    private ServletContext servletContext;
    private BundleContext bundleContext;
    private OsgiAgentServlet servlet;


    @BeforeMethod
    public void setup() {

        config = createMock(ServletConfig.class);
        servletContext = createMock(ServletContext.class);
        bundleContext = createMock(BundleContext.class);

        expect(config.getServletContext()).andReturn(servletContext).anyTimes();
        expect(config.getServletName()).andReturn("jolokia").anyTimes();
    }

    @Test
    public void simpleInit() throws ServletException, InvalidSyntaxException {
        servlet = new OsgiAgentServlet();
        initWithLogService();
    }

    @Test
    public void simpleInitWithGivenBundleContext() throws InvalidSyntaxException, ServletException {
        servlet = new OsgiAgentServlet(bundleContext,new AllowAllRestrictor());
        initWithLogService();
    }


    @Test
    public void initWithoutBundleContext() throws ServletException {
        servlet = new OsgiAgentServlet();

        expect(servletContext.getAttribute("osgi-bundlecontext")).andReturn(null).anyTimes();
        HttpTestUtil.prepareServletConfigMock(config);
        HttpTestUtil.prepareServletContextMock(servletContext);
        preparePlainLogging();
        replay(servletContext,config);
        servlet.init(config);
        servlet.destroy();

    }

    private void initWithLogService() throws InvalidSyntaxException, ServletException {
        prepareServiceLookup();
        HttpTestUtil.prepareServletConfigMock(config);
        HttpTestUtil.prepareServletContextMock(servletContext);
        preparePlainLogging();

        replay(config, servletContext, bundleContext);

        servlet.init(config);

        LogHandler handler = servlet.createLogHandler(config, new StaticConfiguration(ConfigKey.DEBUG,"true"));
        handler.debug("Debug");
        handler.info("Info");
        handler.error("Error",new Exception());
        destroyServlet();
    }




    private void destroyServlet() {
        reset(bundleContext);
        bundleContext.removeServiceListener(EasyMock.<ServiceListener>anyObject());
        expectLastCall().asStub();
        replay(bundleContext);
        servlet.destroy();
    }

    // ===========================================================================

    private void prepareServiceLookup() throws InvalidSyntaxException {
        expect(servletContext.getAttribute("osgi-bundlecontext")).andStubReturn(bundleContext);
        addServiceLookup(LogService.class);
        addServiceLookup(JolokiaService.class);
        addServiceLookup(ServerDetectorLookup.class);
    }

    private void addServiceLookup(Class pLogServiceClass) throws InvalidSyntaxException {
        expect(bundleContext.createFilter("(objectClass=" + pLogServiceClass.getName()+ ")"))
                .andStubReturn((Filter) createMock(Filter.class));
        bundleContext.addServiceListener(EasyMock.<ServiceListener>anyObject(), eq("(objectClass=" + pLogServiceClass.getName() + ")"));
        expectLastCall().asStub();
        expect(bundleContext.getServiceReferences(pLogServiceClass.getName(),null)).andStubReturn(null);
    }

    private void preparePlainLogging() {
        servletContext.log(EasyMock.<String>anyObject());
        servletContext.log("jolokia: Debug");
        servletContext.log("jolokia: Info");
        servletContext.log(eq("jolokia: Error"),isA(Exception.class));
        expectLastCall().anyTimes();
    }
}
