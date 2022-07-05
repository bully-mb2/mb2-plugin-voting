# MB2 Voting
MB2 Voting is a plugin made for the [MB2 Log Reader](https://github.com/bully-mb2/mb2-log-reader). The goal of this plugin is to allow players to vote on a range of subjects such as map changes or custom polls

# Prerequisites
1. [JRE](https://java.com/en/download/manual.jsp) that can run Java 11 or higher
2. [MB2 Log Reader](https://github.com/bully-mb2/mb2-log-reader)

# Running
```
java -jar mb2-plugin-voting-VERSION.jar
```
After your first run a settings file will be generated next to the jar. Fill your credentials there and run again.

# Developing
To start developing generate your sources by running 
```
./mvn jaxb2:generate
```
Run this command every time the schema updates


## License
MB2 Voting is licensed under GPLv2 as free software. You are free to use, modify and redistribute MB2 Voting following the terms in LICENSE.txt