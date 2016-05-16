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
package cn.vlabs.rest.protocal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.log4j.Logger;

import cn.vlabs.rest.server.dispatcher.claasic.ServiceDispatcher;
import cn.vlabs.rest.stream.IResource;
import cn.vlabs.rest.stream.StreamInfo;

public class ServiceRequest {
    public static ServiceRequest fromRequest(HttpServletRequest request) {
        if (isURLForm(request)) {
            return readUrlForm(request);
        } else {
            try {
                return parseInputStreamForm(request);
            } catch (FileUploadException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
            // return readMultiPartForm(request);
        }
    }

    private static boolean isURLForm(HttpServletRequest request) {
        return "application/x-www-form-urlencoded".equals(request.getContentType());
    }

    private static ServiceRequest readUrlForm(HttpServletRequest request) {
        ServiceRequest servRequest = new ServiceRequest();
        servRequest.setRequestXML(request.getParameter("RequestXML"));
        return servRequest;
    }

    private static Logger log = Logger.getLogger(ServiceDispatcher.class);

    private static ServiceRequest parseInputStreamForm(HttpServletRequest request) throws FileUploadException,IOException {
        ServiceRequest servRequest = new ServiceRequest();
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (isMultipart) {
            ServletFileUpload upload = new ServletFileUpload();
            FileItemIterator iter = upload.getItemIterator(request);
            log.info("Version 0.98c");
            long start = System.currentTimeMillis();
            while (iter.hasNext()) {
                FileItemStream item = iter.next();
                String name = item.getFieldName();
                InputStream stream = item.openStream();
                if (item.isFormField()) {
                    log.info(item.toString());
                    log.info("Form field " + name + " with value " + Streams.asString(stream) + " detected.");
                } else {
                    log.info("File field " + name + " with file name " + item.getName() + " detected.");
                    String fieldname = item.getFieldName();
                    if ("RequestXML".equals(fieldname)) {
                        servRequest.setRequestXML(readFromInputStream(stream, "UTF-8"));
                    } else if ("file".equals(fieldname)) {
                        StreamInfo info = new StreamInfo();
                        info.setFilename(item.getName());
                        info.setMimeType(item.getContentType());
                        info.setInputStream(stream);
                        info.setLength(-1);
                        servRequest.setStream(info);
                        break;
                    } else {
                        return null;
                    }
                }
            }
            long end = System.currentTimeMillis();
            log.info("parse input stream use time:"+(end - start) + " ms.");
        }
        return servRequest;
    }

    private static String readFromInputStream(InputStream stream, String encode) {
        StringBuffer sb = new StringBuffer();
        try {
            InputStreamReader reader = new InputStreamReader(stream, encode);
            BufferedReader br = new BufferedReader(reader);
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public void setStream(IResource stream) {
        this.stream = stream;
    }

    public IResource getStream() {
        return stream;
    }

    public void setRequestXML(String requestXML) {
        this.requestXML = requestXML;
    }

    public String getRequestXML() {
        return requestXML;
    }

    private String requestXML;
    private IResource stream;
}
