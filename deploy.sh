#!/bin/bash

scp -o "StrictHostKeyChecking=no" install/build/Adempiere_390LTS.tar.gz $REMOTE_USER@$REMOTE_HOST:$APP_DIR && \
ssh -o "StrictHostKeyChecking=no" $REMOTE_USER@$REMOTE_HOST 'bash -s' < sh initService.sh