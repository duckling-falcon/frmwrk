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

import java.lang.reflect.Method;

import cn.vlabs.rest.RestSession;

public class MethodInvoker {
	private Method method;
	private boolean sessionRequired;
	private String methodName;

	public MethodInvoker(String methodName, Method method) {
		this.method = method;
		Class<?>[] paramTypes = method.getParameterTypes();
		if (paramTypes != null && paramTypes.length > 0) {
			sessionRequired = ClassUtil.hasInertface(paramTypes[0],
					RestSession.class);
		} else {
			sessionRequired = false;
		}
		this.methodName = methodName;
	}

	public Method getMethod() {
		return method;
	}

	public boolean isSessionRequired() {
		return sessionRequired;
	}

	public String getMethodName() {
		return this.methodName;
	}
}
