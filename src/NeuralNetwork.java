/**
 * @(#)NeuralNetwork.java
 */

import java.util.*;
import java.io.Serializable;

		////////////////////////////////////////////////////////////////////////////
		////						NEURAL-NETWORK								////
		////	Constructed of a 3D Array of doubles used as neuron weights		////
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
//			THESE				|    |-\               /                          \        /---    /          \             
//			TWO 				+----+  \-----       /                             \ /----       /             \           
//			ARE					      \       \---- /                            /--\          /                 \          
//			INPUTS				        \          \-----                    /---    \        /                   \        
//			(run method)		         \        /      \-----          /---          \     /                      \      
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


/*
	INEFFICIENT CLASS BASED NEURAL-NETWORK
public class NeuralNetwork implements Serializable {
	
	private NeuralLayer[] neural_layers;
	
    public NeuralNetwork(int inputs, int[] neuron_layers) {
    	neural_layers = new NeuralLayer[neuron_layers.length];
    	neural_layers[0] = new NeuralLayer(inputs, neuron_layers[0]);
    	for(int i = 1; i < neural_layers.length; i++){
    		neural_layers[i] = new NeuralLayer(neuron_layers[i-1], neuron_layers[i]);
    	}
    }
    
    public NeuralNetwork mutatedNetwork(double probability, double variance){
    	int[] n_layers = new int[neural_layers.length];
    	for(int i = 0; i < n_layers.length; i++){
    		n_layers[i] = neural_layers[i].getNeurons().length;
    	}
    	NeuralNetwork mut = new NeuralNetwork(	neural_layers[0].getNeurons()[0].getNumInputs(), n_layers );
    	
    	for(int i = 0; i < mut.neural_layers.length; i++){
    		for(int j = 0; j < mut.neural_layers[i].getNeurons().length; j++){
    			mut.neural_layers[i].getNeurons()[j] = neural_layers[i].getNeurons()[j].clone();
    			if(Math.random() < probability){
    				mut.neural_layers[i].getNeurons()[j].mutate(variance);	
    			}
    		} 
    	}
    	return mut;
    }
    
    public double[] run(double[] inputs){
    	loadStimuli(inputs);
    	passData();
    	return getOutputs(neural_layers.length-1);
    }    
    	
    public void loadStimuli(double[] inputs){
    	for(Neuron neuron: neural_layers[0].getNeurons()){
    		neuron.calc(inputs);
    	}
    }
    public void passData(){
    	for(int i = 1; i < neural_layers.length; i++){
			neural_layers[i].takeInputs(neural_layers[i-1]);
    	}
    }
    public double[] getOutputs(int layer){
    	double[] outs = new double[neural_layers[layer].getNeurons().length];
    	for(int i = 0; i < outs.length; i++){
    		outs[i] = neural_layers[layer].getNeurons()[i].getValue();
    	}
		reset();
    	return outs;
    }
    
    public void reset(){
    	for(NeuralLayer l : neural_layers){
    		for(Neuron n: l.getNeurons()){
    			n.reset();
    		}
    	}
    }
}

class NeuralLayer implements Serializable{
	private Neuron[] neurons;
	public NeuralLayer(int inputs, int num_neurons){
		neurons = new Neuron[num_neurons];
		for(int i = 0; i < num_neurons; i++){
			neurons[i] = new Neuron(inputs);
		}
	}
	
	public void takeInputs(NeuralLayer nlayer){
		for(Neuron n: neurons){
			n.calc(nlayer);
		}
	}
	
	public Neuron[] getNeurons(){
		return neurons;
	}
}

class Neuron implements Serializable{
	private double[] weights;
	private double value;
	private double bias;
	public Neuron(int inputs){
		bias = Math.random()*8-4;
		value = 0;
		weights = new double[inputs];
		for(int i = 0; i < weights.length; i++){
			weights[i] = Math.random()*8-4;//random weights
		}
	}
	
	public void calc(double[] inputs){
		for(int i = 0; i < weights.length; i++){
			value += inputs[i]*weights[i];
		}
		value = sigmoid(value+bias);
	}
	
	public void calc(NeuralLayer inputs){
		for(int i = 0; i < weights.length; i++){
			value += inputs.getNeurons()[i].getValue()*weights[i];
		}
		value = sigmoid(value+bias);
	}
	
	public double getValue(){
		return value;
	}
	
	public int getNumInputs(){
		return weights.length;
	}
	
	public void reset(){
		value = 0;
	}
	
	public static double sigmoid(double x){
		return 1 / (1 + Math.exp(-x));
    }
    
    public void mutate(double v){
    	bias += Math.random()*v-v/2.0;
		for(int i = 0; i < weights.length; i++){
			weights[i] += Math.random()*v-v/2.0;
		}
    }
    
    public Neuron clone(){
    	Neuron n = new Neuron(weights.length);
    	n.bias = this.bias;
    	for(int i = 0; i < weights.length; i++){
    		n.weights[i] = new Double(weights[i]);
    	}
    	return n;
    }
}
*/

