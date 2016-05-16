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

import cn.vlabs.rest.ServiceException;
import cn.vlabs.rest.protocal.Envelope;
import cn.vlabs.rest.protocal.MessageErrors;

public class Predefined {
	public static final Envelope AUTHORIZE_FAILED;

	public static final Envelope MISSING_HEAD;

	public static final Envelope PARSE_ERROR;

	static {
		PARSE_ERROR = createErrorEnvelope(ServiceException.ERROR_PARSE_MESSAGE,
				"Can't read request message.");

		MISSING_HEAD = createErrorEnvelope(ServiceException.ERROR_MISSING_HEAD,
				"Message head is required.");
		AUTHORIZE_FAILED = createErrorEnvelope(
				ServiceException.ERROR_ACCESS_FORBIDDEN, "Authorize failed.");
	}

	public static Envelope fromException(ServiceException e) {
		Envelope response = new Envelope();
		response.setErrors(new MessageErrors(e.getCode(), e.getMessage()));
		return response;
	}
	public static Envelope serviceNotFound(String service){
		Envelope envelope = createErrorEnvelope(
				ServiceException.ERROR_SERVICE_NOT_FOUND,
				"Service("+service+")you requested not found.");
		return envelope;
	}
	public static Envelope internalExceptionEnvelope(String message) {
		Envelope response = new Envelope();
		response.setErrors(new MessageErrors(
				ServiceException.ERROR_INTERNAL_ERROR, message));
		return response;
	}
	private static Envelope createErrorEnvelope(int code, String message) {
		Envelope envelope = new Envelope();
		envelope.setErrors(new MessageErrors(code, message));
		return envelope;
	}

	private Predefined() {
	};
}
