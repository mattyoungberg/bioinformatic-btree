# Project Runtime Table

All times were taken on a 2020 MacBook Pro with a standard M1 chip and 8GB of memory. Hyperfine was 
used to do back-to-back runs of the program (including 3 warmup runs each) to provide statistics
about the runtime, such as average, standard deviation, and min/max. The run time in this table
represents an average of all the runs done by hyperfine.

| gbk file | degree | sequence length | cache | cache size | cache hit rate | run time |
| -------- | ------ | --------------- | ----- | ---------- | -------------- | -------- |
| test5.gbk|  102   |     20          |  no   |    0       |      0%        |  22.82s  |
| test5.gbk|  102   |     20          |  yes  |    100     |      66.49%    |  16.65s  |
| test5.gbk|  102   |     20          |  yes  |    500     |      77.90%    |  11.21s  |
| test5.gbk|  102   |     20          |  yes  |    1000    |      81.51%    |  10.54s  |
| test5.gbk|  102   |     20          |  yes  |    5000    |      95.17%    |   4.50s  |
| test5.gbk|  102   |     20          |  yes  |    10000   |      99.67%    |   2.18s  |
