package com.mycompany.shortestjobfirst;


import java.text.DecimalFormat;
import java.util.*;
import java.lang.*;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.provisioners.*;
import org.cloudbus.cloudsim.core.CloudSim;

public class rrr {
	private static List<Cloudlet> cloudletList;
	private static List<Vm> vmlist;

        public static HashMap<Integer, Long> sortByValue(HashMap<Integer, Long> hm)
        {
            List<Map.Entry<Integer, Long> > list =
                   new LinkedList<Map.Entry<Integer, Long> >(hm.entrySet());

            Collections.sort(list, new Comparator<Map.Entry<Integer, Long> >() {
                public int compare(Map.Entry<Integer, Long> o1,
                                   Map.Entry<Integer, Long> o2)
                {
                    return (o1.getValue()).compareTo(o2.getValue());
                }
            });

            HashMap<Integer, Long> temp = new LinkedHashMap<Integer, Long>();
            for (Map.Entry<Integer, Long> aa : list) {
                temp.put(aa.getKey(), aa.getValue());
            }
            return temp;
        }
	public static void main(String[] args) {

		Log.printLine("Starting Shortest Job First scheduling...");

	        try{
	            	int num_user = 1;   // number of cloud users
	            	Calendar calendar = Calendar.getInstance();
	            	boolean trace_flag = false;  // mean trace events

	            	CloudSim.init(num_user, calendar, trace_flag);

	            	@SuppressWarnings("unused")
					Datacenter datacenter0 = createDatacenter("Datacenter_1");

	            	DatacenterBroker broker = createBroker();
	            	int brokerId = broker.getId();

	            	vmlist = new ArrayList<Vm>();
	            	int vmid=0;
	            	int mips = 64; 
	            	long size = 10000; //image size (MB)
	            	int ram = 1024; //vm memory (MB)
	            	long bw = 1000;
                        int pesNumber = 1; //number of CPUs
	            	String vmm = "Xen"; //VMM name
                        for(int i=0;i<10;i++){
                            vmlist.add(new Vm(vmid, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerSpaceShared()));
                            vmid++;
                        }
	            	broker.submitVmList(vmlist);
	 
	            	cloudletList = new ArrayList<Cloudlet>();
	            	int id = 0;
                        int min=200000, max=299999;
	            	long fileSize = 300;
	            	long outputSize = 300;
	            	UtilizationModel utilizationModel = new UtilizationModelFull();
                        for(int i=0;i<100;i++){
                            long length = (long)(Math.random()*(max-min+1)+min);
                            cloudletList.add(new Cloudlet(id, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel));
                            id++;
                        }
                        
                        HashMap<Integer, Long> map = new HashMap<>(); 
                        for(int i=0;i<100;i++){
                            long check = cloudletList.get(i).getCloudletLength();
                            map.put(i, check);
                        }
                        HashMap<Integer, Long> sortlist = sortByValue(map);
                        
                        cloudletList.clear();
                        int v=0;
                        Cloudlet arr[] = new Cloudlet[100];
                        for (Map.Entry<Integer, Long> en : sortlist.entrySet())
                        { 
                            arr[v] = new Cloudlet(en.getKey(), en.getValue(), pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
                            arr[v].setUserId(brokerId);
                            cloudletList.add(arr[v]);
                            v++;
                        }
	            	broker.submitCloudletList(cloudletList);
                        for(int i=0;i<100;i++){
                            int j=i%vmlist.size();
                            broker.bindCloudletToVm(cloudletList.get(i).getCloudletId(),vmlist.get(j).getId());
                        }
                        
	            	CloudSim.startSimulation();

	            	List<Cloudlet> newList = broker.getCloudletReceivedList();

	            	CloudSim.stopSimulation();

	            	printCloudletList(newList);

	            	Log.printLine("Shortest Job First scheduling is finished!");
                        
	        }
	        catch (Exception e) {
	            e.printStackTrace();
	            Log.printLine("The simulation has been terminated due to an unexpected error");
	        }
	    }

		private static Datacenter createDatacenter(String name){

	    	List<Host> hostList = new ArrayList<Host>();
	    	List<Pe> peList = new ArrayList<Pe>();
	    	int mips = 1000;
	    	peList.add(new Pe(0, new PeProvisionerSimple(mips)));
	        int hostId=0;
	        int ram = 8192; //host memory (MB)
	        long storage = 1000000; //host storage
	        int bw = 10000;

	        hostList.add(new Host(hostId,new RamProvisionerSimple(ram),new BwProvisionerSimple(bw),storage,peList,new VmSchedulerTimeShared(peList)));
	        String arch = "x86";      // system architecture
	        String os = "Linux";          // operating system
	        String vmm = "Xen";
	        double time_zone = 10.0;         // time zone this resource located
	        double cost = 3.0;              // the cost of using processing in this resource
	        double costPerMem = 0.05;		// the cost of using memory in this resource
	        double costPerStorage = 0.001;	// the cost of using storage in this resource
	        double costPerBw = 0.0;			// the cost of using bw in this resource
	        LinkedList<Storage> storageList = new LinkedList<Storage>();	//we are not adding SAN devices by now

	        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
	                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);
	        Datacenter datacenter = null;
	        try {
	            datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
	        }
                catch (Exception e) {
	            e.printStackTrace();
	        }
	        return datacenter;
	    }

	    private static DatacenterBroker createBroker(){

	    	DatacenterBroker broker = null;
	        try {
			broker = new DatacenterBroker("Broker");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	    	return broker;
	    }

	    private static void printCloudletList(List<Cloudlet> list) {
	        int size = list.size();
	        Cloudlet cloudlet;

	        String indent = "    ";
	        Log.printLine();
	        Log.printLine("========== OUTPUT ==========");
	        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent +
	                "Data center ID" + indent + "VM ID" + indent + "Time" + indent + "Start Time" + indent + "Finish Time");

	        DecimalFormat dft = new DecimalFormat("###.##");
	        for (int i = 0; i < size; i++) {
	            cloudlet = list.get(i);
	            Log.print(indent + cloudlet.getCloudletId() + indent + indent);

	            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS){
	                Log.print("SUCCESS");

	            	Log.printLine( indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId() +
	                     indent + indent + dft.format(cloudlet.getActualCPUTime()) + indent + indent + dft.format(cloudlet.getExecStartTime())+
                             indent + indent + dft.format(cloudlet.getFinishTime()));
	            }
	        }

	    }
}
