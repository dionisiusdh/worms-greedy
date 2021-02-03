JVM = java
JC = javac
JFLAGS = -g -d $(BIN)
#RUNFLAGS = -cp
RUNFLAGS = -jar
JAR = jar
JARFLAGS = -cvfe

SRC = src
BIN = bin

CLASSES = Matriks.java \
		  Main.java \
		  Test.java

JARFILE = tubes.jar
DRIVER = Main
TEST = Test

# Proses:
# 1. Compile file .class
# 2. Masukin semuanya ke jar file
# 3. Execute jar file-nya
# 	a. Run driver
# 	b. Run test

# Default action
default: run clean

# Proses pertama
compile: $(addprefix $(SRC)/, $(CLASSES))
	$(JC) $(JFLAGS) $(addprefix $(SRC)/, $(CLASSES))

# Proses kedua
jar: compile $(addprefix $(BIN)/, $(CLASSES:.java=.class))
	$(JAR) $(JARFLAGS) $(addprefix $(BIN)/, $(JARFILE)) $(DRIVER) $(addprefix -C $(BIN)/ , $(CLASSES:.java=.class))

jarTest: compile $(addprefix $(BIN)/, $(CLASSES:.java=.class))
	$(JAR) $(JARFLAGS) $(addprefix $(BIN)/, test.jar) $(TEST) $(addprefix -C $(BIN)/ , $(CLASSES:.java=.class))

# Prosees ketiga(a)
run: jar $(addprefix $(BIN)/, $(JARFILE))
	$(JVM) $(RUNFLAGS) $(addprefix $(BIN)/, $(JARFILE))

# Proses ketiga(b)
test: jarTest $(addprefix $(BIN)/, test.jar)
	$(JVM) $(RUNFLAGS) $(addprefix $(BIN)/, test.jar)

# Bersih-bersih
clean:
	$(RM) $(BIN)/*.class $(BIN)/*.jar
