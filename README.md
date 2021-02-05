# A template for iv4XR agent-based tests

This small project contains four classes that give you a minimalistic template of what need to be implemented to build an interface between iv4XR agents and your System Under Test (SUT), and then showing a simple test.

It contains the following:

   1. The class  `MyEnv`. This is the interface between the agents and the SUT. You need to implement this. It shows a couple of methods, but in a minimum setup, only one of them needs to be redefined by you.

   2. The class `TacticLib` you will find a handful of typical tactics to
   implement. Tactics are needed so that your agents can operate at much higher level of control rather than literally at the level of primitive actions provided by `MyEnv`. You will need to provide the implementation
   of these tactics.

   3. The class `GoalLib`, provides a number of typical goal-structures that you can use for composing your testing tasks. You will need to implement at least some of them.

   4. The class `A_SimpleExample_of_Test_Using_TestAgent` shows a single test, using an iv4XR test-agent and components named above. Note that this class does not include the deployment of the SUT itself, since I don't have the SUT. You have to add some code for launching the SUT there.


Good luck! :)   
