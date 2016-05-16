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

import static org.junit.Assert.assertNotNull;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.ConversionException;

public class TestXStream {
	private XStream stream =new XStream();
	private String xml;
	private int count=1000;
	public TestXStream(){
		stream = new XStream();
		stream.processAnnotations(Person.class);
		Person p = new Person();
		p.name="Xiejj";
		p.age=100;
		xml = stream.toXML(p);
	}
	
	public void run(){
		for (int i=0;i<10;i++){
			MultiThreadRunner runner = new MultiThreadRunner(count, new RunnableFactory(){
				public Runnable createRunnable(int index) {
					return new Reader(index);
				}
			});
			runner.start();
		}
		System.out.println("All finished.");		
	}
	private class Reader implements Runnable{
		private int seq;
		public Reader(int seq){
			this.seq=seq;
		}
		public void run() {
			for (int i=0;i<10;i++){
				try {
					try {
						Thread.sleep((int)(Math.random()*3));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					Object obj = stream.fromXML(xml);
					assertNotNull("从Stream中返回的Object不应该为空", obj);
				}catch (ConversionException e){
					e.getCause().printStackTrace();
					System.err.println(e);
					System.err.println(xml);
				}catch(Throwable e){
					System.err.println(e.getClass());
					System.err.println(e.getMessage());
					System.err.println("Reader "+seq+" failed at "+(i+1)+"'s fromXML calls");
				}
			}
		}
	}
	
	private static class Person{
		public String name;
		public int age;
		public String toString(){
			return "MyName is "+name+", age is "+age;
		}
	}
	public static void main(String[] args){
		TestXStream tester = new TestXStream();
		tester.run();
	}
}
