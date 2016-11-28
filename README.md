About
-----
In this experiment, the key objective is to **infer the user location based on the user contents** i.e. _infer Twitter user geolocation based on in their tweets_. This experiment involves two  key tasks.

1. Feature Engineering
2. Machine Learning - to predict future unseen data

The **tweetlocml** application attempt to do mainly on engineering features (or attributes) that can be used to build a classifier model for model-driven approach with Machine Learning algorithms to achieve the project objective. The project mainly focus on _Supervised_ Machine Learning model classifier.


Input Data Assumptions
----------------------
There are **4 input data** that need to be available in the following format.

### Gazetteer Dictionary

1. Download [Free Gazetteer data from GeoNames](http://download.geonames.org/export/dump/) e.g. `US.zip`
2. Process it to extract the column `asciiname` field data only. e.g. `US.txt`
3. Optionally, sort the location names and remove duplicates.
4. Save this gazetteer data in the plain text `.txt` format and specify it in [`config.properties`](config.properties), `gazetteer=path/to/file.txt`

### Tweet Data

1. Harvest Twitter user public tweets data e.g. using [twitter4j](http://twitter4j.org/en/) with [Twitter API](https://dev.twitter.com/docs)
2. And process it in the following format:
        
        user_id (tab) tweet_id (tab) tweet_text (tab) location_code  (newline)
   
3. Save it as plain text `.txt` format
4. Use hold-out strategy, split the tweets corpus to 60/20/20 for Training/Development/Test respectively. 
5. And specify them in [`config.properties`](config.properties), `tweets.train, tweets.dev, tweets.test`

`location_code` is 2-letter format of US city names e.g. SD for San Diego. This can be pre-determined from a Twitter user profile or location Long/Lat information through Twitter API.  For the test dataset, do label the `location_code` as question-mark `?` instead -- i.e. in order to evaluate as an unseen data.

### OpenNLP

1. Download the OpenNLP model data from http://opennlp.sourceforge.net/README.html#models
2. Specify `opennlp.model.pos.en` field in `config.properties`

### Stop words
1. Download from http://ir.dcs.gla.ac.uk/resources/linguistic_utils/stop_words
2. Save it as `stop_words.txt`

Building
--------
**tweetlocml** uses a couple of 3rd party libraries. Please refer [tweetlocml/pom.xml](pom.xml) for details.

**tweetlocml** can build with maven.

    cd tweetlocml
    mvn clean
    mvn test
    mvn package

The build artifacts can find under `tweetlocml/target` folder.


Deployment
----------
The automated deployment is written for Ant [`build.xml`](build.xml) script. You will need to have [Apache Ant in your path](http://ant.apache.org/). Otherwise you can read build script and manually manipulate the deployment structure.

    cd tweetlocml
    ant clean
    ant deploy
    

Quick Start by using batch scripts
----------------------------------

1. Generate classifier
    
    * Generate Part-of-speech Noun classifier 

            gen-classifier-pos.bat

    * Generate Part-of-speech Noun + English Stemming classifier

            gen-classifier-stem.bat 

    * Generate Part-of-speech Noun + Place (Approximate matching with US gazetteer) classifier

            gen-classifier-place.bat

2. Perform analysis on the generated engineered features. (Somewhat empirical analysis)

3. Select the attributes and save into `attributes.txt` file (configurable in `config.properties`).

4. Generate Weka arff file with frequency count numeric feature vector for tweets:

    * Training data:
    
            gen-arff-train.bat

    * Dev data:

            gen-arff-dev.bat

    * Test data:

            gen-arff-test.bat

5. Perform Machine Learning in Weka.

6. Repeat until (at your) best representation classifier found for the problem.


Configuration
-------------
1.  Open `config.properties` and configure all the paths.
2.  Adjust other parameters. Default values are a good starting point.


Running First Time
------------------
If config is not under the same root as where `tweetlocml.jar` is, then pass `-c` option.

    java -jar tweetlocml.jar -c /path/to/config.properties  [... and other options]

Preprocess Gazetteer (Run Once)
    
    java -jar tweetlocml.jar -d 1 --preprocess gaze

Partition Tweets (Optional, if Tweets corpus is big)

    java -jar tweetlocml.jar --preprocess parted


Dry Run
-------
To dry run first few tweets with pos - Part-of-speech classifier

    java -jar tweetlocml.jar -d 3


Classifiers
----------
    java -jar tweetlocml.jar -a pos -d 2

* pos = Part-of-speech classifier (default if no `-a` is pass)
* stem = Part-of-speech +Stemmming classifier
* place = Part-of-speech  +Place classifier - might take time to run


Output file 
-----------
Use `-o` to specify custom output file. Otherwise default to `output_[CITY].csv` format.

    java -jar tweetlocml.jar -a place -o output_place.csv -d 5
    java -jar tweetlocml.jar -a place -o directory/output_place.csv -d 5


Start index at 123 and run 10 more lines
-------------------
    java -jar tweetlocml.jar -a stem -o output_stem.csv -i 123 -d 10


Generate Weka arff from the selected attributes after analysis
---------------------
Configure attributes file location in `config.properties`. The default is:  `attributes=attributes.txt`

Generate for training set:

    java -jar tweetlocml.jar --arff train

Generate for development set:

    java -jar tweetlocml.jar --arff dev

Generate for test set:

    java -jar tweetlocml.jar --arff test

It will generate frequency numeric attributes and the respective vectors for each tweet raw data.


Binding the parted classifier output files
--------------------------

Running the place classifier will take long time due to each term approximate matching to location dictionary.
The approach is to make partition for tweets dataset. The default is 5% each. This can change at `config.properties`.
Each parted files can run concurrently on multiple servers. At the end, these parted files need to bind
back to create the final master file. In this case, unique user count information is lost due to partition.
To bind the parted files, run with `--mastering` option with point to the first partition directory.
Program will auto discover the rest of partitions and bind to final master file for each city.

    java -jar tweetlocml.jar --mastering ./place/parted/train00


Running As a Job
----------------
It is good idea to run with [`screen`](https://www.google.com.au/search?q=linux%20screen) on Linux.
    
    screen
    java -jar tweetlocml.jar -a pos &
    [ctrl + a, d]
    tail -f app.log
    [ctrl + c]


Notes
-----
This assignment work is done for COMP90049 Project 2 assessment 2016 SM2, The University of Melbourne. You can read [the report](report/SanKhoLin_829463_COMP90049_Project2_Report.pdf) on background context, though it discusses more on the data that I have worked with. You may also want to read the related [`tweetloc`](https://github.com/victorskl/tweetloc) assignment. The implementation still has room for improvement. You may cite this work as follow.

LaTeX/BibTeX:

    @misc{sanl1,
        author    = {Lin, San Kho},
        title     = {tweetlocml - Geolocation of Tweets with Machine Learning},
        year      = {2016},
        url       = {https://github.com/victorskl/tweetlocml},
        urldate   = {yyyy-mm-dd}
    }

Further Improvement Pointers:

1. **tweetlocml** can better streamline with Weka library i.e. bypass Weka GUI and call/use it through programming API.
2. **tweetlocml** can also streamline with `twitter4j` for acquiring tweets.

This could achieve high performance distributed application that run on BigData middleware and frameworks.