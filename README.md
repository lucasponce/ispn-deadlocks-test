ISPN deadlock tests
===================

Finally I attached a Test Class with ISPN and JGroups config, that shows you how you can get TimeoutExceptions and deadlocks quite easily 
when you have jgroups thread pools not properly adjusted. You can simply launch 2 JVM, with as parameter "node1 20" (the 1st parameter is 
the id of the node and the 2nd is the total amount of threads) for the first JVM and "node2 20" for the second JVM, you should get some deadlocks 
knowing that thread_pool.max_threads and oob_thread_pool.max_threads have been set to 20. You will get even more deadlocks if you test 
with 25 threads for each node and you won't get any deadlocks with 15 threads.

Update:

- Increased # jgroups thread pools to 400
- Test can be run with ./run node1 400 and ./run node2 400