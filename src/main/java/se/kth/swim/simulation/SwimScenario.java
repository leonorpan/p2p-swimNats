/*
 * Copyright (C) 2009 Swedish Institute of Computer Science (SICS) Copyright (C)
 * 2009 Royal Institute of Technology (KTH)
 *
 * GVoD is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package se.kth.swim.simulation;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.swim.AggregatorComp;
import se.kth.swim.HostComp;
import se.kth.swim.SwimComp;
import se.kth.swim.croupier.CroupierConfig;
import se.sics.p2ptoolbox.simulator.cmd.OperationCmd;
import se.sics.p2ptoolbox.simulator.cmd.impl.ChangeNetworkModelCmd;
import se.sics.p2ptoolbox.simulator.cmd.impl.SimulationResult;
import se.sics.p2ptoolbox.simulator.cmd.impl.StartAggregatorCmd;
import se.sics.p2ptoolbox.simulator.cmd.impl.StartNodeCmd;
import se.sics.p2ptoolbox.simulator.cmd.impl.KillNodeCmd;
import se.sics.p2ptoolbox.simulator.core.network.NetworkModel;
import se.sics.p2ptoolbox.simulator.core.network.impl.DeadLinkNetworkModel;
import se.sics.p2ptoolbox.simulator.core.network.impl.DisconnectedNodesNetworkModel;
import se.sics.p2ptoolbox.simulator.core.network.impl.UniformRandomModel;
import se.sics.p2ptoolbox.simulator.dsl.SimulationScenario;
import se.sics.p2ptoolbox.simulator.dsl.adaptor.Operation;
import se.sics.p2ptoolbox.simulator.dsl.adaptor.Operation1;
import se.sics.p2ptoolbox.simulator.dsl.distribution.ConstantDistribution;
import se.sics.p2ptoolbox.simulator.dsl.distribution.extra.GenIntSequentialDistribution;
import se.sics.p2ptoolbox.util.network.NatType;
import se.sics.p2ptoolbox.util.network.NatedAddress;
import se.sics.p2ptoolbox.util.network.impl.BasicAddress;
import se.sics.p2ptoolbox.util.network.impl.BasicNatedAddress;

/**
 * @author Alex Ormenisan <aaor@sics.se>
 */
public class SwimScenario {
	
	//How much nodes should be started?
	private static Integer size=10;
	//How much nodes should be killed?
	private static Integer killedSize=0;
	//How many nated nodes?
	private static Integer nated=1;

	 private static final Logger log = LoggerFactory.getLogger(SwimScenario.class);
	 private static List<Integer> KILLED;
    private static long seed;
    private static InetAddress localHost;
    //int viewSize, int shuffleSize, long shufflePeriod, long shuffleTimeout
    private static CroupierConfig croupierConfig = new CroupierConfig(10, 5, 2000, 1000); 
    static {
        try {
            localHost = InetAddress.getByName("127.0.0.1");
        } catch (UnknownHostException ex) {
            throw new RuntimeException(ex);
        }
    }

    //Make sure that your dead link set reflect the nodes in your system
    private static final Map<Integer, Set<Pair<Integer, Integer>>> deadLinksSets = new HashMap<Integer, Set<Pair<Integer, Integer>>>();

    static {
        Set<Pair<Integer, Integer>> deadLinks;

        deadLinks = new HashSet<Pair<Integer, Integer>>();
        deadLinks.add(Pair.with(10, 12));
        deadLinks.add(Pair.with(12, 10));
        deadLinksSets.put(1, deadLinks);

        deadLinks = new HashSet<Pair<Integer, Integer>>();
        deadLinks.add(Pair.with(10, 12));
        deadLinks.add(Pair.with(12, 10));
        deadLinks.add(Pair.with(14, 10));
        deadLinksSets.put(2, deadLinks);
    }

    //Make sure disconnected nodes reflect your nodes in the system
    private static final Map<Integer, Set<Integer>> disconnectedNodesSets = new HashMap<Integer, Set<Integer>>();

    static {
        Set<Integer> disconnectedNodes;

        disconnectedNodes = new HashSet<Integer>();
        disconnectedNodes.add(4);
        disconnectedNodes.add(6);
        disconnectedNodes.add(8);
        disconnectedNodes.add(10);
        disconnectedNodes.add(12);
        disconnectedNodesSets.put(1, disconnectedNodes);

        disconnectedNodes = new HashSet<Integer>();
        disconnectedNodes.add(10);
        disconnectedNodes.add(12);
        disconnectedNodesSets.put(2, disconnectedNodes);
    }

    static Operation1<StartAggregatorCmd, Integer> startAggregatorOp = new Operation1<StartAggregatorCmd, Integer>() {

        @Override
        public StartAggregatorCmd generate(final Integer nodeId) {
            return new StartAggregatorCmd<AggregatorComp, NatedAddress>() {
                private NatedAddress aggregatorAddress;
                
                @Override
                public Class getNodeComponentDefinition() {
                    return AggregatorComp.class;
                }

                @Override
                public AggregatorComp.AggregatorInit getNodeComponentInit() {
                    aggregatorAddress = new BasicNatedAddress(new BasicAddress(localHost, 23456, nodeId));
                    //return new AggregatorComp.AggregatorInit(aggregatorAddress,size,new ArrayList<Integer>());
                    List<Integer> nodesKilled = new ArrayList<Integer>();
                    //nodesKilled.add(4);
//                    nodesKilled.add(4);
//                    nodesKilled.add(6);
//                    nodesKilled.add(8);
//                    nodesKilled.add(10);
//                    nodesKilled.add(12);
                    return new AggregatorComp.AggregatorInit(aggregatorAddress,size,nodesKilled);

                }

                @Override
                public NatedAddress getAddress() {
                    return aggregatorAddress;
                }

            };
        }
    };

    static Operation1<StartNodeCmd, Integer> startNodeOp = new Operation1<StartNodeCmd, Integer>() {

        @Override
        public StartNodeCmd generate(final Integer nodeId) {
            return new StartNodeCmd<HostComp, NatedAddress>() {
                private NatedAddress nodeAddress;

                @Override
                public Class getNodeComponentDefinition() {
                    return HostComp.class;
                }

                @Override
                public HostComp.HostInit getNodeComponentInit(NatedAddress aggregatorServer, Set<NatedAddress> bootstrapNodes) {

                    if (nodeId % 2 == 0) {
                        //open address
                        nodeAddress = new BasicNatedAddress(new BasicAddress(localHost, 12345, nodeId));
                    } else {
                        //nated address
                        nodeAddress = new BasicNatedAddress(new BasicAddress(localHost, 12345, nodeId), NatType.NAT, bootstrapNodes);
                    }
//                    if (bootstrapNodes.contains(nodeAddress)){
//                    	bootstrapNodes.remove(nodeAddress.getId());
//                    }
                    /**
                     * we don't want all nodes to start their pseudo random
                     * generators with same seed else they might behave the same
                     */

                    long nodeSeed = seed + nodeId;
                    log.info("number of bootstrapnodes for {} is {}",new Object[]{nodeId,bootstrapNodes.size()});
                    System.out.println("RECONNECTING NODE: "+nodeId);
                    return new HostComp.HostInit(nodeAddress, bootstrapNodes, aggregatorServer, nodeSeed, croupierConfig);
                }

                @Override
                public Integer getNodeId() {
                    return nodeId;
                }

                @Override
                public NatedAddress getAddress() {
                    return nodeAddress;
                }

                @Override
                public int bootstrapSize() {
                    return 5;
                }

            };
        }
    };

    static Operation1<KillNodeCmd, Integer> killNodeOp = new Operation1<KillNodeCmd, Integer>() {

        public KillNodeCmd generate(final Integer nodeId) {
        	System.out.println("KILLING NODE: "+nodeId);
            return new KillNodeCmd() {
                public Integer getNodeId() {
                    return nodeId;
                }
            };
        }

    };

    //Usable NetworkModels:
    //1. UniformRandomModel
    //parameters: minimum link latency, maximum link latency
    //by default Simulator starts with UniformRandomModel(50, 500), so minimum link delay:50ms, maximum link delay:500ms
    //2. DeadLinkNetworkModel
    //composite network model that can be built on any other network model
    //parameters: network model, set of dead links (directed links)
    //Pair<1,2> means if node 1 will try to send a message to node 2, the simulator will drop this message, since this is a dead link
    //3. DisconnectedNodesNetworkModel
    //composite network model that can be built on any other network model
    //parameters: network model, set of disconnected nodes
    //a disconnected node will not be able to send or receive messages
    static Operation1<ChangeNetworkModelCmd, Integer> disconnectedNodesNMOp = new Operation1<ChangeNetworkModelCmd, Integer>() {

        @Override
        public ChangeNetworkModelCmd generate(Integer setIndex) {
            NetworkModel baseNetworkModel = new UniformRandomModel(50, 500);
            System.out.println("DISCONNECTING NODE: "+disconnectedNodesSets.get(setIndex));
            NetworkModel compositeNetworkModel = new DisconnectedNodesNetworkModel(setIndex, baseNetworkModel, disconnectedNodesSets.get(setIndex));
            return new ChangeNetworkModelCmd(compositeNetworkModel);
        }
    };
    
    static Operation1<ChangeNetworkModelCmd, Integer> reConnectedNodesNMOp = new Operation1<ChangeNetworkModelCmd, Integer>() {

        @Override
        public ChangeNetworkModelCmd generate(Integer setIndex) {
            NetworkModel baseNetworkModel = new UniformRandomModel(50, 500);
            
            return new ChangeNetworkModelCmd(baseNetworkModel);
        }
    };

    static Operation1<ChangeNetworkModelCmd, Integer> deadLinksNMOp = new Operation1<ChangeNetworkModelCmd, Integer>() {

        @Override
        public ChangeNetworkModelCmd generate(Integer setIndex) {
            NetworkModel baseNetworkModel = new UniformRandomModel(50, 500);
            NetworkModel compositeNetworkModel = new DeadLinkNetworkModel(setIndex, baseNetworkModel, deadLinksSets.get(setIndex));
            return new ChangeNetworkModelCmd(compositeNetworkModel);
        }
    };

    static Operation<SimulationResult> simulationResult = new Operation<SimulationResult>() {

        public SimulationResult generate() {
            return new SimulationResult() {

                @Override
                public void setSimulationResult(OperationCmd.ValidationException failureCause) {
                    SwimSimulationResult.failureCause = failureCause;
                }
            };
        }
    };

    //Operations require Distributions as parameters
    //1.ConstantDistribution - this will provide same parameter no matter how many times it is called
    //2.BasicIntSequentialDistribution - on each call it gives the next int. Works more or less like a counter
    //3.GenIntSequentialDistribution - give it a vector. It will draw elements from it on each call. 
    //Once out of elements it will give null. 
    //So be carefull for null pointer exception if you draw more times than elements
    //check se.sics.p2ptoolbox.simulator.dsl.distribution for more distributions
    //you can implement your own - by extending Distribution
    public static SimulationScenario simpleBoot(final long seed) {
        SwimScenario.seed = seed;
        SimulationScenario scen = new SimulationScenario() {
            {
                StochasticProcess startAggregator = new StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(1, startAggregatorOp, new ConstantDistribution(Integer.class, 0));
                    }
                };

                StochasticProcess startPeers = new StochasticProcess() {
                    {             
						Random random = new Random();
						List<Integer> nodeIds = new ArrayList<Integer>();
						while (nodeIds.size() < size-nated) {
							int rand = random.nextInt(200);
							if (!nodeIds.contains(rand)) {
								if (rand % 2 == 0) {
									// open node
									nodeIds.add(rand);
								}
							}
						}
						Integer[] nodes = (Integer[]) nodeIds
								.toArray(new Integer[size-nated]);
						eventInterArrivalTime(constant(1000));
						raise(size-nated, startNodeOp,
								new GenIntSequentialDistribution(nodes));
                    }
                };
                
                StochasticProcess startNatPeers = new StochasticProcess() {
                    {             
						Random random = new Random();
						List<Integer> nodeIds = new ArrayList<Integer>();
						while (nodeIds.size() < nated) {
							int rand = random.nextInt(200);
							if (!nodeIds.contains(rand)) {
								if (rand%2==1){
									// nated node
										nodeIds.add(rand);
								}
							}
						}
						Integer[] nodes = (Integer[]) nodeIds
								.toArray(new Integer[nated]);
						eventInterArrivalTime(constant(1000));
						raise(nated, startNodeOp,
								new GenIntSequentialDistribution(nodes));
                    }
                };
                
                StochasticProcess reconnectPeer = new StochasticProcess() {
                    {
                    	eventInterArrivalTime(constant(1000));
                        raise(1, startNodeOp, new GenIntSequentialDistribution(new Integer[]{10}));
                    }
                };
                

                StochasticProcess killPeers = new StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        //raise(5, killNodeOp, new GenIntSequentialDistribution(new Integer[]{4,6,8,10,12}));
                        raise(1, killNodeOp, new ConstantDistribution(Integer.class,4));
                    }
                };

                StochasticProcess deadLinks1 = new StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(1, deadLinksNMOp, new ConstantDistribution(Integer.class, 1));
                    }
                };

                StochasticProcess disconnectedNodes1 = new StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        
                        raise(1, disconnectedNodesNMOp, new ConstantDistribution(Integer.class, 1));
                    }
                };

                StochasticProcess fetchSimulationResult = new StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(1, simulationResult);
                    }
                };

                startAggregator.start();
                startPeers.startAfterTerminationOf(1000, startAggregator);
                //startNatPeers.startAfterTerminationOf(1000, startPeers);
              //killPeers.startAfterTerminationOf(35000, startPeers);
                //deadLinks1.startAfterTerminationOf(10000,startPeers);
               //disconnectedNodes1.startAfterTerminationOf(10000, startPeers);
               //reconnectPeer.startAfterTerminationOf(10000, killPeers);
                fetchSimulationResult.startAfterTerminationOf(150*1000, startPeers);
                terminateAfterTerminationOf(1000, fetchSimulationResult);

            }
        };

        scen.setSeed(seed);

        return scen;
    }
}
