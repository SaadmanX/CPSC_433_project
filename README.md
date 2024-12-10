
###### Compiling instruction
Make all:
    `javac Main.java`

clean:
    `find . -name "*.class" -type f -delete`    

Compiling any test cases:
    `java Main input.txt a b c d v x y z` 

    where a, b, c, d and v, x, y, z are the weight and penalty, respectively

##### Large Input Solutions

The solutions of large inputs are in `city1.txt` and `city2.txt` for `LargeInput1.txt` `and LargeInput2.txt`, respectively.

To verify the result, you can run it by yourself with command `java Main LargeInput1.txt 1 1 1 1 1 1 1 1` and `javac Main LargeInput2.txt 1 1 1 1 1 1 1 1`
