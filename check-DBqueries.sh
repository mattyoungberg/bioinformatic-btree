#!/bin/sh

for i in 1 2 3 4 5 6 7 8 9 10 20 31
do
	echo
	echo "Running queryfile " query$i "on test0.$i.db"
	echo
	time java -jar build/libs/GeneBankSearchDatabase.jar --database=test0.$i.db --queryfile=data/queries/query$i > data/queries/query$i-test0.$i.db.out
done
echo

for i in 1 2 3 4 5 6 7 8 9 10 20 31
do
	diff -w data/queries/query$i-test0.$i.db.out results/query-results/query$i-test0.gbk.out
	if test "$?" = "0"
	then
		echo "----> Query-Test-$i PASSED!"
	else
		echo "----> Query-Test-$i FAILED@$#!"
	fi

done
echo

for i in 1 2 3 4 5 6 7 8 9 10 20 31
do
	echo
	echo "Running queryfile " query$i "on test5.$i.db"
	echo
	time java -jar build/libs/GeneBankSearchDatabase.jar --database=test5.$i.db --queryfile=data/queries/query$i > data/queries/query$i-test5.$i.db.out
done
echo

for i in 1 2 3 4 5 6 7 8 9 10 20 31
do
	diff -w data/queries/query$i-test5.$i.db.out results/query-results/query$i-test5.gbk.out
	if test "$?" = "0"
	then
		echo "----> Query-Test-$i PASSED!"
	else
		echo "----> Query-Test-$i FAILED@$#!"
	fi

done
echo