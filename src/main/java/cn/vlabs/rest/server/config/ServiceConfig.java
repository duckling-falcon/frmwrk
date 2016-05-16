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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("ServiceConfig")
public class ServiceConfig {
	@XStreamImplicit(itemFieldName = "service")
	private ArrayList<ServiceItem> services;

	@XStreamImplicit(itemFieldName = "filter")
	private ArrayList<FilterItem> filters;

	@XStreamImplicit(itemFieldName = "filter-mapping")
	private ArrayList<FilterMappingConfig> filterMappings;

	public static ServiceConfig fromConfig(String file) throws IOException{
		InputStreamReader reader =new InputStreamReader(new FileInputStream(file), "utf-8");
		try{
			XStream xstream = new XStream();
			xstream.processAnnotations(new Class[]{
					ServiceConfig.class,
					ServiceItem.class,
					ParamItem.class,
					FilterMappingConfig.class,
					FilterItem.class});
			xstream.autodetectAnnotations(true);
			return (ServiceConfig)xstream.fromXML(reader);
		}finally{
			reader.close();
		}
	}

	public ArrayList<ServiceItem> getServices() {
		return services;
	}

	public ArrayList<FilterItem> getFilters() {
		return filters;
	}

	public ArrayList<FilterMappingConfig> getFilterMappings() {
		return filterMappings;
	}
	
	public ServiceConfig(){}
}
