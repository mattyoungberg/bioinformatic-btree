# Team #9
***

## Team Members

Last Name       | First Name      | GitHub User Name
--------------- | --------------- | --------------------
Caplinger       | Derek           | drc3141
Youngberg       | Matt            | mattyoungberg

## Test Results
**Q: How many of the dumpfiles matched (using the check-dumpfiles.sh script)?**  
A: All the dumpfiles matched 👍

**Q: How many of the query files results matched (using the check-queries.sh script)?**  
A: All the query files matched 👍

## Cache Performance Results
- No SQLite database created
- Ran on a Macbook with an M1 standard chip and 8GB of memory
- [hyperfine](https://github.com/sharkdp/hyperfine) used to measure average runtimes

| gbk file | degree | sequence length | cache | cache size | cache hit rate | run time |
| -------- | ------ | --------------- | ----- | ---------- | -------------- | -------- |
| test5.gbk|  102   |     20          |  no   |    0       |      0%        |  22.82s  |
| test5.gbk|  102   |     20          |  yes  |    100     |      66.49%    |  16.65s  |
| test5.gbk|  102   |     20          |  yes  |    500     |      77.90%    |  11.21s  |
| test5.gbk|  102   |     20          |  yes  |    1000    |      81.51%    |  10.54s  |
| test5.gbk|  102   |     20          |  yes  |    5000    |      95.17%    |   4.50s  |
| test5.gbk|  102   |     20          |  yes  |    10000   |      99.67%    |   2.18s  |



## AWS Notes

Deploying a default EC2 micro instance via the AWS console was straightforward. Initially, we faced difficulties
locating the instance online when it wasn't publicly exposed, which might have required using Amazon's custom DNS.
However, we instead opted to expose the instance to the public internet. We found the box immediately. Our familiarity
with SSH and key management quickly got us into the box and the repo pulled from GitHub.

After that, testing proceeded smoothly. We noticed that the `test0.gbk` and `test5.gbk` files ran slower compared to our
personal machines with more computing power, which was to be expected. Intrigued, though, we decided to run a test on
the `hs_ref_chrY.gbk` file. For this, we used `tmux` to detach the process, allowing us to disconnect and revisit it a
few hours later. Upon our return, it was evident that the process had stalled. A check on the system's resources
revealed that CPU, memory, and disk usage were within normal limits. Our hypothesis is that the JVM's allocated virtual
memory was exhausted, leading to the process hang-up, as indicated by a static, round memory usage figure. Concluding
that allocating additional virtual resources to the JVM was beyond our project's scope, and that 1GB of system memory
was not much to begin with, we decided not to pursue testing of that file further.

## Reflections

### Reflection: Derek Caplinger

TBD

### Reflection: Matt Youngberg

This project has been the most intensive experience I've had in a class at Boise State, presenting several enjoyable
challenges.

One such challenge was implementing in-order traversal of the BTree, which I addressed through an Iterator
implementation. This approach provided an interface familiar to my teammates, and that in itself facilitated easy
parallel integration. The essence of an Iterator is to traverse a collection, and its use in this context was aptly
fitting. A significant advantage of using well-known interfaces is the reduced integration time due to established
familiarity. I’m glad that I could effectively utilize this aspect in our project. Adhering to the interface may have
made the code more verbose, as an Iterator almost always has to maintain state between invocations of next() if it wants
to be asymptotically efficient, but the benefits of using an established interface outweighed the verbosity. I used the
same approach with the GeneBank files as well, treating the files as a type of subsequence collection once the desired
subsequence length is known.

Another fun aspect of this project was all the facets that were available for optimization. How you define your nodes
on disk, how you manage your buffers, how you manage your cache, how you manage reading subsequences from disk, etc. It
seemed that everywhere you looked, there was an opportunity to optimize the code's runtime and memory footprint. And you
can bet that I took every opportunity I found to do so. I like to think it paid off, as our code performed very
similarly to the professors' solution, with and without a cache. It also looks as if we found similarly efficient
disk-encoding schemes, as their performance table suggested they could produce a BTree of with a minimum degree of 102
when given block sizes of 4096B, and ours could do the same.

Admittedly, there were moments of frustration while working on this project, particularly when it felt like we were
piecing together an already-complete project that had been abruptly "dropped out" from beneath us. This "fill in the
blanks" approach was frustrating in a few different scenarios:

- Some existing tests required us to introduce methods within our BTree solely for white box testing, and these methods
were not part of the initially provided interface. I felt that the black-box tests in conjunction with the script
integration tests were adequate to ensure system correctness, and so the given white-box tests felt unnecessary and 
forced me to add further methods and logic to the BTree that I would not have otherwise added.
- In our project, as guided by the provided tests, the constructors were tasked not only with in-memory entity creation
but also with comprehending their format on disk and dealing with different file states. From my experience, it's more
effective to separate in-memory entity creation (aka, constructors) from its reconstitution from a persistent store. The
in-memory construction process should be independently viable and accommodate varying states. Ideally, the understanding
of the disk format would be encapsulated within a static factory method. This method would handle the loading of
necessary data and subsequently use a constructor to build the entity in memory. Utilizing Java's capability for
constructor overloading is beneficial in this context: It allows the definition of a flexible, non-public constructor
that can accommodate various states of an entity, while public overloaded constructors can present simpler interfaces
focused on efficiently creating new entities. But the constructors as defined by the tests forced our hand.
- Exposing TreeObjects through the BTree interface seemed to compromise user-facing abstraction and simplicity,
principles I personally find crucial in design. In one sense, it made sense because frequencies were needed and
combining the subsequence and the frequency together necessitates some object if it's going to come from the BTree, but
in another, it also meant the client now had to assume more cognitive load to comprehend the custom object (whereas the
concept of a BTree in isolation is generally known and its methodset predictable). What seems a normal design to me is
for the onus to be on the client (for example, the command line programs) to check for duplicates before inserting into
a collection, and maintain a separate frequency count if needed for the sake of their application.

These experiences underscored a unique challenge in academic software projects: balancing the fulfillment of application 
requirements with internal coherence, particularly when developing a codebase from scratch that inherits complex design
choices from an existing solution.

Throughout our project, I often contemplated a question posed by Professor Amit regarding the feasibility of creating a
generic BTree. Reflecting on BTrees' common usage in relational databases, I recognized that they are optimized for
predefined data types with known disk sizes, a critical factor for efficient storage and retrieval. This optimization is
a key reason for the widespread use of BTree indexes in relational databases.

In contrast, object databases rarely employ BTrees due to the variable sizes of objects, which are not as easily
predictable. In the Java context, developing a generic BTree class poses the complex challenge of defining an abstract
data type that, when serialized to disk, maintains a consistent and pre-determined size. My experience suggests that
implementing such a system in Java would require extensions to accommodate all anticipated data types. Furthermore, any
addition of new types would necessitate further development work. Therefore, much like a relational database, a Java
application would need to limit its support to a predefined set of types, as accommodating additional types would
require significant development effort.

Ultimately, I enjoyed this project immensely. It had its fair share of frustrations, an exciting amount of big
challenges, and a great opportunity for growth as a computer scientist. I'm glad to have had the opportunity to work
with my teammate, and I'm proud of the work we accomplished.
