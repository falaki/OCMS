# External libraries
EXTERNAL_LIBS := lingpipe-3.4.0.jar

# Java executables and options
JAVA := java
JAVAC := javac
JAR := jar
JAVADOC := javadoc

DOCS := documents/docs
RM_FLAGS := -rf

JAVA_FLAGS = -Xms1000M -Xmx1000M -cp .$(addprefix :, $(EXTERNAL_LIBS))  

JAVADOC_FLAGS =  -d $(DOCS) -author -windowtitle OCMS -header "Opportunistic Connectivity Management Simulator" 
JAVAC_FLAGS = -Xlint:unchecked  -deprecation -cp .$(addprefix :, $(EXTERNAL_LIBS))

# Jars to create
OCMS_JAR := ocms.jar


# Start of source files
OCMS_SRC := \
    ocms/dataset/Experiment.java \
    ocms/dataset/DataSet.java \
    ocms/dataset/DataCluster.java \
    ocms/dataset/Tuple.java \
    ocms/dataset/Sample.java \
    ocms/util/Log.java \
    ocms/util/Block.java \
    ocms/util/LogMessage.java \
    ocms/util/Logger.java \
    ocms/util/Configuration.java \
    ocms/util/Profile.java \
    ocms/util/ProfileException.java \
    ocms/medium/Medium.java \
    ocms/medium/WiFiMedium.java \
    ocms/medium/GSMMedium.java \
    ocms/medium/MediumException.java \
    ocms/nic/NIC.java \
    ocms/nic/WiFiProfile.java \
    ocms/nic/WiFiNIC.java \
    ocms/nic/GSMNIC.java \
    ocms/nic/WiFiNICWrapper.java \
    ocms/nic/GSMNICWrapper.java \
    ocms/nic/NICException.java \
    ocms/user/User.java \
    ocms/user/UIDataSet.java \
    ocms/schedulers/SchedulerException.java \
    ocms/schedulers/Scheduler.java \
    ocms/schedulers/SchedulerWrapper.java \
    ocms/schedulers/hypothetical/OptimalScheduler.java \
    ocms/schedulers/hypothetical/StepOptimalScheduler.java \
    ocms/schedulers/hypothetical/DumbScheduler.java \
    ocms/schedulers/heuristic/EBScheduler.java \
    ocms/schedulers/heuristic/LBScheduler.java \
    ocms/schedulers/heuristic/UserStaticScheduler.java \
    ocms/schedulers/caching/GSMCachingScheduler.java \
    ocms/simulator/Simulator.java \
    ocms/eventqueue/Event.java \
    ocms/eventqueue/EventConsumer.java \
    ocms/eventqueue/EventQueue.java \
    ocms/eventqueue/EventQueueException.java \
    ocms/algorithms/statistics/Histogram.java  \
    ocms/algorithms/statistics/Ranking.java  \
    ocms/algorithms/statistics/CDF.java  \
    ocms/algorithms/statistics/Frequency.java  \
    ocms/algorithms/statistics/Counter.java 




# End of source files

OCMS_OBJS := $(OCMS_SRC:.java=.class)

# All objects being compiled
OBJS := $(OCMS_OBJS) 
JARS := $(OCMS_JAR) 

.DEFAULT: all
.PHONY: all clean
.SUFFIXES: .java

all: $(JARS)

run: all
#	@$(JAVA) $(JAVA_FLAGS) ocms.algorithms.Test
	@$(JAVA) $(JAVA_FLAGS) ocms.simulator.Simulator ocms.conf

docs: all
	$(JAVADOC) $(JAVADOC_FLAGS) $(OCMS_SRC)  
	echo "Press Enter to upload the docs and Ctrl+C to stop"; read; scp -r documents/docs mhfalaki@cs:~/public_html/ocms/

	
clean:
	$(RM) $(JARS)
	$(RM) $(OBJS)
#	$(RM) $(RM_FLAGS) $(DOCS)

$(OCMS_JAR) : $(OCMS_OBJS)
	$(JAR) cvf $@ $(OBJS)

%.class: %.java
	$(JAVAC) $(JAVAC_FLAGS) $< 



