
###### Compiling instruction
Make all:
    `javac Main.java`

clean:
    `find . -name "*.class" -type f -delete`    

Compiling any test cases:
    `java Main input.txt a b c d v x y z` 

    where a, b, c, d and v, x, y, z are the weight and penalty, respectively


#### For Testing Purposes

### minnumber
minnumber.txt 1 0 0 0 100 100 0 10
Eval: 0

### parallelpen
parallelpen.txt 0 0 0 1 1 1 0 5
Eval: 5

### prefexamp
prefexamp.txt 0 1 0 0 100 100 0 100
Eval: 30

### pairing 
pairing.txt 0 0 1 0 0 0 11 0
Eval: 55

### shortInput:
shortinput.txt 1 1 1 1 1 1 1 1

Eval-value: 1
Solution:
CMSA U13T3 DIV 01: MO, 9:00
CMSA U13T3 DIV 01 PRC 01: FR, 13:00
CMSA U13T3 DIV 02: FR, 9:00
CMSA U13T3 DIV 02 PRC 01: FR, 11:00
CUSA O18 DIV 01: FR, 10:00

### shortInput2:
shortinput2.txt 1 1 1 1 1 1 1 1

Eval-value: 1
Solution:
CMSA U13T3 DIV 01: MO, 9:00
CMSA U13T3 DIV 01 PRC 01: FR, 13:00
CMSA U13T3 DIV 01 PRC 02: MO, 11:00
CMSA U13T3 DIV 02: FR, 9:00
CMSA U13T3 DIV 02 PRC 01: FR, 11:00
CUSA O18 DIV 01: FR, 10:00

### shortInput3:
shortInput3.txt 1 1 1 1 1 1 1 1

Eval-value: 0
Solution:
CMSA U13T3 DIV 01: MO, 9:00
CMSA U13T3 DIV 01 PRC 01: FR, 13:00
CMSA U13T3 DIV 01 PRC 02: MO, 11:00
CMSA U13T3 DIV 02: FR, 9:00
CMSA U13T3 DIV 02 PRC 01: FR, 11:00
CUSA O18 DIV 01: MO, 9:00
CUSA O18 DIV 02: FR, 10:00
