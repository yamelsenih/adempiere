#!/bin/bash
cd /home/travis/build/erpcya/adempiere/install/build

  git init
  git remote add deploy $REMOTE_USER@$REMOTE_HOST:$APP_DIR 
  git config user.name $DEPLOY_USER
  git config user.email $DEPLOY_EMAIL
  git add Adempiere_390LTS.tar.gz
  git commit -m "Deploy from Travis - build {$TRAVIS_BUILD_NUMBER}"
 

  echo "Sends build"
  git push -f deploy master
