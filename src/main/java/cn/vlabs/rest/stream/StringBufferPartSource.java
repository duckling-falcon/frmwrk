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
package cn.vlabs.rest.stream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.httpclient.methods.multipart.PartSource;

public class StringBufferPartSource implements PartSource{
	public StringBufferPartSource(String value){
		try {
			bytes = value.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		length = bytes.length;
	}
	public InputStream createInputStream() throws IOException {
		return new ByteArrayInputStream(bytes);
	}

	public String getFileName() {
		return "request.xml";
	}

	public long getLength() {
		return length;
	}
	private byte[] bytes;
	private long length;
}
