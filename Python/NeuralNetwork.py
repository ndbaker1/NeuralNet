import math
import random

class NeuralNetwork:
    '''Structure :: [VALUE, BIAS, [WEIGHTS]]'''

    #Creates Input, Hidden, and Output Layers with Weights
    def __init__(self, inputs, hidden_layers, outputs):
        self.net = []
        #Create Input Layer
        self.net.append([0]*inputs)
        #Create Hidden Layers
        for i in range(0, len(hidden_layers)):
            self.net.append([None]*hidden_layers[i])
        #Create Output Layers
        self.net.append([None]*outputs)        
        #Perform Weight/Bias/Value Assignment
        for nl in range(1, len(self.net)):
            for n in range(0, len(self.net[nl])):
                self.net[nl][n] = [0]*(len(self.net[nl-1])+2)
    
    #Assigns Random weight values in a given range
    def randomizeWeights(self, limit):
        self.limit = limit
        for nl in range(1,len(self.net)):
            for neuron in self.net[nl]:
                for i in range(1, len(neuron)):
                    neuron[i] = (1 - 2*random.random())*limit

    #Adjusts the weights based on a set of Predetermined Data
    def train(self, InputSet, OutputSet):
        for i in range(0,len(InputSet)):
            self.run(InputSet[i])
            self.backPropragate(OutputSet[i], 0.5)
    '''
    #Experimental
    def check(self):
        for layer in range(1, len(self.net)):
            for neuron in self.net[layer]:
                for weight in range(1, len(neuron)):
                    if neuron[weight] < -10:
                        neuron[weight] = -10
                    elif neuron[weight] > 10:
                        neuron[weight] = 10
    '''
    
    #Adjusts Weights and Bias according to backpropagation
    #adjustment(for a weight) = output*(1-output)*delta*input
    #-see backpropagation.pdf for formula
    def backPropragate(self, TargetOutput, learningRate):
        #Current and Front Error Arrays
        error = [None]*2
        for layer in range(len(self.net)-1, 0, -1): #Backwards index Loop from Last to 2nd index
            error[1] = error[0]
            error[0] = [0]*len(self.net[layer])
            for neuron in range(0,len(error[0])):
                derrivedError = 0
                if (layer == len(self.net)-1): #Output Layer Case
                    derrivedError = (TargetOutput[neuron] - self.net[-1][neuron][0])
                else:
                    for error_i in range(0, len(error[1])): #Sum of Error*Weight of the Neurons in front
                        derrivedError += error[1][error_i]*self.net[layer+1][error_i][neuron+2]
                error[0][neuron] = self.sigmoidError(self.net[layer][neuron][0])*derrivedError
                self.net[layer][neuron][1] += learningRate*derrivedError
                for weight in range(2,len(self.net[layer][neuron])):
                    if (layer == 1): #Input Layer Reference Case
                        self.net[layer][neuron][weight] += learningRate*error[0][neuron]*self.net[layer-1][weight-2]
                    else:
                        self.net[layer][neuron][weight] += learningRate*error[0][neuron]*self.net[layer-1][weight-2][0]
        
                    
    #Processes Input and Returns Output
    def run(self, inputs):
        if (self.newInput(inputs)):
            self.processInput()
        return self.getOutput()
    
    #Passes Input through the Neural Net
    def processInput(self):
        #Proprogate the input signals through the network
        for neuralLayer in range(1, len(self.net)):
            for neuron in self.net[neuralLayer]:
                #Value begins at Bias
                neuron[0] = neuron[1]
                for i in range(2, len(neuron)-1):
                    if (neuralLayer == 1): #Input Layer Reference Case
                        neuron[0] += neuron[i]*self.net[neuralLayer-1][i-2]
                    else:
                        neuron[0] += neuron[i]*self.net[neuralLayer-1][i-2][0]
                neuron[0] = self.sigmoid(neuron[0])
    
    #Inputs values/stimulus into the Input array and returns whether the input is new or old
    def newInput(self, inputs):
        #Throw Exception if the input array is not the correct size
        if(len(inputs) != len(self.net[0])):
            raise ValueError('Input Array Size Does not Match Neural-Network')
        #if the inputs are the same, return false
        if (inputs == self.net[0]):
            return False
        #assign the inputs to the input layer
        self.net[0] = inputs
        return True
    
    #Returns the values from the neurons of the Output Layer
    def getOutput(self):
        output = []
        for i in range(0,len(self.net[-1])):
            output.append(self.net[-1][i][0])
        return output
    
    #Sigmoid Squash Function
    def sigmoid(self, x):
        return 1/(1+math.exp(-x))
    
    #derivative of sigmoid used for error
    def sigmoidError(self, x):
        return x*(1-x)
