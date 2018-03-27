#!/bin/sh

scp install/build/Adempiere_390LTS.tar.gz $REMOTE_USER@$REMOTE_HOST:$APP_DIR && \
ssh $REMOTE_USER@$REMOTE_HOST 'bash -s' < sh initService.sh