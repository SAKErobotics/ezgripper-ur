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

import java.awt.EventQueue;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;

import org.apache.xmlrpc.XmlRpcException;

import com.ur.urcap.api.contribution.ProgramNodeContribution;
import com.ur.urcap.api.domain.URCapAPI;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.script.ScriptWriter;
import com.ur.urcap.api.ui.annotation.Input;
import com.ur.urcap.api.ui.annotation.Label;
import com.ur.urcap.api.ui.annotation.Img;
import com.ur.urcap.api.ui.component.InputButton;
import com.ur.urcap.api.ui.component.InputEvent;
import com.ur.urcap.api.ui.component.InputTextField;
import com.ur.urcap.api.ui.component.LabelComponent;
import com.ur.urcap.api.ui.component.ImgComponent;

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
			} else if (opString.contains("goto")) {
				String[] s = opString.split("\\s+");
				getInstallation().getXmlRpcDaemonInterface().gripperMove(
						Integer.parseInt(s[1]), Integer.parseInt(s[2]));
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
			if (opString.contains("goto")) {
				String[] s = opString.split("\\s+");
				return getInstallation().getXMLRPCVariable() +
						String.format(".ezg_move(%s, %s)", s[1], s[2]);
			}
			return "# Unknown operation: " + opString;
		}
	}
	
	private static final String OPERATION = "operation";

	private final DataModel model;
	private final URCapAPI api;
	private Timer uiTimer;
	private String servoIds = "";

	public EzgripperProgramNodeContribution(URCapAPI api, DataModel model) {
		this.api = api;
		this.model = model;
	}

	@Label(id = "lblStatus")
	private LabelComponent lblStatus; 
	
	@Img(id = "liveImage")
	private ImgComponent liveImage;
	
	@Input(id = "btnGoto")
	private InputButton gotoButton;
	@Input(id = "btnClose")
	private InputButton closeButton;
	
	@Input(id = "txtGoto1")
	private InputTextField goto1Field;
	@Input(id = "txtGoto2")
	private InputTextField goto2Field;
	@Input(id = "txtClose")
	private InputTextField closeField;
	
	
	@Input(id = "btnCalibrate")
	private InputButton calibrateButton;

	public void showPic(int n) {
		String picName = "/com/sakerobotics/ezgripper/ur/impl/img/EZGripper"+n+".png";
		try{
			liveImage.setImage(ImageIO.read(getClass().getResource(picName)));
		} catch (java.io.IOException e) {
			e.printStackTrace();
		}		
	}
	
	@Input(id = "txtGoto1")
	public void txtGoto1Event(InputEvent event) throws Exception {
		if (event.getEventType() == InputEvent.EventType.ON_CHANGE) {
			showPic(Integer.parseInt(goto1Field.getText())/5);
		}
	}

	
	@Input(id = "btnGoto")
	public void onGotoClick(InputEvent event) throws Exception {
		if (event.getEventType() == InputEvent.EventType.ON_CHANGE) {
			String text = "GoTo " + goto1Field.getText() + " " + goto2Field.getText();
			model.set(OPERATION, text);
			Operation op = new Operation(text);
			op.execute();
		}
	}
	@Input(id = "btnClose")
	public void onCloseNewClick(InputEvent event) throws Exception {
		if (event.getEventType() == InputEvent.EventType.ON_CHANGE) {
			String text = "Close " + closeField.getText();
			model.set(OPERATION, text);
			Operation op = new Operation(text);
			op.execute();
			
			goto1Field.setText("0");
			goto2Field.setText(closeField.getText());
			showPic(0);
		}
	}
	
	
	@Input(id = "btnCalibrate")
	public void onCalibrateClick(InputEvent event) throws Exception {
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
		String opString = model.get(OPERATION, "goto 50 50").toLowerCase();
		if (opString.contains("goto")) {
			String[] s = opString.split("\\s+");
			goto1Field.setText(s[1]);
			goto2Field.setText(s[2]);
		} else if (opString.contains("open")) {
			goto1Field.setText(opString.replaceAll("\\D", ""));
			goto2Field.setText("100");
		} else if (opString.contains("close")) {
			goto1Field.setText("0");
			goto2Field.setText(opString.replaceAll("\\D", ""));
			closeField.setText(opString.replaceAll("\\D", ""));
		}
		showPic(Integer.parseInt(goto1Field.getText())/5);
		
		try {
			servoIds = Arrays.toString(getInstallation().getXmlRpcDaemonInterface().get_ids());
		} catch (XmlRpcException e) {
			e.printStackTrace();
		}
		
		// UI updates from non - GUI threads must use EventQueue.invokeLater ( or SwingUtilities.invokeLater )
		uiTimer = new Timer ( true ) ;
		uiTimer.schedule( new TimerTask () {
			@Override
			public void run () {
				EventQueue.invokeLater( new Runnable () {
					@Override
					public void run () {
						updateUI ();
					}
				}) ;
			}
		} , 0 , 1000) ;
	}

	private void updateUI() {
		String statusString="";
		try {
			int[] pos = getInstallation().getXmlRpcDaemonInterface().get_positions();
			int[] temps = getInstallation().getXmlRpcDaemonInterface().get_temperatures();
			statusString = "ids="+servoIds+" positions="+Arrays.toString(pos)+" temperatures="+Arrays.toString(temps);
		} catch (XmlRpcException e) {
			e.printStackTrace();
		}
		lblStatus.setText("Status: "+statusString);
	}
	
	@Override
	public void closeView() {
		uiTimer.cancel();
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
