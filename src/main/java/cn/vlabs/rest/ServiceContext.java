/*
 * Copyright (c) 2008-2016 Computer Network Information Center (CNIC), Chinese Academy of Sciences.
 * 
 * This file is part of Duckling project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 *
 */
package cn.vlabs.rest;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.protocol.Protocol;

import cn.vlabs.rest.ssl.EasySSLProtocolSocketFactory;

public class ServiceContext {
    private static final String HTTP_CONNECTION_TIMEOUT = "http.connection.timeout";


    private static final int DEFAULT_TIMEOUT = 30000;

    private static MultiThreadedHttpConnectionManager connManager;

    static {
        connManager = new MultiThreadedHttpConnectionManager();
        setMaxConnection(5, 5);
		Protocol easyhttps = new Protocol("https", new EasySSLProtocolSocketFactory(), 443);
        Protocol.registerProtocol("https", easyhttps);
    }

    public static void setMaxConnection(int connePerHost, int connTotal) {
        HttpConnectionManagerParams params = connManager.getParams();
        params.setConnectionTimeout(DEFAULT_TIMEOUT);
        params.setDefaultMaxConnectionsPerHost(connePerHost);
        params.setMaxTotalConnections(connTotal);
        connManager.setParams(params);
    }

    public static void shutdownAll() {
        MultiThreadedHttpConnectionManager.shutdownAll();
    }

    private boolean alive = false;

    @Deprecated
    private String appname;

    @Deprecated
    private String apppassword;

    private String charset = Constant.DEFAULT_CHARSET;

    /**
     * 每个ServiceClient拥有不同的Session值和Cookie值，因此每个Service都可以独立拥有一个变量
     */
    private HttpClient client;

    private String serverURL;
    public ServiceContext(){
    	alive = false;
    	client = new HttpClient(connManager);
    }
    public ServiceContext(String serverURL) {
        this.serverURL = serverURL;
        alive = false;
        client = new HttpClient(connManager);
    }

    @Deprecated
    public ServiceContext(String appname, String apppassword, String serverURL) {
        setAppname(appname);
        setApppassword(apppassword);
        setServerURL(serverURL);
        alive = false;
        client = new HttpClient(connManager);
    }

    public void close() {
        if (connManager != null)
            connManager.closeIdleConnections(0);
    }

    @Deprecated
    public String getAppname() {
        return appname;
    }

    @Deprecated
    public String getApppassword() {
        return apppassword;
    }

    public String getCharset() {
        return (charset == null ? Constant.DEFAULT_CHARSET : charset);
    }

    public String getServerURL() {
        return serverURL;
    }

    public boolean isKeepAlive() {
        return alive;
    }

    @Deprecated
    public void setAppname(String appname) {
        this.appname = appname;
    }

    @Deprecated
    public void setApppassword(String apppassword) {
        this.apppassword = apppassword;
    }

    public void setCharset(String charset) {
        if (charset != null) {
            this.charset = charset;
        }
    }

    public void setKeepAlive(boolean alive) {
        this.alive = alive;
    }

    public void setServerURL(String serverURL) {
        this.serverURL = serverURL;
        alive = false;
        client = new HttpClient(connManager);
    }

    public void setTimeout(int connTimeout) {
        client.getParams().setParameter(HTTP_CONNECTION_TIMEOUT, connTimeout);
    }

    public synchronized void shutdown() {
        connManager.shutdown();
    }

    HttpClient getClient() {
        return client;
    }
}
