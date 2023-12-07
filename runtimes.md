# Project Runtime Table

Hyperfine was used to do several back-to-back runs of the program (including 3 warmup runs each) to provide statistics
about the runtime, such as averages, standard deviations, and min/max runtimes. The "run time" column in this table
gives an average of all the runs conducted by Hyperfine.

## Professors' Solution

- No SQLite database created
- Ran on a Macbook with an M1 Pro chip and 16GB of memory

| gbk file | degree | sequence length | cache | cache size | cache hit rate | run time |
| -------- | ------ | --------------- | ----- | ---------- | -------------- | -------- |
| test5.gbk|  102   |     20          |  no   |    0       |      0%        |  29.52s  |
| test5.gbk|  102   |     20          |  yes  |    100     |      66.14%    |  12.56s  |
| test5.gbk|  102   |     20          |  yes  |    500     |      77.84%    |  10.22s  |
| test5.gbk|  102   |     20          |  yes  |    1000    |      81.45%    |  10.05s  |
| test5.gbk|  102   |     20          |  yes  |    5000    |      95.08%    |   5.08s  |
| test5.gbk|  102   |     20          |  yes  |    10000   |      99.51%    |   3.15s  |


## Team 9 Solution

- No SQLite database created
- Ran on a Macbook with an M1 standard chip and 8GB of memory

| gbk file | degree | sequence length | cache | cache size | cache hit rate | run time |
| -------- | ------ | --------------- | ----- | ---------- | -------------- | -------- |
| test5.gbk|  102   |     20          |  no   |    0       |      0%        |  22.82s  |
| test5.gbk|  102   |     20          |  yes  |    100     |      66.49%    |  16.65s  |
| test5.gbk|  102   |     20          |  yes  |    500     |      77.90%    |  11.21s  |
| test5.gbk|  102   |     20          |  yes  |    1000    |      81.51%    |  10.54s  |
| test5.gbk|  102   |     20          |  yes  |    5000    |      95.17%    |   4.50s  |
| test5.gbk|  102   |     20          |  yes  |    10000   |      99.67%    |   2.18s  |
