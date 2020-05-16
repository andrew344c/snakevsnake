# **Snake V Snake**
### APCS Final Project

A twist on the classic singleplayer snake game, a multiplayer snake game where players compete against each other in various gamemodes.

###Project Structure
The Game components package contain essential to the game itself. The GUI package contains the frontend side of the game.
The Networking package contains essential materials for clients towards connecting to a server (ClientService) and also 
server software for maintaining a server and managing connecting players (ServerService, ClientHandler). The precompiled 
classes in out are of major version 52.

To run the project, since there are no extra dependencies and only core java libraries, simply run 
```
java Path-To-out/production/snakevsnake/GUI.MainWindow
```
(Currently code is messed up from testing multi-player, right now it would be):
```$xslt
java Path-To-out/production/snakevsnake/GUI.MainWindow "ip-of-server" "port"
#Make sure to have a server running
```




