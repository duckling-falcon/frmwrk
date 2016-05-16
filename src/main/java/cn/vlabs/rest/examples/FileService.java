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

import cn.vlabs.rest.IFileSaver;
import cn.vlabs.rest.ServiceClient;
import cn.vlabs.rest.ServiceContext;
import cn.vlabs.rest.ServiceException;
import cn.vlabs.rest.stream.StreamInfo;

public class FileService {
	private ServiceClient client;
	public FileService(ServiceContext context){
		client = new ServiceClient(context);
	}
	
	public String upload(String message, String file) throws ServiceException, IOException{
		StreamInfo stream = new StreamInfo();
		File f = new File(file);
		stream.setFilename(f.getName());
		stream.setInputStream(new FileInputStream(f));
		stream.setLength(f.length());
		String result =(String)client.sendService("FileTest.readFile", message, stream);
		stream.getInputStream().close();
		return result;
	}
	
	public String download() throws ServiceException{
		DemoSaver saver = new DemoSaver(){
			private String firstLine;
			public void save(String filename, InputStream in) {
				try {
					BufferedReader reader = new BufferedReader(new InputStreamReader(in, "utf-8"));
					firstLine = reader.readLine();
				} catch (UnsupportedEncodingException e) {
					firstLine=null;
				} catch (IOException e) {
					firstLine=null;
				}
			}
			public String getFirstLine(){
				return firstLine;
			}
		};
		client.sendService("FileTest.writeFile", null, saver);
		return saver.getFirstLine();
		
	}
	private static class DemoSaver implements IFileSaver{
		private String firstLine;
		public void save(String filename, InputStream in) {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(in, "utf-8"));
				firstLine = reader.readLine();
			} catch (UnsupportedEncodingException e) {
				firstLine=null;
			} catch (IOException e) {
				firstLine=null;
			}
		}
		public String getFirstLine(){
			return firstLine;
		}
	};
}
