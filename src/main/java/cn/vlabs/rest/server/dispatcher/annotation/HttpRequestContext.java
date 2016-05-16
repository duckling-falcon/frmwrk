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
package cn.vlabs.rest.server.dispatcher.annotation;

import javax.servlet.http.HttpServletRequest;

import cn.vlabs.rest.server.filter.RequestContext;

public class HttpRequestContext implements RequestContext {
	private HttpServletRequest request;
	private String serviceName;
	private String methodName;
	private String fullServiceName;
	public HttpRequestContext(HttpServletRequest request){
		this.request = request;
	}
	public void setService(String fullServiceName){
		int dotIndex = fullServiceName.indexOf('.');
		if (dotIndex!=-1){
			serviceName = fullServiceName.substring(0, dotIndex);
			methodName= fullServiceName.substring(dotIndex + 1);
		}else{
			serviceName=fullServiceName;
			methodName =null;
		}
		this.fullServiceName=fullServiceName;
	}
	public int getLocalPort() {
		return request.getLocalPort();
	}

	public String getRemoteAddr() {
		if (request.getHeader("x-forwarded-for") == null) {  
			 return request.getRemoteAddr();  
		}  
		return request.getHeader("x-forwarded-for");  
	}

	public String getRemoteHost() {
		return request.getRemoteHost();
	}

	public int getRemotePort() {
		return request.getRemotePort();
	}
	public String getMethodName() {
		return methodName;
	}
	public String getServiceName() {
		return serviceName;
	}
	public String getFullServiceName() {
		return fullServiceName;
	}
}
