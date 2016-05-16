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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.vlabs.rest.stream.IResource;

public class ServletFileOutputer {
	private static final int DEFAULT_BUFF_SIZE = 4096;

	public static void dumpFile(HttpServletRequest request,
			HttpServletResponse response, IResource resource)
			throws IOException {
		preprocess(request, response, resource);
		OutputStream out = response.getOutputStream();
		InputStream in = resource.getInputStream();
		try {
			byte[] buff = new byte[DEFAULT_BUFF_SIZE];
			int count = 0;
			while ((count = in.read(buff)) != -1) {
				out.write(buff, 0, count);
			}
		} finally {
			in.close();
			out.close();
		}
	}

	private static void preprocess(HttpServletRequest request,
			HttpServletResponse response, IResource resource) {
		String agent = request.getHeader("user-agent");
		response.setHeader("Pragma", "");
		response.setHeader("Cache-Control", "");
		response.setContentLength((int) resource.getLength());
		setFileName(resource.getFilename(), agent, response);
	}

	private static void setFileName(String fname, String agent,
			HttpServletResponse response) {
		String suffix = MimeType.getSuffix(fname);
		// response.setCharacterEncoding("UTF-8");

		// 文件的内容类型
		response.setContentType(MimeType.getContentType(suffix));
		try {
			// 浏览器端存储的文件名
			if (isFirefox(agent))
				fname = javax.mail.internet.MimeUtility.encodeText(fname,
						"UTF-8", "B");
			else if (isIE(agent)) {
				String codedFname = URLEncoder.encode(fname, "UTF-8");
				codedFname = codedFname.replaceAll("+", "%20");
				if (codedFname.length() > 150) {
					codedFname = new String(fname.getBytes("GBK"), "ISO8859-1");
				}
				fname = codedFname;
			} else
				fname = URLEncoder.encode(fname, "UTF-8");
			response.setHeader("Content-Disposition", "attachment;filename=\""
					+ fname + "\"");
		} catch (UnsupportedEncodingException e) {
		}
	}

	private static boolean isFirefox(String agent) {
		return agent != null && agent.contains("Firefox");
	}

	private static boolean isIE(String agent) {
		return agent != null && agent.contains("MSIE");
	}
}
