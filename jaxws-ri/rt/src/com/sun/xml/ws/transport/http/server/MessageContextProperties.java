/*
 * $Id: MessageContextProperties.java,v 1.1 2005-05-23 23:01:38 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.transport.http.server;

/**
 *
 * @author JAX-RPC Development Team
 */
public interface MessageContextProperties {

    public static final String SERVLET_CONTEXT =
        "com.sun.xml.rpc.server.http.ServletContext";
    public static final String HTTP_SERVLET_REQUEST =
        "com.sun.xml.rpc.server.http.HttpServletRequest";
    public static final String HTTP_SERVLET_RESPONSE =
        "com.sun.xml.rpc.server.http.HttpServletResponse";
    public static final String IMPLEMENTOR =
        "com.sun.xml.rpc.server.http.Implementor";
    public static final String ONE_WAY_OPERATION =
        "com.sun.xml.rpc.server.OneWayOperation";
    public static final String CLIENT_BAD_REQUEST =
        "com.sun.xml.rpc.server.http.ClientBadRequest";
    public static final String CLIENT_INVALID_CONTENT_TYPE =
        "com.sun.xml.rpc.server.http.ClientInvalidContentType";
}
