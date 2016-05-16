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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import cn.vlabs.rest.ServiceClient;
import cn.vlabs.rest.ServiceContext;
import cn.vlabs.rest.ServiceException;
import cn.vlabs.rest.stream.StreamInfo;

public class UploadStub {
	public UploadStub(ServiceContext context){
		this.context=context;
	}
	public String upload(String message, String file) throws ServiceException, IOException{
		ServiceClient client = new ServiceClient(context);
		StreamInfo stream = new StreamInfo();
		File f = new File(file);
		stream.setFilename(f.getName());
		stream.setInputStream(new FileInputStream(f));
		stream.setLength(f.length());
		String result =(String)client.sendService("System.upload", message, stream);
		stream.getInputStream().close();
		return result;
	}
	private ServiceContext context;
}
