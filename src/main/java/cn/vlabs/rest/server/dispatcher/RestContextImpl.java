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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import javax.servlet.ServletContext;

import cn.vlabs.rest.server.Capability;
import cn.vlabs.rest.server.config.ParamItem;
import cn.vlabs.rest.server.filter.RestContext;

public class RestContextImpl implements RestContext {
	private ServletContext context;

	private HashMap<String, LinkedList<String>> params;

	public RestContextImpl(ServletContext context,
			ArrayList<ParamItem> paramItems) {
		this.context = context;
		params = new HashMap<String, LinkedList<String>>();
		for (ParamItem item : paramItems) {
			LinkedList<String> values = params.get(item.getName());
			if (values == null) {
				values = new LinkedList<String>();
				params.put(item.getName(), values);
			}
			values.add(item.getValue());
		}
	}

	public String getFramkeworkVersion() {
		return Capability.FrameVersoin;
	}

	public String getParameter(String param) {
		if (param != null) {
			LinkedList<String> values = params.get(param);
			if (values != null && values.size() > 0) {
				return values.get(0);
			}
		}
		return null;
	}

	public String[] getParameters(String param) {
		if (param != null) {
			LinkedList<String> values = params.get(param);
			if (values != null && values.size() > 0) {
				return values.toArray(new String[0]);
			}
		}
		return null;
	}

	public String getRealPath(String path) {
		return context.getRealPath(path);
	}
}
