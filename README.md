# MB2 Voting
MB2 Voting is a plugin made for the [MB2 Log Reader](https://github.com/bully-mb2/mb2-log-reader). The goal of this plugin is to allow players to vote on a range of subjects such as map changes or custom polls

# Prerequisites
1. [JRE](https://java.com/en/download/manual.jsp) that can run Java 11 or higher
2. [MB2 Log Reader](https://github.com/bully-mb2/mb2-log-reader)

# Usage
User commands: triggered by /say <command>
```
!maplist <page>
    - Shows the maplist
!nominate <map>
    - Nominates a map for the next vote
!rtv
    - Vote to change the map
!unrtv
    - Revoke vote to change the map
!search <query>
    - Search for map in the maplist
!<number> 
    - Vote for a specific choice
```

Admin commands: triggered by /smod say <command>
```
!poll <option1>, <option2>, ...
    - Start a poll with the specified options
!forcertv
    - Force a vote to change the map
```

## Configuration
A maplist will be automatically generated upon first run. This map file includes the entire maplist available to MB2 servers by default. The maplist should contain 1 map per line, optionally with the max amount of rounds specified before it.
![image](https://user-images.githubusercontent.com/86576295/177452137-1290f524-06ee-4d88-91af-688ec111f4e2.png)

### mb2-log-reader
If you want to update player names constantly you can enable the ClientUserinfoChanged event in the log reader
```
parser.disable.clientuserinfochanged=false
```
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
