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
package cn.vlabs.rest.protocal;

public class Envelope {
	public void setHead(MessageHead head) {
		this.head = head;
	}

	public MessageHead getHead() {
		return head;
	}

	public void setErrors(MessageErrors errors) {
		this.errors = errors;
	}

	public MessageErrors getErrors() {
		return errors;
	}

	public void setBody(Object body) {
		this.body = body;
	}

	public Object getBody() {
		return body;
	}

	private MessageHead head;

	private MessageErrors errors;

	private Object body;
	public static final int SC_HAS_ERROR=203;
	public static final Envelope EMPTY= new Envelope();
}
