# NeuralNets
Forward Feeding Neural Network Models in Java and Python
## Java
Constructed using a 3D Array of doubles to simulate Neurons

The network can learn/adapt through **genetic algorithms** which cover:
```
- random mutation of neuron weights
- reproductive success as dictated by a fitness function
```
Networks are produced en mass and are given a controllable body in my simple physics-like simulation.<br>
This allows me to test the Network's ability to navigate an environment and also keep track of information such as number of generations passed.<br>
Additional Functionality:
```
- Save a serialized 3D array of the most fit neural network
- Change simulation variables in real-time with the LEFT-SHIFT popup menu
- Load/Create new testing rooms from .txt files
```
## Python
Consructed using a 3D List of floats to simulate Neurons

The Network is trained using known inputs and outputs, which are fed through a **backpropagation algorithm**.<br>
An intuitive and non-calculus based approach is described in [BACKPROPAGATION.pdf](Python/BACKPROPAGATION.pdf) which I used for reference
