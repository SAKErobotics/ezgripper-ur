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

import com.ur.urcap.api.contribution.DaemonContribution;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.script.ScriptWriter;
import com.ur.urcap.api.ui.annotation.Input;
import com.ur.urcap.api.ui.annotation.Label;
import com.ur.urcap.api.ui.component.InputButton;
import com.ur.urcap.api.ui.component.InputEvent;
import com.ur.urcap.api.ui.component.InputTextField;
import com.ur.urcap.api.ui.component.LabelComponent;
import com.ur.urcap.api.contribution.InstallationNodeContribution;

import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class EzgripperInstallationNodeContribution implements InstallationNodeContribution {
	private static final String XMLRPC_VARIABLE = "ezgripper_daemon";
	private static final String ENABLED_KEY = "enabled";

	private DataModel model;
	private final EzgripperDaemonService daemonService;
	private XmlRpcEzgripperInterface xmlRpcDaemonInterface;
	private Timer uiTimer;

	public EzgripperInstallationNodeContribution(EzgripperDaemonService daemonService, DataModel model) {
		this.daemonService = daemonService;
		this.model = model;
		xmlRpcDaemonInterface = new XmlRpcEzgripperInterface("127.0.0.1", 10017);
		applyDesiredDaemonStatus();
	}

	@Input(id = "btnEnableDaemon")
	private InputButton enableDaemonButton;
	@Input(id = "btnDisableDaemon")
	private InputButton disableDaemonButton;
	@Label(id = "lblDaemonStatus")
	private LabelComponent daemonStatusLabel;

	@Input(id = "btnCalibrate")
	private InputButton calibrateButton;
	@Input(id = "btnOpen")
	private InputButton openButton;
	@Input(id = "btnClose")
	private InputButton closeButton;
	@Input(id = "btnRelease")
	private InputButton releaseButton;
	
	@Input(id = "txtCommand")
	private InputTextField commandField;
	@Input(id = "txtOutput")
	private InputTextField outputField;
	@Input(id = "btnExec")
	private InputButton execButton;
	
	
	@Input(id = "btnEnableDaemon")
	public void onStartClick(InputEvent event) {
		if (event.getEventType() == InputEvent.EventType.ON_CHANGE) {
			setDaemonEnabled(true);
			applyDesiredDaemonStatus();
		}
	}

	@Input(id = "btnDisableDaemon")
	public void onStopClick(InputEvent event) {
		if (event.getEventType() == InputEvent.EventType.ON_CHANGE) {
			setDaemonEnabled(false);
			applyDesiredDaemonStatus();
		}
	}
	
	@Input(id = "btnCalibrate")
	public void onCalibrateClick(InputEvent event) {
		if (event.getEventType() == InputEvent.EventType.ON_CHANGE) {
			System.out.println("CALIBRATE");
			try {
				xmlRpcDaemonInterface.gripperCalibrate();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	@Input(id = "btnOpen")
	public void onOpenClick(InputEvent event) {
		if (event.getEventType() == InputEvent.EventType.ON_CHANGE) {
			System.out.println("OPEN");
			try {
				xmlRpcDaemonInterface.gripperOpen();
				
				int pos = xmlRpcDaemonInterface.get_position();
				System.out.println("GET_POS="+pos);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	@Input(id = "btnClose")
	public void onCloseClick(InputEvent event) {
		if (event.getEventType() == InputEvent.EventType.ON_CHANGE) {
			System.out.println("CLOSE");
			try {
				xmlRpcDaemonInterface.gripperClose();
				
				int pos = xmlRpcDaemonInterface.get_position();
				System.out.println("GET_POS="+pos);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	@Input(id = "btnRelease")
	public void onReleaseClick(InputEvent event) {
		if (event.getEventType() == InputEvent.EventType.ON_CHANGE) {
			System.out.println("RELEASE");
			try {
				xmlRpcDaemonInterface.gripperRelease();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Input(id = "btnExec")
	public void onExecClick(InputEvent event) {
		if (event.getEventType() == InputEvent.EventType.ON_CHANGE) {
			System.out.println("EXEC");
			try {
				String output = xmlRpcDaemonInterface.exec(commandField.getText());
				outputField.setText(output);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	
	@Override
	public void openView() {
		try {
			outputField.setText(xmlRpcDaemonInterface.get_last_message());
		} catch (Exception e) {}
		
		//UI updates from non-GUI threads must use EventQueue.invokeLater (or SwingUtilities.invokeLater)
		uiTimer = new Timer(true);
		uiTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						updateUI();
					}
				});
			}
		}, 0, 1000);
	}

	private void updateUI() {
		DaemonContribution.State state = getDaemonState();

		if (state == DaemonContribution.State.RUNNING) {
			enableDaemonButton.setEnabled(false);
			disableDaemonButton.setEnabled(true);
		} else {
			enableDaemonButton.setEnabled(true);
			disableDaemonButton.setEnabled(false);
		}

		String text = "";
		switch (state) {
		case RUNNING:
			text = "Daemon runs";
			break;
		case STOPPED:
			text = "Daemon stopped";
			break;
		case ERROR:
			text = "Daemon failed";
			break;
		}
		daemonStatusLabel.setText(text);
	}

	@Override
	public void closeView() {
		uiTimer.cancel();
	}

	public boolean isDefined() {
		return getDaemonState() == DaemonContribution.State.RUNNING;
	}

	@Override
	public void generateScript(ScriptWriter writer) {
		writer.globalVariable(XMLRPC_VARIABLE, "rpc_factory(\"xmlrpc\", \"http://127.0.0.1:10017/RPC2\")");
	}

	private void applyDesiredDaemonStatus() {
		if (isDaemonEnabled()) {
			try {
				awaitDaemonRunning(5000);
			} catch(Exception e){
				System.err.println("Could not start daemon");
			}
		} else {
			daemonService.getDaemon().stop();
		}
	}

	private void awaitDaemonRunning(long timeOutMilliSeconds) throws InterruptedException {
		daemonService.getDaemon().start();
		long endTime = System.nanoTime() + timeOutMilliSeconds * 1000L * 1000L;
		while(System.nanoTime() < endTime && (daemonService.getDaemon().getState() != DaemonContribution.State.RUNNING || !xmlRpcDaemonInterface.isReachable())) {
			Thread.sleep(100);
		}
	}

	private DaemonContribution.State getDaemonState(){
		return daemonService.getDaemon().getState();
	}

	private Boolean isDaemonEnabled() {
		return model.get(ENABLED_KEY, true); //This daemon is enabled by default
	}

	private void setDaemonEnabled(Boolean enable) {
		model.set(ENABLED_KEY, enable);
	}

	public String getXMLRPCVariable(){
		return XMLRPC_VARIABLE;
	}

	public XmlRpcEzgripperInterface getXmlRpcDaemonInterface() {return xmlRpcDaemonInterface; }
}
