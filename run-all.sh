#!/bin/bash

# Build, unit test, create jars
./gradlew clean
./gradlew build
./gradlew createJarGeneBankCreateBTree
./gradlew createJarGeneBankSearchBTree
./gradlew createJarGeneBankSearchDatabase

# test0.gbk
./create-btrees.sh test0.gbk
./check-dumpfiles.sh test0.gbk
sleep 1
./check-queries.sh test0.gbk
sleep 1
./check-db.sh test0.gbk
sleep 1

# test5.gbk
./create-btrees.sh test5.gbk
./check-dumpfiles.sh test5.gbk
sleep 1
./check-queries.sh test5.gbk
sleep 1
./check-db.sh test5.gbk
#sleep 1

# hs_ref_chrY.gbk
#./create-btrees.sh hs_ref_chrY.gbk
#./check-dumpfiles.sh hs_ref_chrY.gbk
#./check-queries.sh hs_ref_chrY.gbk
#./check-db.sh hs_ref_chrY.gbk