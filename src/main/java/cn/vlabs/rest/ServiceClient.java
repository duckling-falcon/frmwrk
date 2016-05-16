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
package cn.vlabs.rest;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.protocol.Protocol;

import cn.vlabs.rest.protocal.EncodablePostMethod;
import cn.vlabs.rest.protocal.Envelope;
import cn.vlabs.rest.protocal.MessageHead;
import cn.vlabs.rest.stream.IResource;
import cn.vlabs.rest.stream.InputStreamPartSource;
import cn.vlabs.rest.stream.StreamInfo;
import cn.vlabs.rest.stream.StringBufferPartSource;

import com.thoughtworks.xstream.XStream;

public class ServiceClient {
	private static interface IReader {
		public void read(PostMethod method) throws IOException,
				ServiceException;

		public void readAsObject(PostMethod method) throws IOException,
				ServiceException;
	}
	public class ReadAsObject implements IReader {
		private Object result;

		public Object getResult() {
			return result;
		}

		public void read(PostMethod method) throws IOException,
				ServiceException {
			readAsObject(method);
		}

		public void readAsObject(PostMethod method) throws ServiceException,
				IOException {
			Reader reader = getReader(method);
			try {
				Object obj = fromXML(reader);
				if (obj == null || !(obj instanceof Envelope)) {
					throw new ProtocalException(
							ServiceException.ERROR_PARSE_MESSAGE,
							"Unable to parse response message.");
				} else {
					Envelope env = (Envelope) obj;
					if (env.getErrors() != null) {
						throw env.getErrors().toException();
					}
					result = env.getBody();
				}
			} finally {
				reader.close();
			}
		}

		private Reader getReader(PostMethod method) throws ProtocalException {
			try {
				return new InputStreamReader(new BufferedInputStream(method
						.getResponseBodyAsStream()), method.getResponseCharSet());
			} catch (UnsupportedEncodingException e) {
				throw new ProtocalException(
						ServiceException.ERROR_PARSE_MESSAGE,
						"Encode specified by server " + context.getCharset()
								+ " is not supportted.");
			} catch (IOException e) {
				throw new ProtocalException(
						ServiceException.ERROR_PARSE_MESSAGE,
						"Reading response message from server failed caused by \n"
								+ e.getMessage());
			}
		}
	}

	public class ReadAsStream extends ReadAsObject {
		private IFileSaver saver;

		private StreamInfo stream;

		public ReadAsStream(IFileSaver saver) {
			this.saver = saver;
		}

		public IResource getStream() {
			return stream;
		}

		public void read(PostMethod method) throws IOException,
				ServiceException {
			BufferedInputStream bis = new BufferedInputStream(method
					.getResponseBodyAsStream());
			try {
				saver.save(readFileName(method), bis);
			} finally {
				bis.close();
			}
		}

		private String readFileName(PostMethod method) {
			Header destFile = method.getResponseHeader("Content-Disposition");
			String fname = null;
			if (destFile != null)
				fname = destFile.getValue().replace("attachment;filename=", "")
						.replaceAll("\"", "");
			return fname;
		}
	}
	/**
	 * XStream对于所有的ServiceClient的对象都可以共用一个，因此设为静态变量。
	 */
	private static XStream xstream;
	
	static {
		xstream = new XStream();
		xstream.alias("Envelope", Envelope.class);
		xstream.autodetectAnnotations(false);
		Protocol myhttps = new Protocol("https", new MySecureProtocolSocketFactory(), 443);
		Protocol.registerProtocol("https", myhttps);
	}
	/**
	 * 这里保存着同一个服务的共同设置，因此需要在多个实例之间共享
	 */
	private ServiceContext context;
	private HttpClient client;
	public ServiceClient(ServiceContext context) {
		this.context = context;
		this.client = context.getClient();
	}

	public Object sendService(String servcie, Object message)
			throws ServiceException {
		PostMethod method = createMethod(servcie, message);
		ReadAsObject reader = new ReadAsObject();
		sendService(servcie, method, reader);
		return reader.getResult();
	}

	public void sendService(String service, Object message, IFileSaver saver)
			throws ServiceException {
		PostMethod method = createMethod(service, message);
		ReadAsStream reader = new ReadAsStream(saver);
		sendService(service, method, reader);
	}

	public Object sendService(String service, Object message, IResource stream)
			throws ServiceException {
		PostMethod method = createMultiPart(service, message, stream);
		ReadAsObject reader = new ReadAsObject();
		sendService(service, method, reader);
		return reader.getResult();
	}

	public void sendService(String service, Object message, IResource stream,
			IFileSaver saver) throws ServiceException {
		PostMethod method = createMethod(service, message);
		ReadAsStream reader = new ReadAsStream(saver);
		sendService(service, method, reader);
	}

	private MessageHead buildHead(String servcie, boolean hasStream) {
		MessageHead head = new MessageHead();
		head.setService(servcie);
		return head;
	}

	private PostMethod createMethod(String service, Object message) {
		Envelope env = createRequest(service, message, false);
		EncodablePostMethod method = new EncodablePostMethod(context
				.getServerURL(), context.getCharset());
		method.addParameter("RequestXML", toXML(env));
		method.setDoAuthentication(true);
		return method;
	}

	private PostMethod createMultiPart(String service, Object message,
			IResource stream) {
		Envelope env = createRequest(service, message, true);
		EncodablePostMethod method = new EncodablePostMethod(context
				.getServerURL(), context.getCharset());
		FilePart xml = new FilePart("RequestXML", new StringBufferPartSource(
				toXML(env)));
		FilePart file = new FilePart("file", new InputStreamPartSource(stream));
		file.setCharSet(context.getCharset());
		MultipartRequestEntity requestEntity = new MultipartRequestEntity(
				new Part[] { xml, file }, method.getParams());
		method.setRequestEntity(requestEntity);
		return method;
	}

	private Envelope createRequest(String service, Object message,
			boolean hasStream) {
		Envelope env = new Envelope();
		env.setHead(buildHead(service, hasStream));
		env.setBody(message);
		return env;
	}

	private Object fromXML(Reader reader){
		return xstream.fromXML(reader);
	}
	private void sendService(String service, PostMethod method, IReader reader)
			throws ServiceException {
		try {
			int statusCode = client.executeMethod(method);
			if (statusCode == HttpStatus.SC_OK) {
				reader.read(method);
			} else if (statusCode == Envelope.SC_HAS_ERROR) {
				reader.readAsObject(method);
			} else {
				throw new ConnectException(
						ServiceException.ERROR_HTTP_CONNECT_FAIL,
						method.getStatusText()+" " + HttpStatus.getStatusText(statusCode),
						context.getServerURL());
			}
		} catch (HttpException e) {
			throw new ConnectException(
					ServiceException.ERROR_HTTP_CONNECT_FAIL, e, context
							.getServerURL());
		} catch (IOException e) {
			throw new ConnectException(
					ServiceException.ERROR_HTTP_CONNECT_FAIL, e,
					context.getServerURL());
		} finally {
			method.releaseConnection();
			if (!context.isKeepAlive()){
				context.close();
			}
		}
	}
	private String toXML(Object obj){
		return xstream.toXML(obj);
	}
}
