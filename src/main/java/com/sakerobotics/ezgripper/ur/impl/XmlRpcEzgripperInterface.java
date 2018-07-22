/*
 * Software License Agreement (BSD License)
 * 
 * Copyright (c) 2016, SAKE Robotics
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the name of the copyright holder nor the names of its
 *      contributors may be used to endorse or promote products derived from
 *      this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.sakerobotics.ezgripper.ur.impl;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;

public class XmlRpcEzgripperInterface {

	private final XmlRpcClient client;

	public XmlRpcEzgripperInterface(String host, int port) {
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		try {
			config.setServerURL(new URL("http://" + host + ":" + port + "/RPC2"));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		config.setConnectionTimeout(1000); //1s
		client = new XmlRpcClient();
		client.setConfig(config);
	}

	public boolean isReachable() {
		try {
			client.execute("ezg_ping", Collections.emptyList());
			return true;
		} catch (XmlRpcException e) {
			System.err.println("XmlRpcEzgripperInterface.isReachable()=false : "+e);
			return false;
		}
	}

	public void gripperCalibrate() throws XmlRpcException {
		client.execute("ezg_calibrate", Collections.emptyList());
	}

	public void gripperOpen() throws XmlRpcException {
		client.execute("ezg_open", Collections.emptyList());
	}

	public void gripperClose() throws XmlRpcException {
		client.execute("ezg_close", Collections.emptyList());
	}

	public void gripperRelease() throws XmlRpcException {
		client.execute("ezg_release", Collections.emptyList());
	}
	
	public void gripperPing() throws XmlRpcException {
		client.execute("ezg_ping", Collections.emptyList());
	}
	
	public void gripperMove(int position,int effort) throws XmlRpcException {
		client.execute("ezg_move", Arrays.asList(position, effort));
	}

	public String exec(String cmd) throws XmlRpcException {
		Object result = client.execute("ezg_exec", Arrays.asList(cmd));
		return processString(result);
	}

	public String get_last_message() throws XmlRpcException {
		Object result = client.execute("ezg_get_last_message", Collections.emptyList());
		return processString(result);
	}

	public int get_position() throws XmlRpcException {
		Object result = client.execute("ezg_get_position", Collections.emptyList());
		return processInt(result);
	}

	public int[] get_positions() throws XmlRpcException {
		Object result = client.execute("ezg_get_positions", Collections.emptyList());
		return processIntArray(result);
	}
	
	public int[] get_temperatures() throws XmlRpcException {
		Object result = client.execute("ezg_get_temperatures", Collections.emptyList());
		return processIntArray(result);
	}
	
	public int[] get_ids() throws XmlRpcException {
		Object result = client.execute("ezg_get_ids", Collections.emptyList());
		return processIntArray(result);
	}
	
	public String get_devname() throws XmlRpcException {
		Object result = client.execute("ezg_get_devname", Collections.emptyList());
		return processString(result);
	}

	public String init_connection() throws XmlRpcException {
		Object result = client.execute("ezg_init_connection", Collections.emptyList());
		return processString(result);
	}

	private int[] processIntArray(Object response) {
		if (response instanceof Object[]) {
			Object[] objs = (Object[]) response;
			int[] intres = new int[objs.length];
			for(int i=0; i<objs.length; i++) {
				if (objs[i] instanceof Integer) {
					intres[i] = (Integer)objs[i];
				}
			}
			return intres;
		} else {
			return new int[0];
		}
	}
	
	private String processString(Object response) {
		if (response instanceof String) {
			return (String) response;
		} else {
			return "";
		}
	}
	
	private int processInt(Object response) {
		if (response instanceof Integer) {
			return (Integer) response;
		} else {
			return -1;
		}
	}
	
}
