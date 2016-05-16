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

public final class ClassUtil {
	public static boolean isSubClassOf(Class<?> paramClass, Class<?> target) {
		while(paramClass!=null){
			if (target.equals(paramClass)){
				return true;
			}
			paramClass = paramClass.getSuperclass();
		}
		return false;
	}
	public static boolean hasInertface(Class<?> paramClass, Class<?> target){
		if (paramClass.isInterface())
			return target.equals(paramClass);
		Class<?>[] interfaces = paramClass.getInterfaces();
		if (interfaces!=null){
			for (Class<?> inerfaceClass:interfaces){
				if (target.equals(inerfaceClass))
					return true;
			}
		}
		return false;
	}
	private ClassUtil(){};
}
