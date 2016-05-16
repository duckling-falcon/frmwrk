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
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import cn.vlabs.rest.RestSession;
import cn.vlabs.rest.protocal.Envelope;
import cn.vlabs.rest.protocal.ServiceRequest;
import cn.vlabs.rest.server.Predefined;
import cn.vlabs.rest.server.config.FilterItem;
import cn.vlabs.rest.server.config.FilterMappingConfig;
import cn.vlabs.rest.server.config.ServiceConfig;
import cn.vlabs.rest.server.config.ServiceItem;
import cn.vlabs.rest.server.dispatcher.BaseDispatcher;
import cn.vlabs.rest.server.dispatcher.RestContextImpl;
import cn.vlabs.rest.server.dispatcher.RestSessionImpl;
import cn.vlabs.rest.server.dispatcher.ServiceInitException;
import cn.vlabs.rest.server.filter.Filter;
import cn.vlabs.rest.server.filter.RequestContext;
import cn.vlabs.rest.stream.IResource;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;

public class AnnotationBasedDispatcher extends BaseDispatcher {
	private static Logger log = Logger
			.getLogger(AnnotationBasedDispatcher.class);

	private HashMap<String, Filter> filters;

	private HashMap<String, ServiceInvoker> services;

	private XStream xstream;

	public AnnotationBasedDispatcher(ServletContext context, String configFile)
			throws IOException {
		ServiceConfig config = ServiceConfig.fromConfig(configFile);

		ArrayList<FilterMap> mappings = loadMapping(config);
		loadFilters(context, config);
		loadServices(config, mappings);
		xstream = new XStream();
		xstream.alias("Envelope", Envelope.class);
		xstream.autodetectAnnotations(false);
		
		init(context);
	}

	private Envelope callService(ServiceRequest servRequest,
			RequestContext context, RestSession session) {
		Envelope request = parseEnvelope(servRequest.getRequestXML());
		if (request == null)
			return Predefined.PARSE_ERROR;

		if (request.getHead() == null) {
			return Predefined.MISSING_HEAD;
		}
		String fullServiceName = request.getHead().getService();
		if (fullServiceName == null) {
			return Predefined.serviceNotFound(request.getHead().getService());
		}
		context.setService(fullServiceName);
		if (context.getMethodName() == null) {
			log.error("Wrong request format for service name "
					+ fullServiceName);
			return Predefined.PARSE_ERROR;
		}

		ServiceInvoker invoker = services.get(context.getServiceName());
		if (invoker != null) {
			try {
				return invoker.invoke(context, session, request.getBody(),
						servRequest.getStream());
			} catch (RuntimeException e) {
				log.error(e.getMessage());
				log.error("Caused by ", e);
				return Predefined.internalExceptionEnvelope(e.getMessage());
			}
		} else {
			log.error("Service " + request.getHead().getService()
					+ " is not found.");
			return Predefined.serviceNotFound(fullServiceName);
		}
	}
	public void init(ServletContext context){
		for (ServiceInvoker invoker:services.values()){
			invoker.init(context);
		}
	}
	
	public void destroy() {
		if (services!=null){
			for (ServiceInvoker invoker:services.values()){
				invoker.destroy();
			}
			services = null;
		}
		HashMap<String, Filter> localFilters = filters;
		filters = null;
		if (localFilters!=null){
			for (Filter filter : localFilters.values()) {
				filter.destroy();
			}
		}
	}

	public void doService(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) throws IOException {
		ServiceRequest servRequest = ServiceRequest.fromRequest(httpRequest);
		Envelope response;
		if (servRequest.getRequestXML() != null) {
			RestSession session = new RestSessionImpl(httpRequest.getSession());
			HttpRequestContext context = new HttpRequestContext(httpRequest);
			response = callService(servRequest, context, session);
		} else {
			response = requestXMLNotFound(httpRequest);
		}
		if (response != null && response != Envelope.EMPTY) {
			if (!(response.getBody() instanceof IResource)) {
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
			} else {
				ServletFileOutputer.dumpFile(httpRequest, httpResponse,
						(IResource) response.getBody());
			}
		}
	}

	private HashMap<String, Filter> getFilters() {
		if (filters == null) {
			filters = new HashMap<String, Filter>();
		}
		return filters;
	}

	private void loadFilters(ServletContext context, ServiceConfig config) {
		ArrayList<FilterItem> configedFilters = config.getFilters();
		if (configedFilters != null) {
			for (FilterItem item : configedFilters) {
				try {
					Class<?> filterClass = Class.forName(item.getClazz());
					if (ClassUtil.hasInertface(filterClass, Filter.class)) {
						Filter filter = (Filter) filterClass.newInstance();
						filter.init(new RestContextImpl(context, item
								.getInitParams()));
						getFilters().put(item.getName(), filter);
					} else {
						log.error("Configed filter name=" + item.getName()
								+ "\nclass=" + item.getClazz()
								+ " is not subclass of " + Filter.class);
					}
				} catch (ClassNotFoundException e) {
					log.error("Configed filter name=" + item.getName()
							+ "\n class=" + item.getClazz() + " is not found.");
				} catch (InstantiationException e) {
					log.error("Could not created filter instance.\nwhich name="
							+ item.getName() + "\n class=" + item.getClazz());
				} catch (IllegalAccessException e) {
					log.error("Could not created filter instance.\nwhich name="
							+ item.getName() + "\n class=" + item.getClazz());
					log.error("for " + e.getMessage());
				}
			}
		}
	}

	private ArrayList<FilterMap> loadMapping(ServiceConfig config) {
		ArrayList<FilterMap> filterMap = new ArrayList<FilterMap>();
		ArrayList<FilterMappingConfig> mappings = config.getFilterMappings();
		if (mappings != null) {
			for (FilterMappingConfig map : mappings) {
				filterMap.add(new FilterMap(map.getServices(), map
						.getFilterName()));
			}
		}
		return filterMap;
	}

	private void loadServices(ServiceConfig config,
			ArrayList<FilterMap> mappings) {
		services = new HashMap<String, ServiceInvoker>();
		ArrayList<ServiceItem> serviceItems = config.getServices();
		if (serviceItems != null) {
			for (ServiceItem item : serviceItems) {
				try {
					if (item.getName() != null) {
						ServiceInvoker invoker = new ServiceInvoker(item
								.getClazz(), lookForService(mappings, item
								.getName()));
						services.put(item.getName(), invoker);
					} else {
						log
								.error("Configed service must have a name. please check your config file.");
						log.error("The item's configed class is "
								+ item.getClazz());
					}
				} catch (ServiceInitException e) {
					log.error("Could not create service instance caused by "
							+ e.getMessage());
				}
			}
		}
	}

	private Filter[] lookForService(ArrayList<FilterMap> mappings,
			String service) {
		ArrayList<Filter> selected = new ArrayList<Filter>();
		if (filters != null) {
			for (FilterMap map : mappings) {
				if (map.match(service)) {
					Filter filter = filters.get(map.getFilterName());
					if (filter != null) {
						selected.add(filter);
					} else {
						log.error("Can't find filter mapping.\n"
								+ map.toString());
					}
				}
			}
		}
		return selected.toArray(new Filter[selected.size()]);
	}

	private Envelope parseEnvelope(String xml) {
		Object objenv = xstream.fromXML(xml);
		try {
			if (objenv instanceof Envelope) {
				return (Envelope) objenv;
			} else {
				log.error("Wrong Message Format.");
				log.info("The inputed message is:\n" + xml);
			}
		} catch (XStreamException e) {
			log.error(e.getMessage());
			log.info("The request XML is " + xml);
		}
		return null;
	}
}
