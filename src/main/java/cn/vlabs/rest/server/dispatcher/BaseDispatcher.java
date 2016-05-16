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
package cn.vlabs.rest.server.dispatcher;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import cn.vlabs.rest.protocal.Envelope;
import cn.vlabs.rest.server.Dispatcher;
import cn.vlabs.rest.server.Predefined;

public abstract class BaseDispatcher implements Dispatcher {
	private Logger log=Logger.getLogger(BaseDispatcher.class);
	protected Envelope requestXMLNotFound(HttpServletRequest request){
		log.error("Error parse request, due to input xml(named in 'RequestXML') is not found.");
		log.error(dumpRequest(request));
		return Predefined.PARSE_ERROR;
	}
	private String dumpRequest(HttpServletRequest request){
		StringBuilder buff = new StringBuilder();
		buff.append("Parameter Map:\n");
		Enumeration<String> iter = request.getParameterNames();
		while (iter.hasMoreElements()){
			String key = (String)iter.nextElement();
			String[] values = request.getParameterValues(key);
			buff.append(key).append(":");
			buff.append('[');
			if (values!=null){
				boolean first =true;
				for (String value:values){
					if (!first){
						buff.append(",{"+value+"}");
					}else{
						buff.append("{"+value+"}");
						first=false;
					}
				}
			}
			buff.append("]\n");
		}
		return buff.toString();
	}
}
