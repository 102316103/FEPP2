package com.syscom.fep.frmcommon.util;

import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;
import oshi.util.FormatUtil;

import java.util.Arrays;
import java.util.List;

public class Process {
    public static void getProcess() {
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        OperatingSystem os = si.getOperatingSystem();
        printProcesses(os, hal.getMemory());
        printNetworkInterfaces(hal.getNetworkIFs());
    }

    private static void printProcesses(OperatingSystem os, GlobalMemory memory) {
        System.out.println("Processes: " + os.getProcessCount() + ", Threads: " + os.getThreadCount());
        // Sort by highest CPU
        List<OSProcess> procs = os.getProcesses();

        System.out.println("   PID  %CPU %MEM       VSZ       RSS Name");
        for (int i = 0; i < procs.size(); i++) {
            OSProcess p = procs.get(i);
            if(p.getName().equals("Skype")) {
                //System.out.println(p.getThreadCount());
                System.out.format(" %5d %5.1f %4.1f %9s %9s %s%n", p.getProcessID(),
                        100d * (p.getKernelTime() + p.getUserTime()) / p.getUpTime(),
                        100d * p.getResidentSetSize() / memory.getTotal(), FormatUtil.formatBytes(p.getVirtualSize()),
                        FormatUtil.formatBytes(p.getResidentSetSize()), p.getName());
            }
        }
    }

    private static void printNetworkInterfaces(List<NetworkIF> networkIFs) {
        System.out.println("Network interfaces:");
        for (NetworkIF net : networkIFs) {
            System.out.format(" Name: %s (%s)%n", net.getName(), net.getDisplayName());
            System.out.format("   MAC Address: %s %n", net.getMacaddr());
            System.out.format("   MTU: %s, Speed: %s %n", net.getMTU(), FormatUtil.formatValue(net.getSpeed(), "bps"));
            System.out.format("   IPv4: %s %n", Arrays.toString(net.getIPv4addr()));
            System.out.format("   IPv6: %s %n", Arrays.toString(net.getIPv6addr()));


            boolean hasData = net.getBytesRecv() > 0 || net.getBytesSent() > 0 || net.getPacketsRecv() > 0
                    || net.getPacketsSent() > 0;
            System.out.format("   Traffic: received %s/%s%s; transmitted %s/%s%s %n",
                    hasData ? net.getPacketsRecv() + " packets" : "?",
                    hasData ? FormatUtil.formatBytes(net.getBytesRecv()) : "?",
                    hasData ? " (" + net.getInErrors() + " err)" : "",
                    hasData ? net.getPacketsSent() + " packets" : "?",
                    hasData ? FormatUtil.formatBytes(net.getBytesSent()) : "?",
                    hasData ? " (" + net.getOutErrors() + " err)" : "");
        }
    }

    private static void printNetStat(){

    }

    public static void main(String[] args) {
        getProcess();
    }
}
