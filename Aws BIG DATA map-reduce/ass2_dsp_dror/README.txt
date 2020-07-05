=========================================================================================
			  DSP ASSIGNMENT 2 by Dror Kliger
=========================================================================================
		THIS FILE CONTAINS INSTRUCTIONS FOR THIS PROJECT
How to use:
- In order to run this project, you will have to run collocationExtractionMain class
  in collocationExtractionMain folder with one argument ("heb" or "eng") that will 
  affect the corpus language to analize.
  The system assumes that there are three jar file in the AWS S3 directory: 
  "dror-ass2/jars/" named: 1)"collocationExtraction.jar"
			   2)"step2CalculateLog.jar"
			   3)"step3SortForTop100.jar"
  and two text file in "collocationExtractionMain" folder of the project named:
			   1)"eng_stop_words.txt"
			   2)"heb_stop_words.txt".


- In this project I used ten M4Large EC2 instances.

=========================================================================================
System's flowchart:

- Main:
        The Main reads the english\hebrew stop words file, according to the argument
	it gets. Then, it build an AWS EMR client, generally configuring the three
	steps of the job and sends a running request.

- step1(collocationExtraction):
	This step gets an argument in the shape of a string that includes all the stop
	words, seperated with \t sign and eng\heb 1gram and 2gram corpus s3 link.
	Mapper: The purpose of the mapper in this step is to filter the stop words and
	generate data about 1-gram and 2-gram word that would help us later count them.
	Combiner: In order to ease the job of the reducer that would come later on,
	I added a combiner that would summarize the value numbers to one number 
	counter.
	Partitioner: The patitioning would have to not seperate lines from the same
	decade to not harm the next steps to come.	
	Reducer: The purpose of the reducer in this step is to summarize all the lines
	numbers to one counter, add to each 2-gram and 1-gram the decade number and to
	each 2-gram line, the first word counter we generated.

- step2:
	The purpose of this step is to arrange all the relevant data and calculate a 
	formula for the log likelihood ratio of every 2-gram.
	Mapper: This step's mapper change the sorting algorithm to sort by the second
	words of the 2-gram lines, in order to add them the number of occurences of
	the second word.
	Reducer: This step's reducer adds the number of occurences of the second word
	of every 2-gram to it's line and calculates their likelihood.

- step3:
	The purpose of this step is to arrange all the 2-grams lines by thier log-
	likelihood order for every decade.
	Mapper: This step's mapper change the keys and the values to make the reducer
	step easier.
	Reducer: This step's reducer makes list of the top hundred 2-gram combinations
	by thier log likelihood for every decade.
=========================================================================================

- Results Descussion:
	When cheking the results, We can notice for the improvement the combiner made
	in the first step.
	When not using the combiner we got the followin result:
	Map-Reduce Framework
		Map input records=4215010703
		Map output records=3402817509
		Map output bytes=83633748281
		Map output materialized bytes=19393753672
		Input split bytes=91879
		Combine input records=0
		Combine output records=0
	<______	Reduce input groups=623970463    ___________>
	<______	Reduce shuffle bytes=19393753672 ___________>
		Reduce input records=3402817509 
		Reduce output records=623970463 
		Spilled Records=9640613635
		Shuffled Maps =11237
		Failed Shuffles=0
		Merged Map outputs=11237
		GC time elapsed (ms)=2122697
		CPU time spent (ms)=120989750
		Physical memory (bytes) snapshot=593591996416
		Virtual memory (bytes) snapshot=2272263921664
		Total committed heap usage (bytes)=473267961856

	When using the combiner we got the followin result:
	Map-Reduce Framework
		Map input records=4215010703
		Map output records=3402817509
		Map output bytes=83633748281
		Map output materialized bytes=8289356120
		Input split bytes=91879
		Combine input records=3462109519
		Combine output records=683367650
	<______	Reduce input groups=623970463 ___________>
	<______	Reduce shuffle bytes=8289356120 ___________>
		Reduce input records=624075640
		Reduce output records=623970463
		Spilled Records=1754101944
		Shuffled Maps =11237
		Failed Shuffles=0
		Merged Map outputs=11237
		GC time elapsed (ms)=1001174
		CPU time spent (ms)=104143980
		Physical memory (bytes) snapshot=583905005568
		Virtual memory (bytes) snapshot=2271860490240
		Total committed heap usage (bytes)=475657666560
============================================================================================

Good and bad examples:
	Bad examples: 
	- 200 דברים אמוריםז	-96.74225802364012
	- 200 באופן אבטומאטי	-96.67666292827141
	- 200 זכר וגקבה	-92.17840614600212
	- 199 לארכו ולרהבו	-91.55817464070425
	- 199 עזרא ונחמיח	-71.66121686301108
	- 199 שוא ומדוהים	-57.820633010310345
	- 199 הצהרת בלסור	-57.786481808346394
	- 199 הפרת וההידקל	-57.59948778644339
	- 182 רצועה בישא	-14.89898782236655
	
	Good examples:
	- 164 THOMAS FRANKLIN	-13.42225804495125
	- 164 COVERED BRIDGE	-13.42225804495125
	- 164 LADY JANE	-13.42225804495125
	- 163 conquer kingdomes	-11.40748491666895
	- 163 Library Union	-11.40748491666895
	- 163 Wonder Working	-11.40748491666895
	- 199 המלעיל והמלרע	-89.26969970582125
	- 199 השביעיות והשמיניות	-96.55407890487422
	- 199 הקרובים והרתוקים	-98.8734742009839
	- 199 בטלה ומבוטלה	-59.393140590478936

============================================================================================