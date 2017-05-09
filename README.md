# EZGripper extension for Universal Robots

Download the URCap file from https://github.com/SAKErobotics/ezgripper-ur/releases

Put ezgripper-ur-X.Y.Z.urcap zip file onto a USB drive for installation into PolyScope.

In PolyScope, 
1) Goto Setup -> URCaps
2) Add (+) the .urcaps file
3) Restart
4) Goto Program Robot
5) Program New, Load or start from template
6) tab Structure -> subtab URCaps
7) click EZGripper
8) move EZGripper to top of program
9) tab Command
10) click Calibrate ---- this must be done before using the EZGrippers.  That is why it is at the top of the program.
11) add additional EZGripper instances for open and closing the grippers
