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
package cn.vlabs.rest.examples;


import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cn.vlabs.rest.ServiceException;

public class UploadStubTest {

	@Before
	public void setUp() throws Exception {
		stub = new UploadStub(TestHelper.getContext());
	}

	@After
	public void tearDown() throws Exception {
	}
	@Test
	public void testUpload() throws ServiceException, IOException {
		String result = stub.upload("注意", "target/test-classes/upload1.txt");
		assertEquals("注意不是所有的付出都有回报！", result);
	}
	private UploadStub stub;
}
