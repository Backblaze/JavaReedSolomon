# JavaReedSolomon

This is a simple and efficient Reed-Solomon implementation in Java.

The ReedSolomon class does the encoding and decoding, and is supported
by Matrix, which does matrix arithmetic, and Galois, which is a finite
field over 8-bit values.

For examples of how to use ReedSolomon, take a look at SampleEncoder
and SampleDecoder.  They show, in a very simple way, how to break a
file into shards and encode parity, and then how to take a subset of
the shards and reconstruct the original file.

We would like to send out a special thanks to James Plank at the
University of Tennessee at Knoxville for his useful papers on erasure
coding.  If you'd like an intro into how it all works, take a look at
[this introductory paper](http://web.eecs.utk.edu/~plank/plank/papers/SPE-9-97.html).
