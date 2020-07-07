=========================================================================================
		  Distributed system programming ass1 by Dror Kliger
=========================================================================================
		THIS FILE CONTAINS INSTRUCTIONS FOR THIS PROJECT
How to use:
- In order to run this project, you will have to run localapp.jar.
  When executing the jar you should use the following cmd commands: 
	1) cd localapp\
	2) java -jar localapp.jar #input-file-name# #output-file-name# n terminate
	
	When n is an integer number that represents the number of worker instances
	per input file and terminate is an optional argument that asks the manager to
	terminate all the workers and itself when it finishes all its current work.

- The type of Instance I used for this task is T2Small. 
- The time it took the system to finish proccessing the input files was 11 minutes
  when entering one worker per input file.

=========================================================================================
System's flow:

- Localapp:
        The localapp gets all the input files names from the user. Every line in these
	input files has to contain one of these three commnds {ToHTML,ToImage,ToText}
	and a URL source of a PDF file. It uploads the files to a specific bucket in s3
	and sends messages in accordance. In addition, the localapp starts a new
	manager instance (if not already exist).

- Manager:
	The manager listens to the queue of messeges from the locals (might be multiple)
	and generates task for every line of every input file sent by the locals.The 
	manager starts #number-of-unfinished-input-files#/n number of worker instances
	and send the tasks to a queue that is listened by all the workers.

- Workerapp:
	The worker takes one task from the queue and proccess it. Through it's proccess
	the worker tries to download the PDF file from the URL that in the task. Later
	on, if succeeded, it converts the file to another type according to the command
	that is given in the task. When finished, the worker uploads the new converted
	file to S3 and sends a message back to the manager accordingly.

- Managerapp:
	The manager listens to the queue to get finished tasks results summarizes them
	to one summary file. When an input file is finished to be proccessed, the
	manager sends an s3 URL of the summary file to the specific local.

- Localapp:
	The local then download and convert the file into an HTML file.

=========================================================================================

Systems mechanisms:
- Security:
	The sensitive content in this project is the credentials file. We DO NOT upload it
	to the could in any time. Instead, I gave the relevant instances permissions to
	approach amazon's s3, ec2 and sqs services.

- Scalability:
	The main scaling issue I had to handle in this project was in the manager's
	mechanism. First, there is a possibility that a large number of locals will try to
	connect simultaniously and cause the manager a massive work that would take a very
	long time. In order to solve this issue, I made a seperation between listening to
	the input file queue, the worker's tasks sender and the worker's finished tasks
	proccessor. This seperation was made by threads that run in a synchronized way.
	Second, I had to deal with space problem. When a numerous number of locals sends
	their input files, the amount of data the manager could have to store is huge.
	The solution I made for that problem is to limit the manager's instance's space
	usage. It means that when the manager passes 50% of its space usage, it holds the
	input stream until the situation is fixed.

- Persistence:
	In order to maintain the persistence, I increased the visibility timeout of the 
	vulnereables queues. The higher visibility helps to ensure persistence in a case
	that a worker collapsed (other one can pick the message again). To realy make it
	usable, I made the workers deleted the messages only after they finish working on
	the task.

- Termination:
	A temination proccess starts when a local runs with the optional terminate argument.
	After this local gets all the the input files's summaries it supposed to recive from
	the manager, it sends a termination message to the manager. The manager gets this
	message, stops all new input files reception, waits until all the current tasks are
	finished and sends number of workers amount of messages to the workers. The workers
	recives this message and send acknowledge, it is only when every one of the workers
	sent an ack message, the manager move on to delete every queue the system uses and
	sends back an ack message to the relevant local.
============================================================================================