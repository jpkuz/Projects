import random
		
"""MAIN CLASS"""
class NeuralNet:
	"""FIELDS
	wgt is array of weights - indexed by l, i, j
	neur is array of neurons - indexed by l, i
	"""
	
	"""initializes a neural network where:
	- inputs is the number of inputs for the training set
	- outputs is the number of outputs for the training set
	- hidden is an array of numbers signifying the number of neurons in each layer
	- alpha is the starting alpha value
	- activations is an array of activation function identifiers for each layer-LENGTH must EQUAL the number of LAYERS"""
	def __init__(self, inputs, outputs, hidden=[], alpha=.01, activations=[]):
		self.alpha = alpha
		self.neur = []
		self.wgt = []
		
		# initializes neur
		self.neur.append(NeuralNet.__neuronInitArr(inputs, 0, activations))
		
		for l in range(len(hidden)):
			self.neur.append(NeuralNet.__neuronInitArr(hidden[l]), l+1, activations)
			
		self.neur.append(NeuralNet.__neuronInitArr(outputs, len(activations)-1, activations))
		
		# initializes wgt
		for l in range(len(self.neur)-1):
			self.wgt.append(NeuralNet.__weightInitArr(len(self.neur[l]), len(self.neur[l+1])))
		
	
	"""ARRAY INITIALIZATION METHODS"""

	"""returns an array to be an initialized neuron layer
	num = number of neurons in layer (not including the unit neuron)"""
	def __neuronInitArr(num, index, activation):
		arr = []
		for i in range(num+1):
			if len(activation)!=0:
				arr.append(Neuron(unit=(i==num), act=activation[index]))
			else:
				arr.append(Neuron(unit=(i==num)))
		return arr


	"""returns an array to be an initialized weight layer
	n1 = neurons in the current layer
	n2 = neurons in the next layer"""
	def __weightInitArr(n1, n2):
		arr = []
		for i in range(n1):
			arr.append([])
			for j in range(n2-1):
				arr[i].append(random.random())
		return arr


	"""DISPLAY METHODS"""

	"""displays the current state of the neurons and the weights"""
	def display(self):
		print("Neurons: ")
		self.displayNeur()
		print("Weights: ")
		self.displayWgt()
		
	def displayNeur(self):
		for l in range(len(self.neur)):
			print('layer %d' % l)
			for i in range(len(self.neur[l])):
				print("%8.4f" % self.neur[l][i].val, end=' ')
			print()
		print()
		
	def displayWgt(self):
		for l in range(len(self.wgt)):
			print('layer %d' % l)
			for i in range(len(self.wgt[l])):
				print('from neuron %d' % i)
				for j in range(len(self.wgt[l][i])):
					print("%8.4f" % self.wgt[l][i][j], end = ' ')
				print()
			print()
		print()
		
		
	"""FEEDFOWARD INPUT/OUTPUT METHODS"""
	
	"""foward propagates inputs stored in the first layer of neurons"""
	def __foward(self):
		for l in range(0, len(self.neur)-1):
			# set NON-INPUT neurons equal to zero EXCEPT for UNIT neurons
			for i in range(len(self.neur[l+1])-1):
				self.neur[l+1][i].zero_val()
			
			# increases value of neurons
			for i in range(len(self.neur[l])):
				act = self.neur[l][i].activate()
				for j in range(len(self.neur[l+1])-1):
					self.neur[l+1][j].inc_val(act * self.wgt[l][i][j])
	
	
	"""replaces old input layer with new input layer
	NOTE: do NOT include the UNIT neuron in the inputs"""
	def __add_inputs(self, inputs):
		if len(inputs) != len(self.neur[0])-1:
			raise Error('inputs not proper length')
		
		for i in range(len(inputs)):
			self.neur[0][i].set_val(inputs[i])
			
	"""returns an array of output neurons
	NOTE: NOT including the unit neuron"""
	def get_outputs(self):
		arr = []
		l = len(self.neur)-1
		for i in range(len(self.neur[l])-1):
			arr.append(self.neur[l][i].val)
		return arr
	
	"""takes inputs, foward propagates, and returns outpus"""
	def foward_prop(self, inputs):
		self.__add_inputs(inputs)
		self.__foward()
		self.get_outputs()
		
		
	"""BACKPROPAGATION METHODS"""
	
	"""compares expected output with actual output and uploads the error"""
	def __input_expected(self, exp):
		l = len(self.neur)-1
		if len(exp) != len(self.neur[l])-1:
			raise Error('expected values not proper length')
		for i in range(len(self.neur[l])-1):
			self.neur[l][i].err = exp[i]-self.neur[l][i].val

	"""propagates error through the neural net"""
	def __update_error(self):
		for l in range(len(self.neur)-1, 0, -1):
			for i in range(len(self.neur[l-1])):
				self.neur[l-1][i].zero_err()
			
			for j in range(len(self.neur[l])-1):
				e = self.neur[l][j].err
				for i in range(len(self.neur[l-1])):
					self.neur[l-1][i].inc_err(self.wgt[l-1][i][j] * e)
	
	"""updates weights according to the backpropagation algorithm"""
	def __update_weights(self):
		for l in range(len(self.neur)-1):
			for j in range(len(self.neur[l+1])-1):
				update = self.neur[l+1][j].err * self.neur[l+1][j].der_act() * self.alpha
				for i in range(len(self.neur[l])):
					self.wgt[l][i][j] += update * self.neur[l][i].val
	
	"""user backpropagation method"""
	def back_prop(self, exp):
		self.__input_expected(exp)
		self.__update_error()
		self.__update_weights()
		
		
	"""DATASET METHODS"""
	
	"""input dataset directly
	note: inputs and outputs must be 2D ARRAYS (of the right size)"""
	def input_dataset(self, inputs, outputs):
		self.inputs = inputs
		self.outputs = outputs
		
	"""iterates and updates the neural network through the dataset"""
	def iterate(self, iterations):
		for ITER in range(iterations):
			for i in range(len(self.inputs)):
				self.foward_prop(self.inputs[i])
				self.back_prop(self.outputs[i])
				
	
	"""MISC METHODS"""
	
	"""sets alpha to new value"""
	def set_alpha(self, alpha):
		self.alpha = alpha


"""a helper class to combine the value of the neuron, its error, and its activation function
also performs simple tasks like activating"""
class Neuron:
	"""identifiers of class activation functions"""
	LINEAR = 0
	SIGMOID = 1
	TANH = 2
	RELU = 3
	LEAKY_RELU = 4
	
	"""initializes a neuron
	if unit set val to one. otherwise set value to a random number
	act sets activation funciton"""
	def __init__(self, unit=False, act=0):
		if unit:
			self.val = 1
		else:
			self.val = random.random()
		self.__act = act
		self.err = 0

	"""returns the value of the activated neuron"""
	def activate(self):
		a = self.__act # assigned for concise code
		val = self.val
		if a == Neuron.LINEAR:
			return val
		elif a == Neuron.SIGMOID:
			return 1/math.exp(-val)
		elif a == Neuron.TANH:
			pos_exp = math.exp(val)
			neg_exp = math.exp(-val)
			return (pos_exp - neg_exp) / (pos_exp + neg_exp)
		elif a == Neuron.RELU:
			if val > 0:
				return val
			else:
				return 0
		elif a == Neuron.LEAKY_RELU:
			if val > 0:
				return val
			else:
				return val / 100
		else:
			return val
	
	"""returns the derivative of the activation function"""
	def der_act(self):
		a = self.__act # assignment for concise code
		val = self.val
		if a == Neuron.LINEAR:
			return 1
		elif a == Neuron.SIGMOID:
			f=1/math.exp(-val)
			return f * (1-f)
		elif a == Neuron.TANH:
			pos_exp = math.exp(val)
			neg_exp = math.exp(-val)
			f = (pos_exp - neg_exp) / (pos_exp + neg_exp)
			return 1 - f**2
		elif a == Neuron.RELU:
			if val > 0:
				return 1
			else:
				return 0
		elif a == Neuron.LEAKY_RELU:
			if val > 0:
				return 1
			else:
				return .01
		else:
			return 1
	
	"""sets value of neuron to 0"""
	def zero_val(self):
		self.val=0
	
	"""INCREASES value of neuron by i"""	
	def inc_val(self, i):
		self.val += i
		
	"""SETS value of neuron to i"""
	def set_val(self, i):
		self.val = i
		
	"""sets value of error to 0"""
	def zero_err(self):
		self.err=0
	
	"""INCREASES value of error by i"""	
	def inc_err(self, i):
		self.err += i
		
	"""SETS value of error to i"""
	def set_err(self, i):
		self.err = i





