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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import cn.vlabs.rest.server.annotation.RestMethod;
import cn.vlabs.rest.stream.IResource;
import cn.vlabs.rest.stream.StreamInfo;

public class FileExample {
	@RestMethod("readFile")
	public String readFile(String request, IResource resource) {
		InputStream in = resource.getInputStream();
		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(in, "utf-8"));
			try {
				String line = reader.readLine();
				reader.close();
				return request + line;
			} catch (IOException e) {
				System.err.println("Error while reading file");
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace(System.err);
		}
		return "";
	}
	@RestMethod("writeFile")
	public IResource writeFile() throws FileNotFoundException{
	    // String fn = "C:\\1.txt";
	    String fn = "/tmp/1.txt";

		File f = new File(fn);
		StreamInfo stream = new StreamInfo();
		stream.setFilename(fn);
		stream.setLength(f.length());
		stream.setMimeType("plain/text");
		stream.setInputStream(new FileInputStream(f));
		return stream;
	}
}
