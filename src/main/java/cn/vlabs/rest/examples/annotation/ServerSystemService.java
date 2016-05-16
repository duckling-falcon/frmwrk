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
package cn.vlabs.rest.examples.annotation;

import javax.servlet.ServletContext;

import cn.vlabs.rest.RestSession;
import cn.vlabs.rest.server.Capability;
import cn.vlabs.rest.server.annotation.Destroy;
import cn.vlabs.rest.server.annotation.Init;
import cn.vlabs.rest.server.annotation.RestMethod;

public class ServerSystemService {
	@Init
	public void init(ServletContext context){
	    // System.out.println("Init method is called. Context path ="+context.getContextPath());
	}
	
	@Destroy
	public void destroy(){
	    // System.out.println("destroy method is called.");
	}

	@RestMethod("echo")
	public String echo(String value){
		return value;
	}

	@RestMethod("version")
	public String getVersion(){
		return Capability.FrameVersoin;
	}

	@RestMethod("testSession")
	public String getMethod(RestSession session, String message){
		System.out.println(session.toString());
		return message;
	}

	@RestMethod("add")
	public int add(int a, int b){
		return a+b;
	}
}
