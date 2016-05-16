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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;

import cn.vlabs.rest.RestSession;
import cn.vlabs.rest.protocal.Envelope;
import cn.vlabs.rest.server.Predefined;
import cn.vlabs.rest.server.annotation.Destroy;
import cn.vlabs.rest.server.annotation.Init;
import cn.vlabs.rest.server.annotation.RestMethod;
import cn.vlabs.rest.server.dispatcher.ServiceInitException;
import cn.vlabs.rest.server.filter.Filter;
import cn.vlabs.rest.server.filter.RequestContext;
import cn.vlabs.rest.stream.IResource;

public class ServiceInvoker {
	private static Logger log = Logger.getLogger(ServiceInvoker.class);

	private Class<?> clazz;

	private Filter[] filters;

	private Map<String, MethodInvoker> methods;
	private Method initMethod;
	private Method destroyMethod;
	private Object target;

	public ServiceInvoker(String className, Filter[] filters)
			throws ServiceInitException {
		try {
			this.clazz = Class.forName(className);
			target = this.clazz.newInstance();
		} catch (ClassNotFoundException e) {
			throw new ServiceInitException("Service class not found:"
					+ e.getMessage());
		} catch (InstantiationException e) {
			throw new ServiceInitException(e.getMessage());
		} catch (IllegalAccessException e) {
			throw new ServiceInitException(e.getMessage());
		}
		initMethod = null;
		destroyMethod = null;
		methods = probeMethods(clazz);
		this.filters = filters;
	}

	public void init(ServletContext context) {
		if (initMethod!=null){
			try {
				initMethod.invoke(target, context);
			} catch (IllegalAccessException e) {
				logError(e, initMethod, "@Init");
			} catch (IllegalArgumentException e) {
				logError(e, initMethod, "@Init");
			} catch (InvocationTargetException e) {
				logError(e, initMethod, "@Init");
			}
		}
	}

	public void destroy() {
		if (destroyMethod!=null){
			try {
				destroyMethod.invoke(target);
			} catch (IllegalAccessException e) {
				logError(e, destroyMethod, "@Destroy");
			} catch (IllegalArgumentException e) {
				logError(e, destroyMethod, "@Destroy");
			} catch (InvocationTargetException e) {
				logError(e, destroyMethod, "@Destroy");
			}
		}
	}
	
	private void logError(Throwable e, Method method, String annotation){
		log.error(clazz
				+ "."
				+ method.getName()
				+ " has announce "+annotation+" annotation, but call failed.");
		log.error(e);
	}
	public Envelope invoke(RequestContext context, RestSession session,
			Object body, IResource stream) {
		String methodName = context.getMethodName();
		MethodInvoker methodInvoker = methods.get(methodName);
		if (methodInvoker != null) {
			Method method = methodInvoker.getMethod();
			Object[] params = buildParams(body, stream, session, methodInvoker);
			Envelope filterResponse = doFilter(method, params, context, session);
			if (filterResponse != null) {
				return filterResponse;
			}
			try {
				Object result = methodInvoker.getMethod()
						.invoke(target, params);
				Envelope envelope = new Envelope();
				envelope.setBody(result);
				return envelope;
			} catch (IllegalArgumentException e) {
				log.error("Calling method "
						+ buildPrototype(methodName, params));
				log.error(" is not found.");
				return Predefined.serviceNotFound(context.getFullServiceName());
			} catch (IllegalAccessException e) {
				String message = "Can't access service "
						+ context.getFullServiceName();
				log.error(message);
				log.error("This service is mapping to " + clazz + "."
						+ method.getName());
				return Predefined.internalExceptionEnvelope(message);
			} catch (InvocationTargetException e) {
				String message = "Invoke serivce "
						+ context.getFullServiceName() + " with error\n"
						+ e.getTargetException().getMessage();
				log.error(message);
				log.error("The target's class is " + target.getClass());
				log.error("The detail is ", e.getTargetException());
				return Predefined.internalExceptionEnvelope(message);
			}
		} else {
			log.error("Service " + context.getFullServiceName()
					+ "is not found.");
			log.error("The request is come from " + context.getRemoteAddr());
			return Predefined.serviceNotFound(context.getFullServiceName());
		}
	}

	private String buildPrototype(String methodName, Object[] params) {
		StringBuilder builder = new StringBuilder();
		builder.append(clazz);
		builder.append("\n");
		builder.append(methodName + "(");
		boolean first = true;
		for (Object param : params) {
			if (!first) {
				builder.append(", ");
			} else {
				first = false;
			}
			builder.append(param.getClass().getName());
		}
		builder.append(")");
		return builder.toString();
	}

	private Envelope doFilter(Method method, Object[] params,
			RequestContext context, RestSession session) {
		if (filters != null) {
			Envelope result = null;
			for (Filter filter : filters) {
				result = filter.doFilter(method, params, context, session);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	private boolean isMethodNameValid(String methodName) {
		if (methodName != null) {
			methodName = methodName.trim();
			if (Pattern.matches("\\w+[\\w\\d]*", methodName)) {
				return true;
			}
		}
		return false;
	}

	private Object[] buildParams(Object body, IResource resource,
			RestSession session, MethodInvoker methodInvoker) {
		int length = 0;
		if (resource != null) {
			length++;
		}

		if (methodInvoker.isSessionRequired()) {
			length++;
		}

		if (length == 0) {
			// No change
			if (body == null) {
				return null;
			} else {
				if (body instanceof Object[]) {
					return (Object[]) body;
				} else {
					return new Object[] { body };
				}
			}
		} else {
			int orginLength = 0;
			if (body != null) {
				if (body instanceof Object[]) {
					orginLength = ((Object[]) body).length;
					length += orginLength;
				} else {
					orginLength = 1;
					length++;
				}
			}

			Object[] value = new Object[length];
			int index = 0;
			if (methodInvoker.isSessionRequired()) {
				value[index] = session;
				index++;
			}
			if (orginLength != 0) {
				if (body instanceof Object[]) {
					System.arraycopy(body, 0, value, index, orginLength);
					index += orginLength;
				} else {
					value[index] = body;
					index++;
				}
			}
			if (resource != null) {
				value[index] = resource;
			}
			return value;
		}
	}

	private Map<String, MethodInvoker> probeMethods(Class<?> clazz) {
		HashMap<String, MethodInvoker> serviceMap = new HashMap<String, MethodInvoker>();
		Method[] methods = clazz.getMethods();
		for (Method method : methods) {
			MethodInvoker invoker = parseRestMethod(method);
			if (invoker!=null){
				serviceMap.put(invoker.getMethodName(), invoker);
			}
			if (method.getAnnotation(Init.class)!=null){
				initMethod = method;
			}
			if (method.getAnnotation(Destroy.class)!=null){
				destroyMethod = method;
			}
		}
		return serviceMap;
	}

	private MethodInvoker parseRestMethod(Method method){
		Annotation annotation = method.getAnnotation(RestMethod.class);
		if (annotation != null) {
			RestMethod methodAnnotation = (RestMethod) annotation;
			if (isMethodNameValid(methodAnnotation.value())) {
				// Check if public and instance method
				int modifiers = method.getModifiers();
				if (Modifier.isPublic(modifiers)
						&& !Modifier.isStatic(modifiers)) {
							return new MethodInvoker(methodAnnotation.value(), method);
				} else {
					log.error("Only public instance method can became a Rest Method.\n"
							+ clazz
							+ "."
							+ method.getName()
							+ " will be ignored.");
				}
			} else {
				log.error(clazz
						+ "."
						+ method.getName()
						+ " has announce @RestMethod with invalidate value (like Java variable name rule)");
			}
		}
		return null;
	}
}
