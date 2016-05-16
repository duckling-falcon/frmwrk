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
package cn.vlabs.rest.server.dispatcher.claasic;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import cn.vlabs.rest.AuthValidator;
import cn.vlabs.rest.FrameWorkInfo;
import cn.vlabs.rest.RestSession;
import cn.vlabs.rest.ServiceAction;
import cn.vlabs.rest.ServiceException;
import cn.vlabs.rest.ServiceWithInputStream;
import cn.vlabs.rest.TakeOver;
import cn.vlabs.rest.protocal.Envelope;
import cn.vlabs.rest.protocal.MessageHead;
import cn.vlabs.rest.protocal.ServiceRequest;
import cn.vlabs.rest.server.Capability;
import cn.vlabs.rest.server.Predefined;
import cn.vlabs.rest.server.dispatcher.BaseDispatcher;
import cn.vlabs.rest.server.dispatcher.RestSessionImpl;
import cn.vlabs.rest.stream.IResource;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;

public class ServiceDispatcher extends BaseDispatcher{
	public static class ServiceEntry {
		private String clazz;

		private String servicename;

		private boolean validAppAuth;

		public String getClazz() {
			return clazz;
		}

		public String getServicename() {
			return servicename;
		}

		public boolean isValidAppAuth() {
			return validAppAuth;
		}

		public void setClazz(String clazz) {
			this.clazz = clazz;
		}

		public void setServicename(String servciename) {
			this.servicename = servciename;
		}

		public void setValidAppAuth(boolean validAppAuth) {
			this.validAppAuth = validAppAuth;
		}
	}

	public static class ServiceConfig {
		private ArrayList<ServiceEntry> entires;

		private String type;

		private String validator;

		private String version;

		public ArrayList<ServiceEntry> getEntires() {
			return entires;
		}

		public String getType() {
			return type;
		}

		public String getValidator() {
			return validator;
		}

		public String getVersion() {
			return version;
		}

		public void setEntires(ArrayList<ServiceEntry> entries) {
			this.entires = entries;
		}

		public void setType(String type) {
			this.type = type;
		}

		public void setValidator(String validator) {
			this.validator = validator;
		}

		public void setVersion(String version) {
			this.version = version;
		}
	}

	private static Logger log = Logger.getLogger(ServiceDispatcher.class);

	private AuthValidator auth = null;

	private HashMap<String, ServiceEntry> services;

	private XStream xstream;

	public ServiceDispatcher(String configFile) throws IOException{
		services = new HashMap<String, ServiceEntry>();
		xstream = new XStream();
		xstream.alias("Envelope", Envelope.class);
		xstream.alias("Service", ServiceEntry.class);
		xstream.autodetectAnnotations(false);
		loadServices(configFile);
	}

	public void destroy() {
		
	}

	public void doService(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
		ServiceRequest servRequest = ServiceRequest.fromRequest(httpRequest);
		RestSession session = new RestSessionImpl(httpRequest.getSession());
		Envelope response;
		try {
			if (servRequest.getRequestXML()!=null){
				Object objenv = xstream.fromXML(servRequest.getRequestXML());
				if (objenv == null || !(objenv instanceof Envelope)) {
					log.error("Wrong Message Format. The inputed message is:");
					log.debug(servRequest.getRequestXML());
					response = Predefined.PARSE_ERROR;
				} else {
					Envelope request = (Envelope) objenv;
					response = doService(request, servRequest.getStream(),
							httpRequest, httpResponse, session);
				}
			}else{
				response = requestXMLNotFound(httpRequest);
			}
		} catch (XStreamException e) {
			log.error(e.getMessage());
			log.debug("The request XML is " + servRequest.getRequestXML());
			log.debug("Request IP:" + httpRequest.getRemoteAddr());
			response = Predefined.PARSE_ERROR;
		}

		if (response != null && response != Envelope.EMPTY) {
			String xml = xstream.toXML(response);
			try {
				if (response.getErrors() != null) {
					httpResponse.setStatus(Envelope.SC_HAS_ERROR);
				}
				httpResponse.setCharacterEncoding("utf-8");
				httpResponse.setContentType("text/xml");
				Writer writer = httpResponse.getWriter();
				writer.write(xml);
				writer.close();
			} catch (IOException e) {
				log.error(e);
			}
		}
	}
	
	private Envelope doService(Envelope request, IResource stream,
			HttpServletRequest httpRequest, HttpServletResponse httpResponse,
			RestSession session) {
		MessageHead head = request.getHead();
		if (head == null)
			return Predefined.MISSING_HEAD;
		ServiceEntry entry = services.get(head.getService());
		if (entry == null) {
			return Predefined.serviceNotFound(head.getService());
		}
		log.debug("Service:" + head.getService() + " is called.");
		log.debug("Configed service class:" + entry.getClazz());

		if (entry.isValidAppAuth()) {
			if (!validAuthorize(head.getAppName(), head.getAppPassword()))
				return Predefined.AUTHORIZE_FAILED;
		}
		try {
			ServiceAction action = (ServiceAction) Class.forName(entry.clazz)
					.newInstance();

			if (action instanceof ServiceWithInputStream) {
				ServiceWithInputStream srv = (ServiceWithInputStream) action;
				srv.setResponse(httpResponse);
				srv.setRequest(httpRequest);
				srv.setStream(stream);
			}
			Object body = action.doAction(session, request.getBody());
			if (body != TakeOver.NO_MESSAGE) {
				Envelope response = new Envelope();
				response.setBody(body);
				return response;
			} else
				return Envelope.EMPTY;

		} catch (InstantiationException e) {
			log.error("Create Service " + entry.servicename + " Failed",e);
			return Predefined.serviceNotFound(head.getService());
		} catch (IllegalAccessException e) {
			log.error("Create Service " + entry.servicename + " Failed",e);
			return Predefined.serviceNotFound(head.getService());
		} catch (ServiceException e) {
			return Predefined.fromException(e);
		} catch (ClassNotFoundException e) {
			log.error("Create Service " + entry.servicename + " Failed",e);
			return Predefined.serviceNotFound(head.getService());
		} catch (Throwable e) {
			while (e.getCause() != null) {
				e = e.getCause();
			}
			log.error("Access Service " + entry.servicename + " Failed.",e);
			return Predefined.internalExceptionEnvelope(e.getMessage());
		}
	}

	private void loadServices(String path) throws IOException {
		File f = new File(path);
		if (f.exists() && f.isFile() && f.canRead()) {
			FileInputStream in = new FileInputStream(f);
			XStream stream = new XStream();
			stream.alias("Service", ServiceEntry.class);
			stream.alias("ServiceConfig", ServiceConfig.class);
			try {
				ServiceConfig config = (ServiceConfig) stream.fromXML(in);

				ArrayList<ServiceEntry> entries = config.getEntires();
				if (entries != null) {
					for (ServiceEntry entry : entries) {
						services.put(entry.servicename, entry);
					}
				}
				AuthValidator valid = (AuthValidator) Class.forName(
						config.getValidator()).newInstance();
				this.auth = valid;
				RestSessionImpl.setFrameWorkInfo(new FrameWorkInfo(config
						.getType(), config.getVersion(),
						Capability.FrameVersoin));
			} catch (Throwable e) {
				log.error("Load services failed:", e);
			}
			in.close();
		}
	}

	private boolean validAuthorize(String appname, String apppassword) {
		if (auth != null)
			return auth.validAuthorize(appname, apppassword);
		else
			return false;
	}
}
