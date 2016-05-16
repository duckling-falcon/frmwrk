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

import java.util.ArrayList;
import java.util.regex.Pattern;

public class FilterMap {
	private ArrayList<Pattern> patterns;
	private ArrayList<String> regexs;
	private String filterName;
	public FilterMap(ArrayList<String> regexs, String filterName){
		this.regexs=regexs;
		this.filterName=filterName;
		if (regexs!=null){
			patterns = new ArrayList<Pattern>();
			for (String regex:regexs){
				if (regex!=null){
					regex = regex.trim();
					if (regex.length()!=0){
						regex = regex.replaceAll("\\*", ".*");
						patterns.add(Pattern.compile(regex));
					}
				}
			}
		}
	}
	
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append("filter:"+filterName+"\n");
		if (regexs!=null){
			for (String regex:regexs){
				builder.append("service:"+regex+"\n");
			}
		}
		return builder.toString();
	}
	public String getFilterName(){
		return this.filterName;
	}
	
	public boolean match(String serviceName){
		if (serviceName!=null && patterns!=null){
			for (Pattern pattern:patterns){
				if (pattern.matcher(serviceName).matches()){
					return true;
				}
			}
		}
		return false;
	}
}
