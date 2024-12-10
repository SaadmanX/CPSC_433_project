
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

<<<<<<< HEAD
Eval-value: 0
Solution:
CMSA U13T3 DIV 01: MO, 9:00
CMSA U13T3 DIV 01 PRC 01: FR, 13:00
CMSA U13T3 DIV 01 PRC 02: MO, 11:00
CMSA U13T3 DIV 02: FR, 9:00
CMSA U13T3 DIV 02 PRC 01: FR, 11:00
CUSA O18 DIV 01: MO, 9:00
CUSA O18 DIV 02: FR, 10:00
=======
Penalty: 1 (from not Pair)

#### shortInput3.txt

Assignment: 

CMSA U13T3 DIV 01           MO, 9:00
CMSA U13T3 DIV 01           WE, 9:00
CMSA U13T3 DIV 01           FR, 9:00
CMSA U13T3 DIV 02           FR, 10:00
CUSA O18 DIV 01             FR, 10:00
CUSA O18 DIV 02             FR, 10:00

CMSA U13T3 DIV 01 PRC 01    FR, 13:00
CMSA U13T3 DIV 02 PRC 01    MO, 11:00
CMSA U13T3 DIV 02 PRC 02    FR, 11:00

CMSA U13T3 DIV 01 PRC 02    TU, 9:00
CMSA U13T3 DIV 01 PRC 02    TH, 9:00

Penalty: 2 (from not Pair and secDiff)

#### input.txt
CUSA O18 DIV 01             MO, 8:00 
CUSA O18 DIV 01             WE, 8:00
CUSA O18 DIV 01             FR, 8:00

CMSA U13T3 DIV 02           TU, 9:00
CMSA U13T3 DIV 02           TH, 9:00

CMSA U13T3 DIV 01           FR, 8:00

CUSA O18 DIV 01 PRC 01      FR, 10:00 

CMSA U13T3 DIV 01 PRC 01    MO, 11:00
CMSA U13T3 DIV 01 PRC 01    WE, 11:00
CMSA U13T3 DIV 01 PRC 01    FR, 11:00

Penalty: 6 (1 pair, 5 minFilled)

#### input2.txt

CUSA O18 DIV 01             MO, 8:00 
CUSA O18 DIV 01             WE, 8:00
CUSA O18 DIV 01             FR, 8:00

CMSA U13T3 DIV 02           TU, 9:00

CMSA U13T3 DIV 01           FR, 8:00

CUSA O18 DIV 01 PRC 01      FR, 10:00 

CMSA U13T3 DIV 01 PRC 01    MO, 11:00
CMSA U13T3 DIV 01 PRC 01    WE, 11:00
CMSA U13T3 DIV 01 PRC 01    FR, 11:00

Penalty: 17 (1 pair, 6 minFilled, 10 prefPen)
>>>>>>> 1b9ef57d199a476b34dde15353268aff6a6289a3
>>>>>>> 21509c958389b6e802ab48a0e43f952690647059
