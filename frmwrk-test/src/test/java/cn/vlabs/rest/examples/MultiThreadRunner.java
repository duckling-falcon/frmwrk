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

import java.util.concurrent.Semaphore;

public class MultiThreadRunner {
	private Semaphore semaphore;

	private Thread[] threads;

	private class RunnableWrapper implements Runnable {
		private Runnable cmd;

		public RunnableWrapper(Runnable cmd) {
			this.cmd = cmd;
		}

		public void run() {
			try {
				cmd.run();
			} finally {
				semaphore.release();
			}
		}
	}
	public MultiThreadRunner(int threadCount, Class jobClass){
		this(threadCount, new DefaultRunnableFactory(jobClass));
	}
	public MultiThreadRunner(int threadCount, RunnableFactory factory) {
		threads = new Thread[threadCount];
		for (int i = 0; i < threadCount; i++) {
			RunnableWrapper wrapper = new RunnableWrapper(factory
					.createRunnable(i));
			threads[i] = new Thread(wrapper);
		}
		semaphore = new Semaphore(threadCount, true);
		try {
			semaphore.acquire(threadCount);
		} catch (InterruptedException e) {
			System.err.println("init runner failed due to "+e.getMessage());
		}
	}
	public void start(){
		for (Thread thread:threads){
			thread.start();
		}
		int finished = 0;
		while (finished<threads.length){
			try {
				semaphore.acquire();
				finished++;
			} catch (InterruptedException e) {
			}
		}
	}
}
