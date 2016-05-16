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

public class FrameWorkInfo {
	public FrameWorkInfo(){
		apiType="";
		apiVersion="";
		frameVersion="";
	}

	public FrameWorkInfo(String apiType, String apiVersion, String frameVersion){
		this.apiType=apiType;
		this.apiVersion=apiVersion;
		this.frameVersion=frameVersion;
	}
	public void setApiType(String apiType) {
		this.apiType = apiType;
	}
	public String getApiType() {
		return apiType;
	}

	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}

	public String getApiVersion() {
		return apiVersion;
	}

	public void setFrameVersion(String frameversion) {
		this.frameVersion = frameversion;
	}

	public String getFrameVersion() {
		return frameVersion;
	}
	private String apiType;
	private String apiVersion;
	private String frameVersion;
}
