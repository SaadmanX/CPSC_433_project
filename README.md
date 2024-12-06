Temporary cope:

Make all:
    javac Main.java

clean:
    find . -name "*.class" -type f -delete     


#### For Testing Purposes
#### shortInput.txt

Assignment: 

CMSA U13T3 DIV 01           MO, 9:00
CMSA U13T3 DIV 01           WE, 9:00
CMSA U13T3 DIV 01           FR, 9:00
CMSA U13T3 DIV 02           FR, 10:00
CUSA O18 DIV 01             FR, 10:00

CMSA U13T3 DIV 01 PRC 01    FR, 13:00
CMSA U13T3 DIV 02 PRC 01    MO, 11:00
CMSA U13T3 DIV 02 PRC 02    FR, 11:00

Penalty: 0

#### shortInput2.txt

Assignment: 

CMSA U13T3 DIV 01           MO, 9:00
CMSA U13T3 DIV 01           WE, 9:00
CMSA U13T3 DIV 01           FR, 9:00
CMSA U13T3 DIV 02           FR, 10:00
CUSA O18 DIV 01             FR, 10:00

CMSA U13T3 DIV 01 PRC 01    FR, 13:00
CMSA U13T3 DIV 02 PRC 01    MO, 11:00
CMSA U13T3 DIV 02 PRC 02    FR, 11:00

CMSA U13T3 DIV 01 PRC 02    TU, 9:00
CMSA U13T3 DIV 01 PRC 02    TH, 9:00

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
CUSA O18 DIV 09             FR, 19:00

CMSA U13T3 DIV 02           TU, 9:00

CMSA U13T3 DIV 01           FR, 8:00

CUSA O18 DIV 01 PRC 01      FR, 10:00 

CMSA U13T3 DIV 01 PRC 01    MO, 11:00
CMSA U13T3 DIV 01 PRC 01    WE, 11:00
CMSA U13T3 DIV 01 PRC 01    FR, 11:00

Penalty: 17 (1 pair, 6 minFilled, 10 prefPen)