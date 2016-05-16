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

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.fileupload.FileItem;

public class FormFile implements IResource {
	public FormFile(FileItem file){
		this.file=file;
	}

	public long getLength() {
		return file.getSize();
	}

	public String getMimeType() {
		return file.getContentType();
	}


	public String getFilename() {
		return file.getName();
	}
	/**
	 * 获得返回的输入流。
	 * @return 这个输入流只能打开一次，而且必须由调用者关闭。
	 */
	public InputStream getInputStream() {
		try {
			return file.getInputStream();
		} catch (IOException e) {
			return null;
		}
	}

	private FileItem file ;
}
