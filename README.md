# ProveSoft

---
## Overview
---
ProveSoft is developed using Java Spring 4.1 framework, in conjunction with AngularJS as a frontend framework. The backend is currently provisioned to use the MySQL database, but connectors can easily be created to accomodate other SQL variants.

The project is segregated into 3 separate applications (Gateway, Ui, Resource) which all communicate with each other. The Gateway facilitates user authentication, and is always a point of entry whenever a user requests anything from the Ui or Resource segment.
The Ui component houses all the frontend browser rendering components, and is what the user sees and interacts with.
The Resource component is what speaks with the database, and does all the backend computation. The Ui component passes and retrieves data from the Resource component using JSON payloads.

---
## Production Application Setup
---
#### User Setup
Create a new VPS running the latest LTS version of Ubuntu.
SSH into the new server as ```root``` and create an account specifically for running the application. 
Add the new user to the sudoers group.
Create home directory and change ownership to new user
Logout of ```root``` and ssh back in as the new user.
```
  adduser provesoft
  adduser provesoft provesoft
  adduser provesoft sudo
```

#### Preliminary Installation
Install MySQL Server, and set the root password when prompted.
```
  sudo apt-get update
  sudo apt-get install mysql-server
```
Activate MySQL and finish up with secure install. Remove anonymous users, disallow remote root login, remove test database, and reload privileges table.
```
  sudo mysql_install_db
  sudo /usr/bin/mysql_secure_installation
```
Login as root and create a blank provesoft database. Follow up by creating two database with two users, one of which will be used for the Gateway, and the other for Resource.
```
  CREATE DATABASE provesoftauth CHARCTER SET utf8 COLLATE utf8_unicode_ci;
  CREATE DATABASE provesoft CHARACTER SET utf8 COLLATE utf8_unicode_ci;
  CREATE USER 'psgateway'@'localhost' IDENTIFIED BY 'P$G@t3w@y';            (Used by the Gateway for authentication)
  CREATE USER 'provesoft'@'localhost' IDENTIFIED BY 'Pr0v3$0ft';            (Used by the Resource component)
  CREATE USER 'provesoft'@'%' IDENTIFIED BY 'Pr0v3$0ft';            	    (Used for remote access)
```
To segregate the user authentication and application components, the two users will have separate access rights to the databases. Provesoftauth will retain access to the provesoftauth database for authenticating users in Gateway, and provesoft will only access the provesoft database for the business login in Resource.
In order to allow read only external access, grant select permissions on the same tables as provesoft@localhost to provesoft@%
```
  GRANT ALL PRIVILEGES ON provesoftauth.* TO 'psgateway'@'localhost';
  GRANT ALL PRIVILEGES ON provesoft.* TO 'provesoft'@'localhost';
  GRANT SELECT ON provesoft.* TO 'provesoft'@'%';
  FLUSH PRIVILEGES;
```

If remote access to the MySQL database is needed, the bind address config parameter needs to be changed followed by a restart of the database server.
```
  sudo vi /etc/mysql/my.cnf
  Comment the line starting with "bind-address" and save.
  sudo service mysql restart
```
Install Java 8 JDK. Ubuntu 14.04 does not ship with the Java 8 repository, so it needs to manually added.
```
  sudo add-apt-repository ppa:openjdk-r/ppa
  sudo apt-get update
  sudo apt-get install openjdk-8-jdk
```
Install Supervisor to manage the Web Application. This will ensure that the app restarts should the server reboot, or the app goes down.
```
  sudo apt-get install supervisor
```
Finally, additional entropy needs to be added to the server to allow the web application's methods relying on randomness to function correctly.
```
  sudo apt-get install haveged
```
Configure the Datasource Beans in the Gateway and Resource applications so that Gateway uses "provesoftauth" id and Resource uses "provesoft" id.

#### Install Redis (Needed for gateway authentication)
Get the latest version of Redis and extract it in the home directory
```
  wget http://download.redis.io/redis-stable.tar.gz
  tar xvzf redis-stable.tar.gz
  cd redis-stable
  make
  make install
```
Setup Redis to run as a Daemon auto-restart process.
```
  sudo mkdir /etc/redis
  sudo mkdir /var/redis
  
  sudo cp utils/redis_init_script /etc/init.d/redis_6379
  sudo cp redis.conf /etc/redis/6379.conf
  sudo mkdir /var/redis/6379
```
Edit the configuration file /etc/redis/6379.conf:
```
  Set daemonize to yes
  Set pidfile to /var/run/redis_6379.pid
  Uncomment bind 127.0.0.1		(Only allow localhost access)
  Set the logfile to /var/log/redis_6379.log
  Set the dir to /var/redis/6379
```
Add new Redis init script to all default run levels
```
  sudo update-rc.d redis_6379 defaults
```
Reboot server and test if Redis server works by pinging the server.
```
  redis-cli
  ping
```

#### Finalize and Setup Folder Structure
Create a directory to house the application and claim ownership.
```
	sudo mkdir /www
	sudo chown provesoft:provesoft -R /www
	sudo chmod -R 770 /www
```
Create another subdirectory to hold the apps
```
	mkdir /www/provesoft && chmod -R 770 /www/provesoft
```
Finally create a log directory.
```
	mkdir /www/provesoft/logs && chmod -R 770 /www/provesoft/logs
```
Move the compiled jars for each of the applications into the folder.

Create a Supervisor configuration file for each application.
```
	sudo vi /etc/supervisor/conf.d/provesoft_gateway.conf
	sudo vi /etc/supervisor/conf.d/provesoft_ui.conf
	sudo vi /etc/supervisor/conf.d/provesoft_resource.conf
```
Add the following configuration (using Gateway as an example):
```
	[program:gateway]
	command=/usr/bin/java -jar /www/aerosource/Provesoft_Gateway.jar
	user=provesoft
	autostart=true
	autorestart=true
	startsecs=10
	startretries=3
	stdout_logfile=/www/provesoft/logs/gateway-stdout.log
	stderr_logfile=/www/provesoft/logs/gateway-stderr.log
```
