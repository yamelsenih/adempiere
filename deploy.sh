#!/bin/bash

scp -o "StrictHostKeyChecking=no" /home/travis/build/erpcya/adempiere/install/build/Adempiere_390LTS.tar.gz $REMOTE_USER@$REMOTE_HOST:$APP_DIR 