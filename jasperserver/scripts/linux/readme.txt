scripts/linux/readme.txt
------------------------


Add Jasper Server OS as a Linux Service
-------------------------------------------

The steps described in this section are an example of how to setup Jasper Server OS as
a service under Linux. The examples use Ubuntu and Redhat. 


Prerequisites
-------------

Install from the binary installer. 

The steps described are applicable to a Jasper Server OS instance that has been installed using
the binary installer (jasperreports-server-<ver>-linux-<cpu>-installer.run). 

It is assumed that the installation was carried out using the "bundled/bundled" options. This means
that the installer installed a bundled version of Tomcat and a bundled version of PostgreSQL. If this 
is not the case with your installed instance then the start|stop procedures described may not be 
correct. 


Setup Service Script
--------------------

An example jasperserver service script can be found in the following location:

  <js-install>/scripts/linux/jasperserver
  
This script will be added to the linux /etc/init.d location.   

First you must edit the jasperserver script:

  cd <js-install>/scripts/linux

  edit jasperserver
  
  Change:
  
    JASPER_HOME="/opt/jasperreports-server-4.7"  to your installation path
  
  Change:
  
    JASPER_USER=username  to the system user that Jasper Server OS should run under
  
  copy the script jasperserver to
  
    /etc/init.d
    
    You will need to be root or use sudo to do this. For example:
    
      sudo cp jasperserver /etc/init.d

  Set the jasperserver script to have execute permissions:
  
    sudo chmod 744  /etc/init.d/jasperserver
    
  Set the jasperserver script to have root owner and group
  
    sudo chown root /etc/init.d/jasperserver
    
    sudo chgrp root /etc/init.d/jasperserver

 
Start|Stop Jasperserver Service
-------------------------------

Now that the jasperserver script is in place you should be able to start and stop 
Jasper Server OS as a service.

Examples for Ubuntu. Try: 

  sudo service jasperserver status         (show jasperserver status for postgresql and tomcat)

  sudo service jasperserver start          (start jasperserver postgresql and tomcat)

  sudo service jasperserver stop           (stop jasperserver postgresql and tomcat)

  sudo service --status-all                (view status of all services)
  

Examples for Redhat. Try:

  sudo /sbin/service jasperserver status   (show jasperserver status for postgresql and tomcat)

  sudo /sbin/service jasperserver start    (start jasperserver postgresql and tomcat)
  
  sudo /sbin/service jasperserver start    (stop jasperserver postgresql and tomcat)
    
  sudo /sbin/chkconfig --list              (view status of all services)

    
Add Service to System Startup
-----------------------------

Under Ubuntu you can use the update-rc.d command to setup Jasper Server OS so that it will
startup on a system startup and stop on a system shutdown. Under Redhat the command would be
chkconfig. 

In order to run the commands below that will set jasperserver to start|stop it is assumed that
you have put the jasperserver script into the /etc/init.d folder. 


Example for Ubuntu. Try:

  sudo update-rc.d jasperserver defaults   (adds start and stop links from /etc/rcN.d folders)
                                           (for instance: /etc/rc2.d, /etc/rc3.d, etc)


Example for Redhat. Try: 


  sudo /sbin/chkconfig --add jasperserver  (adds the service)
  
  sudo /sbin/chkconfig jasperserver on     (add run levels)
  
  or
  
  sudo /sbin/chkconfig jasperserver on --levels 2,3,5  (add start links for run levels 2,3,5)
  
  
  sudo /sbin/chkconfig --list jasperserver (list service run levels)
  
  
Now, Jasper Server OS should shutdown gracefully on system shutdown and start
on system startup. 
  
  
Additional Information
----------------------
  
Also, see the man pages for update-rc.d and chkconfig for more details. 

 
Links:

  http://www.linuxjournal.com/article/4445

  https://help.ubuntu.com/community/UbuntuBootupHowto

  http://www.aboutlinux.info/2006/04/enabling-and-disabling-services-during_01.html

 
===
