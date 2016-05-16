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
package cn.vlabs.rest.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.vlabs.rest.Constant;
import cn.vlabs.rest.server.dispatcher.DispatcherFactory;

public class ServiceServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor of the object.
	 */
	public ServiceServlet() {
		super();
	}

	/**
	 * Destruction of the servlet. <br>
	 */
	public void destroy() {
		if (dispatcher!=null){
			dispatcher.destroy();
			dispatcher=null;
		}
	}

	public void init() throws ServletException {
		charset = getServletContext().getInitParameter("charset");
		if (charset==null){
			charset = Constant.DEFAULT_CHARSET;
		}
		
		String prefix = getServletContext().getRealPath("/");
		String configFile = prefix + getInitParameter("config");
		
		String version = getInitParameter("version");
		try {
			dispatcher = DispatcherFactory.getDispatcher(getServletContext(), version, configFile);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding(charset);
		dispatcher.doService(request, response);
	}
	private String charset;
	private Dispatcher dispatcher;
}
