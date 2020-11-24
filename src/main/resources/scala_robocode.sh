#!/bin/sh
#
# Copyright (c) 2001-2020 Mathew A. Nelson and Robocode contributors
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# https://robocode.sourceforge.io/license/epl-v10.html
#

export SCALA_LIB=$HOME/.m2/repository/org/scala-lang/scala-library/2.12.10/scala-library-2.12.10.jar
pwd=`pwd`
cd "${0%/*}"
java -DNOSECURITY=true -Xmx512M -cp libs/robocode.jar:libs/akka-robots-1.0-SNAPSHOT-allinone.jar:$SCALA_LIB -XX:+IgnoreUnrecognizedVMOptions "--add-opens=java.base/sun.net.www.protocol.jar=ALL-UNNAMED" "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED" "--add-opens=java.desktop/javax.swing.text=ALL-UNNAMED" "--add-opens=java.desktop/sun.awt=ALL-UNNAMED" robocode.Robocode $*
cd "${pwd}"
