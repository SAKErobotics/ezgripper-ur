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

import com.ur.urcap.api.contribution.ProgramNodeContribution;
import com.ur.urcap.api.domain.URCapAPI;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.script.ScriptWriter;
import com.ur.urcap.api.ui.annotation.Input;
import com.ur.urcap.api.ui.component.InputButton;
import com.ur.urcap.api.ui.component.InputEvent;

public class EzgripperProgramNodeContribution implements ProgramNodeContribution {
	
	class Operation {
		private final String opString;

		private int extractNumber() {
			return Integer.parseInt(opString.replaceAll("\\D", ""));
		}
		public Operation(String text) {
			opString = text.toLowerCase();
		}
		public void execute() throws XmlRpcException {
			if (opString.equals("calibrate")) {
				getInstallation().getXmlRpcDaemonInterface().gripperCalibrate();
			} else if (opString.equals("release")) {
				getInstallation().getXmlRpcDaemonInterface().gripperRelease();
			} else if (opString.contains("open")) {
				getInstallation().getXmlRpcDaemonInterface().gripperMove(extractNumber(), 100);
			} else if (opString.contains("close")) {
				getInstallation().getXmlRpcDaemonInterface().gripperMove(0, extractNumber());
			}
		}
		public String getScriptLine() {
			if (opString.equals("calibrate")) {
				return getInstallation().getXMLRPCVariable() + ".ezg_calibrate()";
			}
			if (opString.equals("release")) {
				return getInstallation().getXMLRPCVariable() + ".ezg_release()";
			}
			if (opString.contains("open")) {
				return getInstallation().getXMLRPCVariable() +
						String.format(".ezg_move(%d, 100)", extractNumber());
			}
			if (opString.contains("close")) {
				return getInstallation().getXMLRPCVariable() +
						String.format(".ezg_move(0, %d)", extractNumber());
			}
			return "# Unknown operation: " + opString;
		}
	}
	
	private static final String OPERATION = "operation";

	private final DataModel model;
	private final URCapAPI api;

	public EzgripperProgramNodeContribution(URCapAPI api, DataModel model) {
		this.api = api;
		this.model = model;
	}

	@Input(id = "btnCalibrate")
	private InputButton calibrateButton;
	@Input(id = "btnRelease")
	private InputButton releaseButton;
	@Input(id = "btnOpen10")
	private InputButton open10Button;
	@Input(id = "btnOpen20")
	private InputButton open20Button;
	@Input(id = "btnOpen30")
	private InputButton open30Button;
	@Input(id = "btnOpen40")
	private InputButton open40Button;
	@Input(id = "btnOpen50")
	private InputButton open50Button;
	@Input(id = "btnOpen60")
	private InputButton open60Button;
	@Input(id = "btnOpen70")
	private InputButton open70Button;
	@Input(id = "btnOpen80")
	private InputButton open80Button;
	@Input(id = "btnOpen90")
	private InputButton open90Button;
	@Input(id = "btnOpen100")
	private InputButton open100Button;
	@Input(id = "btnClose10")
	private InputButton close10Button;
	@Input(id = "btnClose50")
	private InputButton close50Button;
	@Input(id = "btnClose100")
	private InputButton close100Button;

	@Input(id = "btnCalibrate")
	public void onCalibrateClick(InputEvent event) throws Exception {
		newOperation(event);
	}
	@Input(id = "btnRelease")
	public void onReleaseClick(InputEvent event) throws Exception {
		newOperation(event);
	}
	@Input(id = "btnOpen10")
	public void onOpen10Click(InputEvent event) throws Exception {
		newOperation(event);
	}
	@Input(id = "btnOpen20")
	public void onOpen20Click(InputEvent event) throws Exception {
		newOperation(event);
	}
	@Input(id = "btnOpen30")
	public void onOpen30Click(InputEvent event) throws Exception {
		newOperation(event);
	}
	@Input(id = "btnOpen40")
	public void onOpen40Click(InputEvent event) throws Exception {
		newOperation(event);
	}
	@Input(id = "btnOpen50")
	public void onOpen50Click(InputEvent event) throws Exception {
		newOperation(event);
	}
	@Input(id = "btnOpen60")
	public void onOpen60Click(InputEvent event) throws Exception {
		newOperation(event);
	}
	@Input(id = "btnOpen70")
	public void onOpen70Click(InputEvent event) throws Exception {
		newOperation(event);
	}
	@Input(id = "btnOpen80")
	public void onOpen80Click(InputEvent event) throws Exception {
		newOperation(event);
	}
	@Input(id = "btnOpen90")
	public void onOpen90Click(InputEvent event) throws Exception {
		newOperation(event);
	}
	@Input(id = "btnOpen100")
	public void onOpen100Click(InputEvent event) throws Exception {
		newOperation(event);
	}
	@Input(id = "btnClose10")
	public void onClose10Click(InputEvent event) throws Exception {
		newOperation(event);
	}
	@Input(id = "btnClose50")
	public void onClose50Click(InputEvent event) throws Exception {
		newOperation(event);
	}
	@Input(id = "btnClose100")
	public void onCloseClick(InputEvent event) throws Exception {
		newOperation(event);
	}
	
	private void newOperation(InputEvent event) throws Exception {
		if (event.getEventType() == InputEvent.EventType.ON_CHANGE) {
			InputButton button = (InputButton) event.getComponent();
			String text = button.getText();
			model.set(OPERATION, text);
			Operation op = new Operation(text);
			op.execute();
		}
	}
	
	@Override
	public void openView() {
	}

	@Override
	public void closeView() {
	}

	@Override
	public String getTitle() {
		return "EZGripper: " + model.get(OPERATION, "");
	}

	@Override
	public boolean isDefined() {
		return !model.get(OPERATION, "").isEmpty();
	}

	@Override
	public void generateScript(ScriptWriter writer) {
		Operation op = new Operation(model.get(OPERATION, ""));
		writer.appendLine(op.getScriptLine());
	}

	private EzgripperInstallationNodeContribution getInstallation(){
		return api.getInstallationNode(EzgripperInstallationNodeContribution.class);
	}

}
