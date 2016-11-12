Initial Framework:
Main Activity and reading data from Muse - Liam
Signal class for containing Muse data - 
Wrapper for queue (SignalQueue) containing unprocessed signals - Yunhua
Dictionary (wrapped HashMap) converting from a SignalQueue to letters - 




***NOTES***

Signal constructor: Signal(boolean b) // b = true for blink, false for clench

SignalQueue will have two constructors:
  - one taking in data from Muse
  - one from String
  - overloads the = operator
 
morseDictionary class:
  - default constructor initializes private HashMap: keys are SignalQueues, values are Characters
  - translate(SignalQueue) returns a char that the SignalQueue represents, null if invalid SignalQueue
