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

import cn.vlabs.rest.ServiceClient;
import cn.vlabs.rest.ServiceContext;
import cn.vlabs.rest.ServiceException;

public class SystemService {
	public SystemService(ServiceContext context){
		client = new ServiceClient(context);
	}
	
	public String echo(String msg) throws ServiceException{
		return (String) client.sendService("System.echo", msg);
	}
	public String getFrameWorkInfo() throws ServiceException{
		return (String) client.sendService("System.version",null);
	}
	public String testSession(String message) throws ServiceException{
		return (String)client.sendService("System.testSession", message);
	}
	public int add(int i, int j) throws ServiceException {
		return ((Integer)client.sendService("System.add", new Object[]{i,j})).intValue();
	}
	private ServiceClient client;
}
