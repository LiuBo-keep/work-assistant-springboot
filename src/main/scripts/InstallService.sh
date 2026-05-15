#!/bin/bash
chown -R root:root /opt/@dist.project.name@
chmod 500 /opt/@dist.project.name@/@project.build.finalName@-runner.jar
echo Copy @dist.project.name@.service ...
cp /opt/@dist.project.name@/scripts/@dist.project.name@.service /usr/lib/systemd/system/
systemctl daemon-reload
echo Enable @dist.project.name@ ...
systemctl enable @dist.project.name@.service
echo Start @dist.project.name@ ...
systemctl start @dist.project.name@.service