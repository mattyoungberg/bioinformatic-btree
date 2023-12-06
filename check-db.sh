#!/bin/sh

case $# in
0) echo "Usage: " `basename $0` " <datafile name (from the data/files_gbk folder)> "; exit 1;;
esac

datafile=$1
basename="${datafile%.gbk}"
for i in 1 2 3 4 5 6 7 8 9 10 20 31
do
	echo
	echo "Running queryfile " query$i "on $datafile.$i.db"
	echo
	time java -jar build/libs/GeneBankSearchDatabase.jar --database=$basename.$i.db --queryfile=data/queries/query$i > data/queries/query$i-$basename.$i.db.out
done
echo

for i in 1 2 3 4 5 6 7 8 9 10 20 31
do
	diff -w data/queries/query$i-$basename.$i.db.out results/query-results/query$i-$datafile.out
	if test "$?" = "0"
	then
		echo "----> Query-Test-$i PASSED!"
	else
		echo "----> Query-Test-$i FAILED@$#!"
		exit 1
	fi

done
echo
