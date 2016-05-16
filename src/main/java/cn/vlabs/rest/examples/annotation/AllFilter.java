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

import java.lang.reflect.Method;

import cn.vlabs.rest.RestSession;
import cn.vlabs.rest.protocal.Envelope;
import cn.vlabs.rest.server.Predefined;
import cn.vlabs.rest.server.filter.Filter;
import cn.vlabs.rest.server.filter.RequestContext;
import cn.vlabs.rest.server.filter.RestContext;

public class AllFilter implements Filter{
	private String service;

	public void destroy() {
		
	}

	public void init(RestContext context) {
		service = context.getParameter("block");
	}

	public Envelope doFilter(Method method, Object[] params, RequestContext context, RestSession session) {
		if (service.equals(context.getFullServiceName())){
			System.out.println(context.getFullServiceName());
			System.out.println("Blocked");
			return Predefined.serviceNotFound(context.getFullServiceName());
		}
		return null;
	}
}
