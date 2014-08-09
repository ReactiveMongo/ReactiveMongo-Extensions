#!/bin/bash

echo -e "Starting publish to Sonatype...\n"

sbt ";project bson;publishSnapshotsFromTravis ;project core;publishSnapshotsFromTravis ;project json;publishFromTravis"
RETVAL=$?

if [ $RETVAL -eq 0 ]; then
  echo "Snapshots successfully published"
else
  echo "Error while publishing snapshots"
  exit 1
fi
