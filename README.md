# RIP

RIP Implementation using Distance Vector Routing

Language: Java
Input: Standard I/O

Steps:

	Start the program on all the hosts. (queeg, comet, rhea and glados are the possible hosts)
	Enter the information as provided. (Input files for 2 topologies are provided)
	The program waits for 12 seconds for all the hosts to be ready
	
	Once distance vector routing begins, Covergence is achieved in some time.

	After convergence is acheived, fail a host (stop the program)
	Triggered Updates Method is used to convey failure information 
	Split Horizon with Poison Reverse is used to avoid Count-to-Infinity problem
