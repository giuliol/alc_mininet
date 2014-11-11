#!/bin/bash

git add .
git commit -m '$1'
echo "commit done, pushing.. $1"
git push
