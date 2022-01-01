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

package cn.vlabs.rest.examples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import cn.vlabs.rest.ServiceClient;
import cn.vlabs.rest.ServiceContext;
import cn.vlabs.rest.ServiceException;
import cn.vlabs.rest.IFileSaver;
import cn.vlabs.rest.stream.StreamInfo;

public class SystemServiceIT {
    private ServiceContext context;
    private ServiceClient client;

    @Before
    public void setUp() throws Exception {
        ServiceContext.setMaxConnection(5, 10);
        context = new ServiceContext(
            "http://localhost:8080/frmwrk/Service");
        context.setKeepAlive(true);
        client = new ServiceClient(context);
    }
    
    @After
    public void tearDown() throws Exception {
    }
    
    @Test
    public void testMultiThread()
            throws ServiceException, InterruptedException {
        MultiThreadRunner runner = new MultiThreadRunner(
            1000,
            new RunnableFactory() {
                public Runnable createRunnable(int index) {
                    return new JobRunner(client);
                }
            });
        long startTime = System.currentTimeMillis();
        try {
            runner.start();
            System.out.println((System.currentTimeMillis() - startTime) / 3000.0);
        } finally {
            context.close();
        }
    }
    
    private static class JobRunner implements Runnable {
        private ServiceClient client;
	
        public JobRunner(ServiceClient client) {
            this.client = client;
        }
	
        public void run() {
            for (int i = 0; i < 3; i++) {
                try {
                    assertEquals(
                        "ABC",
                        (String)client.sendService("System.echo", "ABC"));
                } catch (ServiceException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    @Test
    public void testEcho() throws ServiceException, InterruptedException {
        assertEquals("中文",
                     (String)client.sendService("System.echo", "中文"));
        assertEquals("true",
                     (String)client.sendService("System.echo", "true"));
    }
    
    @Test
    public void testPerformance() throws ServiceException {
        long before, after;
        int count = 3000;
        try {
            before = System.currentTimeMillis();
            for (int i = 0; i < count; i++) {
                client.sendService("System.echo", "ABC");
            }
            after = System.currentTimeMillis();
        } finally {
            context.close();
        }
        System.out.println("the average consumed time is "
                           + ((after - before) / (float) count));
    }
    
    @Test
    public void testGetFrameWorkInfo() throws ServiceException {
        String version = (String)client.sendService("System.version", null);
        System.out.println("Framework Version: " + version);
    }
    
    @Test
    public void testSession() throws ServiceException {
        Object obj;
        assertNotNull(obj = client.sendService("System.testSession", null));
        System.out.println("Session is " + (String)obj);
    }
    
    @Test
    public void testAdd() throws ServiceException {
        assertEquals(
            new Integer(5),
            (Integer)client.sendService("System.add", new Object[]{2,3}));
    }

    @Test
    public void testUploadFile() throws ServiceException, IOException {
        File f = new File("target/test-classes/upload1.txt");
        StreamInfo stream = new StreamInfo();
        stream.setFilename(f.getName());
        stream.setInputStream(new FileInputStream(f));
        stream.setLength(f.length());
        String result = (String)client.sendService(
            "FileTest.readFile", "注意", stream);
        stream.getInputStream().close();

        assertEquals("注意不是所有的付出都有回报！", result);
    }

    //
    // rewrite <2021-12-30 Thu>
    // 
    @Test
    public void testDownloadFile() throws ServiceException, IOException {
        File f = new File("target/test-classes/download1.txt");
        DemoSaver saver = new DemoSaver();
        client.sendService("FileTest.writeFile",
                           f.getAbsolutePath(),
                           saver);
        String result = saver.getFirstLine();

        assertEquals("原谅我这一生放纵不羁爱自由——", result);
    }

    private static class DemoSaver implements IFileSaver {
        private String firstLine;
        public void save(String filename, InputStream in) {
            System.out.println("DemoSaver got the filename: "+ filename);
            try {
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in, "utf-8"));
                firstLine = reader.readLine();
            } catch (UnsupportedEncodingException e) {
                firstLine = null;
            } catch (IOException e) {
                firstLine = null;
            }
        }
        
        public String getFirstLine() {
            return firstLine;
        }
    }
    
}
