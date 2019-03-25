/**
 * @(#)NeuralNetwork.java
 */

import java.util.*;
import java.io.Serializable;

		////////////////////////////////////////////////////////////////////////////
		////						NEURAL-NETWORK		////
		////	Constructed of a 3D Array of doubles used as neuron weights	////
		////////////////////////////////////////////////////////////////////////////
		
public class NeuralNetwork implements Serializable {
	// [LAYERS][NEURONS][BIAS<INDEX(0)> && WEIGHTS]
	private double[][][] neural_Network;
	
    public NeuralNetwork(int inputs, int[] hidden_layers, int outputs) {
    	neural_Network = new double[hidden_layers.length+1][][];
    	neural_Network[0] = new double[hidden_layers[0]][inputs+1];
    	for(int i = 1; i < hidden_layers.length; i++){
    		neural_Network[i] = new double[hidden_layers[i]][hidden_layers[i-1]+1];
    	}
    	neural_Network[neural_Network.length-1] = new double[outputs][hidden_layers[hidden_layers.length-1]+1];
    	
    	for(double[][] neural_Layer: neural_Network){
    		for(double[] neuron: neural_Layer){
    			// DEFAULT WEIGHTS AND BIAS
    			for(int i = 0; i < neuron.length; i++){
    				neuron[i] = Math.random()*10-5;
    			}
    		}
    	}
    }
	
	private NeuralNetwork(double[][][] net){
		neural_Network = net;
	}
	
    public NeuralNetwork mutatedNetwork(double probability, double variance){
		double[][][] mut = new double[neural_Network.length][][];
		for(int nl = 0; nl < neural_Network.length; nl++){
			mut[nl] = new double[neural_Network[nl].length][];
			for(int n = 0; n < neural_Network[nl].length; n++){
				mut[nl][n] = new double[neural_Network[nl][n].length];
				for(int w = 0; w < neural_Network[nl][n].length; w++){
					mut[nl][n][w] = neural_Network[nl][n][w];
					if (Math.random() < probability){
						mut[nl][n][w] += Math.random()*variance - variance/2;
					}
				}
			}
		}
		return new NeuralNetwork(mut);
	}
    
    public double[] run(double[] inputs){
    	double[] outputs = Arrays.copyOf(inputs, inputs.length);
    	for(int nl = 0; nl < neural_Network.length; nl++){
    		double[] temp = new double[neural_Network[nl].length];
    		for(int n = 0; n < neural_Network[nl].length; n++){
    			for(int w = 0; w < outputs.length; w++){
    				temp[n] += outputs[w]*neural_Network[nl][n][w+1];
    			}
    			temp[n] = sigmoid(temp[n]+neural_Network[nl][n][0]);
    		}
    		outputs = temp;
    	}
    	return outputs;
    }
    
	public static double sigmoid(double x){
		return 1 / (1 + Math.exp(-x));
    }        
}

//								
//													Example Configuration of a NeuralNetwork( 2, new int[] {3,2}, 1 )
//
//																			HIDDEN LAYERS
//													-------------------------------------------------------------
//													|															|
//													|															|			
//													V															V				
//								                               /-+----+-\                                                   
//								                             /-  |    |  \---                                               
//								                          /--    +----+      \---                                           
//								                       /--      /      \         \---                                       
//								                   /---        /         \           \----                                  
//								INPUTS          /--          /            \               \---                        OUTPUTS : 1    
//								             /--            /              \                  \---                          
//								          /--             /                  \                    \-+----+                  
//								       /--               /                    \                   /-|    |\                
//								+----+                  /                       \             /---  +----+  \              
//			THESE					|    |-\               /                          \        /---    /          \             
//			TWO 					+----+  \-----       /                             \ /----       /             \           
//			ARE					      \       \---- /                            /--\          /                 \          
//			INPUTS					        \          \-----                    /---    \        /                   \        
//			(run method)		      			  \        /      \-----          /---          \     /                      \      
//								           \    /              \ +----+ /               \  /                          +----+
//								            \  /                 |    |                   \                           |    |
//								              \                / +----+ \               /  \                          +----+
//								            /  \           /---          \---          /     \                      /      
//								           /     \     /---                  \---    /       \                    /        
//								          /       \/---                          \---          \                 /          
//								         /     /---\                               / \----       \             /           
//								        /  /---      \                           /        \---    \           /             
//								+----+-/---           \                         /             \--- +----+   /              
//								|    |                  \                     /                    |    | /                
//								+----+--                 \                   /                    /+----+                  
//								       \---                \               /                   /--                         
//								           \---             \             /               /----                             
//								               \---           \          /            /---                                  
//								                   \---        \       /          /---                                      
//								                       \---      +----+      /----                                          
//								                           \---  |    |  /---                                               
//								                               \-+----+--        
