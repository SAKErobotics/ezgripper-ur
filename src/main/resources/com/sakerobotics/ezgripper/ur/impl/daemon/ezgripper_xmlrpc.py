#!/usr/bin/env python

#####################################################################
# Software License Agreement (BSD License)
#
# Copyright (c) 2016, SAKE Robotics
# All rights reserved.
# 
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
#    * Redistributions of source code must retain the above copyright
#      notice, this list of conditions and the following disclaimer.
#    * Redistributions in binary form must reproduce the above copyright
#      notice, this list of conditions and the following disclaimer in the
#      documentation and/or other materials provided with the distribution.
#    * Neither the name of the copyright holder nor the names of its
#      contributors may be used to endorse or promote products derived from
#      this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
# ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
# LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
# CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
# SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
# INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
# CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
# ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
# POSSIBILITY OF SUCH DAMAGE.
##

from libezgripper import create_connection, Gripper, find_servos_on_all_ports

import time
#import sys
#import os
import subprocess

import xmlrpclib
from SimpleXMLRPCServer import SimpleXMLRPCServer


def log(msg):
    #timed_msg = time.strftime("%x %X ") + msg
    #print timed_msg
    #filename = os.environ['HOME'] + "/ezgripper.log"
    #filename = "/home/ur/ezgripper.log"
    #with open(filename, "a") as myfile:
    #    myfile.write(timed_msg + "\n")
    pass

def ezg_ping():
    log("PING")
    return ""

def ezg_calibrate():
    log("CALIBRATE")
    gripper.calibrate()
    gripper.goto_position(100, 100)
    return ""

def ezg_open():
    log("OPEN")
    gripper.goto_position(100, 100)
    return ""
    
def ezg_close():
    log("CLOSE")
    gripper.goto_position(0, 100)
    return ""

def ezg_release():
    log("RELEASE")
    gripper.release()
    return ""

def ezg_move(position, effort):
    log("MOVE %d %d"%(position, effort))
    gripper.goto_position(position, effort)
    return ""

def ezg_exec(cmd):
    log("EXEC %s"%(cmd))

    output = ""
    try:
        outdata,errdata = subprocess.Popen(cmd,shell=True,stdout=subprocess.PIPE,stderr=subprocess.PIPE).communicate()
        output = outdata
        if errdata:
            if output:
                output += "\n"
            output += "stderr: " + errdata # "ls /a"
    except Exception, e:
        output = "Exception: "+str(e)
        
    return output

def ezg_get_last_message():
    return last_message

def ezg_get_position():
    position = gripper.get_position()
    log("GET_POSITION = %d"%(position))
    return position

def ezg_get_positions():
    positions = gripper.get_positions()
    log("GET POSITIONS = %s"%(str(positions)))
    return positions

def ezg_get_temperatures():
    temperatures = gripper.get_temperatures()
    log("GET TEMPERATURES = %s"%(str(temperatures)))
    return temperatures

def ezg_get_ids():
    return ids

def ezg_get_devname():
    print "returning dev_name:", dev_name
    return dev_name

def ezg_init_connection():
    global dev_name, ids, last_message, connection, gripper
    try:
        s = find_servos_on_all_ports(max_id=10)
        if s:
            dev_name, ids = s[0]
            print "dev", dev_name
            print "ids", ids
            connection = create_connection(dev_name=dev_name, baudrate=57600)
            #connection = create_connection(dev_name='/dev/ttyUSB0', baudrate=57600)
            #connection = create_connection(dev_name='hwgrep://0403:6001', baudrate=57600)
            #gripper = Gripper(connection, 'gripper1', [1])
            gripper = Gripper(connection, 'gripper1', ids)
            last_message = ""
        else:
            last_message = "No grippers found"
    except Exception, e:
        last_message = str(e)
    return last_message

dev_name = ""
ids = []
last_message = ""
connection = None
gripper = None

init_count = 1

while True:
  print "Trying connection %d"%init_count
  ezg_init_connection()
  if (connection is not None) and (gripper is not None):
    print "t = ", gripper.servos[0].read_temperature()
    break
  time.sleep(1.0)
  init_count += 1

last_message = "Connection tries: %d, temperature: %d"%(init_count,gripper.servos[0].read_temperature())
print last_message
    
log("EZGripper daemon started")

server = SimpleXMLRPCServer(("127.0.0.1", 10017))
server.register_function(ezg_calibrate, "ezg_calibrate")
server.register_function(ezg_open, "ezg_open")
server.register_function(ezg_close, "ezg_close")
server.register_function(ezg_release, "ezg_release")
server.register_function(ezg_move, "ezg_move")
server.register_function(ezg_ping, "ezg_ping")
server.register_function(ezg_exec, "ezg_exec")
server.register_function(ezg_get_last_message, "ezg_get_last_message")
server.register_function(ezg_get_position, "ezg_get_position")
server.register_function(ezg_get_positions, "ezg_get_positions")
server.register_function(ezg_get_temperatures, "ezg_get_temperatures")
server.register_function(ezg_get_ids, "ezg_get_ids")
server.register_function(ezg_get_devname, "ezg_get_devname")
server.register_function(ezg_init_connection, "ezg_init_connection")

log("starting serve_forever()")
server.serve_forever()
