This program is used to simulate the operation between routers.
The main() funciton will read in infile.txt to initialize router objects and the cost between each accessible routers.
-------sample content of infile.dat-------------------------
0 155.246.80
 1 4
 3
1 155.246.81
 0
2 155.246.82
 1
 3
3 155.246.83
 2
 -----------------------------------------------------------
To initialize a router. You have to assign a number as "id" of the router and followed by its "ip".
And followed by its connection to other routers using "id" and followed by the "cost" of the connection(a line for a connection).
The default cost of a connection is 1, if you aren't specified.
In the above case, the cost from router0 to router1 is 4.

We set the connection between each router to be one way, of course, you can modify the content in infile.dat to force mutual-connection.
 
Feb, 2017
Po-Hsun Chen
