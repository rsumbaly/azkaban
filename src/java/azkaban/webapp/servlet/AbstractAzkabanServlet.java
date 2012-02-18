/*
 * Copyright 2012 LinkedIn, Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package azkaban.webapp.servlet;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import azkaban.webapp.AzkabanWebServer;

/**
 * Base Servlet for pages
 */
public class AbstractAzkabanServlet extends HttpServlet {
    private static final long serialVersionUID = -1;
    public static final String DEFAULT_LOG_URL_PREFIX = "predefined_log_url_prefix";
    public static final String LOG_URL_PREFIX = "log_url_prefix";

    private AzkabanWebServer application;

    /**
     * To retrieve the application for the servlet
     * @return
     */
    public AzkabanWebServer getApplication() {
        return application;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        application = (AzkabanWebServer) config.getServletContext().getAttribute(
                AzkabanServletContextListener.AZKABAN_SERVLET_CONTEXT_KEY);

        if (application == null) {
            throw new IllegalStateException("No batch application is defined in the servlet context!");
        }
    }

    /**
     * Checks for the existance of the parameter in the request
     * 
     * @param request
     * @param param
     * @return
     */
    public boolean hasParam(HttpServletRequest request, String param) {
        return request.getParameter(param) != null;
    }

    /**
     * Retrieves the param from the http servlet request. Will throw an exception if not
     * found
     * 
     * @param request
     * @param name
     * @return
     * @throws ServletException
     */
    public String getParam(HttpServletRequest request, String name) throws ServletException {
        String p = request.getParameter(name);
        if (p == null || p.equals("")) throw new ServletException("Missing required parameter '" + name + "'.");
        else return p;
    }

    /**
     * Returns the param and parses it into an int. Will throw an exception if not found, or
     * a parse error if the type is incorrect.
     * 
     * @param request
     * @param name
     * @return
     * @throws ServletException
     */
    public int getIntParam(HttpServletRequest request, String name) throws ServletException {
        String p = getParam(request, name);
        return Integer.parseInt(p);
    }

    /**
     * Returns the session value of the request.
     * 
     * @param request
     * @param key
     * @param value
     */
    protected void setSessionValue(HttpServletRequest request, String key, Object value) {
        request.getSession(true).setAttribute(key, value);
    }

    /**
     * Adds a session value to the request
     * 
     * @param request
     * @param key
     * @param value
     */
    @SuppressWarnings("unchecked")
    protected void addSessionValue(HttpServletRequest request, String key, Object value) {
        List l = (List) request.getSession(true).getAttribute(key);
        if (l == null) l = new ArrayList();
        l.add(value);
        request.getSession(true).setAttribute(key, l);
    }

    /**
     * Adds an error message to the request
     * @param request
     * @param message
     */
    protected void addError(HttpServletRequest request, String message) {
        addSessionValue(request, "errors", message);
    }

    /**
     * Adds a message to the request
     * 
     * @param request
     * @param message
     */
    protected void addMessage(HttpServletRequest request, String message) {
        addSessionValue(request, "messages", message);
    }

    /**
     * Creates a new velocity page to use.
     * 
     * @param req
     * @param resp
     * @param template
     * @return
     */
    protected Page newPage(HttpServletRequest req, HttpServletResponse resp, String template) {
        Page page = new Page(req, resp, application.getVelocityEngine(), template);
        return page;
    }

    /**
     * Retrieve the Azkaban application
     * @param config
     * @return
     */
    public static AzkabanWebServer getApp(ServletConfig config) {
        AzkabanWebServer app = (AzkabanWebServer) config
                .getServletContext()
                .getAttribute(
                        AzkabanServletContextListener.AZKABAN_SERVLET_CONTEXT_KEY);

        if (app == null) {
            throw new IllegalStateException("No batch application is defined in the servlet context!");
        }
        else {
            return app;
        }
    }
}
