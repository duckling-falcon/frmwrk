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

import org.apache.commons.httpclient.HttpException;

public class ConnectException extends ServiceException {
	public ConnectException(int code, HttpException e, String url) {
		super(code, e.getMessage());
		this.url=url;
	}
	public ConnectException(int code, Exception e, String url){
		super(code, e);
		this.url = url;
	}
	public ConnectException(int code, String message, String url) {
		super(code, message);
		this.url =url;
	}
	public String toString(){
		return "Error ocurred while access url "+url+". caused by \n"+getMessage();
	}
	private String url;
	private static final long serialVersionUID = 1L;

}
