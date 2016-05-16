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
package cn.vlabs.rest.server.config;

import java.util.ArrayList;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

public class FilterItem {
	@XStreamAlias("name")
	private String name;

	@XStreamAlias("class")
	private String clazz;

	@XStreamImplicit(itemFieldName = "init-param")
	private ArrayList<ParamItem> initparams;
	
	public FilterItem(){}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setClazz(String clazz) {
		this.clazz = clazz;
	}

	public String getClazz() {
		return clazz;
	}

	public void setInitParams(ArrayList<ParamItem> initparams) {
		this.initparams = initparams;
	}

	public ArrayList<ParamItem> getInitParams() {
		return initparams;
	}
}
