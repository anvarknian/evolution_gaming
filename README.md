## Solution !!
Use Google Chrome addons `'Simple Websocket Client'` to connect to `ws://localhost:9000/ws`

## Evolution Gaming candidate test assignment
Welcome to the scala test assignment.
We'd like to inform you that your approach will be timed - so please push the "proceed" button only when you are ready to spend the following 3 hours on the assignment.

When proceeding, you will be presented with a git repository where you would be prompted to push your solution. If you already have a bootstrap application structure which you would like to use, feel free to prepare it prior to proceeding with the test assignment.

After pushing the "proceed" button you will be presented with the task and a 3 hour countdown will start.

This is the repository to which you will be prompted to push your code: 
https://scala-assignment.evolutiongaming.com/git/anvarknian/diyuqoyu.git

`Set up your local directory
mkdir /path/to/your/project
cd /path/to/your/project
git init
git remote add origin https://scala-assignment.evolutiongaming.com/git/anvarknian/diyuqoyu.git
Add your boilerplate code, so that you're ready to start working with the test task
You will need to develop a server for a JSON-based protocol served over WebSockets.`

It is up to you to choose the technical stack, however we suggest that you use one which is relevant to the position you are interviewing for and with which you are proficient.

If in doubt, please choose Akka HTTP with Circe as a JSON serializer, and sbt as the build tool.

A client UI is not required, however, if you develop one using Scala.js it will be appreciated. It is, however, completely optional.

The server should be able to handle multiple concurrent clients.

The server should authenticate user "admin" with password "admin" successfully.

The WS server should be hosted on port 9000 and the URL /ws_api.

## Add your boilerplate code to your project repository
`git add .
git commit -m 'Initial commit in the Evolution Gaming test assignment'
git push -u origin master`