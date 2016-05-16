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
package cn.vlabs.rest.stream;

import java.io.InputStream;

public class StreamInfo implements IResource {
	public void setLength(long length) {
		this.length = length;
	}

	/* (non-Javadoc)
	 * @see cn.vlabs.rest.stream.IResource#getLength()
	 */
	public long getLength() {
		return length;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	/* (non-Javadoc)
	 * @see cn.vlabs.rest.stream.IResource#getMimeType()
	 */
	public String getMimeType() {
		return mimeType;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	/* (non-Javadoc)
	 * @see cn.vlabs.rest.stream.IResource#getFilename()
	 */
	public String getFilename() {
		return filename;
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}
	/* (non-Javadoc)
	 * @see cn.vlabs.rest.stream.IResource#getInputStream()
	 */
	public InputStream getInputStream() {
		return inputStream;
	}

	private String mimeType;

	private String filename;

	private long length;

	private InputStream inputStream;

}
