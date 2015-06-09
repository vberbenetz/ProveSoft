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
Login as root and create a blank provesoft database. Follow up by creating two users, one of which will be used for the Gateway, and the other for Resource.
```
  CREATE DATABASE provesoft CHARCTER SET utf8 COLLATE utf8_unicode_ci;
  CREATE USER 'provesoftauth'@'localhost' IDENTIFIED BY 'Pr0v3$0ftAuth';    (Used by the Gateway for authentication)
  CREATE USER 'provesoft'@'%' IDENTIFIED BY 'Pr0v3$0ft';                    (Used by the Resource component)
```
To segregate the user authentication and application components, the two users will have separate access rights to the database. Provesoft will need to retain read access to Authorities to determine the current user's role when returning Resource data. In order to allow read only external access, grant select permissions on the same tables as provesoft@localhost to provesoft@%
Using this method, every subsequent table will need to manually have permissions added to the provesoft user.
```
  GRANT ALL PRIVILEGES ON provesoft.Users TO 'provesoftauth'@'localhost';
  GRANT ALL PRIVILEGES ON provesoft.Authorities TO 'provesoftauth'@'localhost';
  GRANT SELECT PRIVILEGES ON provesoft.Authorities TO 'provesoft'@'localhost';
  .
  .
  GRANT SELECT ON provesoft.<TABLE_NAME_1> TO 'provesoft'@'%';
  .
  .
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
