#!/bin/sh

################################################################################
#  ADempiere start script                                                     ##
#  Starting ADempiere Server                                                  ##
#                                                                             ##
#  E.R.P. Conmsultores y Asociados, C.A                                       ##
#  Yamel Senih                                                                ##
#  ysenih@erpya.com                                                           ##
################################################################################


source /etc/profile

if [ -z "$ISSETUP" ]
then
    cd $ADEMPIERE_HOME
	sed -i "s|ADEMPIERE_DB_SERVER=localhost|ADEMPIERE_DB_SERVER=$ADEMPIERE_DB_HOST|g" AdempiereEnv.properties
	sed -i "s|ADEMPIERE_DB_PORT=5432|ADEMPIERE_DB_PORT=$ADEMPIERE_DB_PORT|g" AdempiereEnv.properties 
	sed -i "s|ADEMPIERE_DB_NAME=adempiere|ADEMPIERE_DB_NAME=$ADEMPIERE_DB_NAME|g" AdempiereEnv.properties
	sed -i "s|ADEMPIERE_DB_USER=adempiere|ADEMPIERE_DB_USER=$ADEMPIERE_DB_USER|g" AdempiereEnv.properties
	sed -i "s|ADEMPIERE_DB_PASSWORD=adempiere|ADEMPIERE_DB_PASSWORD=$ADEMPIERE_DB_PASSWORD|g" AdempiereEnv.properties
	sed -i "s|ADEMPIERE_DB_SYSTEM=postgres|ADEMPIERE_DB_SYSTEM=$ADEMPIERE_DB_ADMIN_PASSWORD|g" AdempiereEnv.properties
	sed -i "s|ADEMPIERE_WEB_ALIAS=localhost|ADEMPIERE_WEB_ALIAS=$(hostname)|g" AdempiereEnv.properties
	sed -i "s|ADEMPIERE_APPS_SERVER=localhost|ADEMPIERE_APPS_SERVER=$(hostname)|g" AdempiereEnv.properties
    sed -i "s|ADEMPIERE_WEB_PORT=8888|ADEMPIERE_WEB_PORT=$ADEMPIERE_WEB_PORT|g" AdempiereEnv.properties
    sed -i "s|ADEMPIERE_SSL_PORT=4444|ADEMPIERE_SSL_PORT=$ADEMPIERE_SSL_PORT|g" AdempiereEnv.properties
    sed -i "s|ADEMPIERE_APPS_TYPE=tomcat|ADEMPIERE_APPS_TYPE=$ADEMPIERE_SERVICE_TYPE|g" AdempiereEnv.properties
    sed -i "s|-Xms64M -Xmx512M|$ADEMPIERE_JAVA_OPTIONS|g" AdempiereEnv.properties
	sh RUN_silentsetup.sh
	
	echo "ISSETUP=Y" >> /etc/profile
	echo "export ISSETUP" >> /etc/profile 
fi

sh $ADEMPIERE_HOME/utils/RUN_Server2.sh && tail -f /dev/null
exit 0
