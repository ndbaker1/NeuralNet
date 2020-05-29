from NeuralNetwork import NeuralNetwork

training_set_input = [[0,1,0],[0,0,1],[0,1,1],[1,1,1],[1,0,1],[1,1,0],[1,0,0],[0,0,0]]
training_set_output = [[1],[0],[1],[1],[0],[1],[0],[0]]

def test():
    print(network.net)
    print(network.run([0,1,0]))
    print(network.run([1,1,0]))
    print(network.run([0,1,1]))
    print(network.run([1,1,1]))
    print(network.run([0,0,1]))
    print(network.run([1,0,0]))
    print(network.run([1,0,1]))
    print(network.run([0,0,0]))
    
def train():
    print(runs, 'Training Runs . . .\n')
    for i in range(runs):
        network.train(training_set_input,training_set_output)
    

if __name__ == '__main__':
    network = NeuralNetwork(3,[20,10],1)
    network.randomizeWeights(10)
    print('Initialized . . .')
    runs = 10
    test()
    
    while(input() != 'q'):
        train()
        test()
