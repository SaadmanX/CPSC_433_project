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

CMSA U13T3 DIV 02           TU, 9:00

CMSA U13T3 DIV 01           FR, 8:00

CUSA O18 DIV 01 PRC 01      FR, 10:00 

CMSA U13T3 DIV 01 PRC 01    MO, 11:00
CMSA U13T3 DIV 01 PRC 01    WE, 11:00
CMSA U13T3 DIV 01 PRC 01    FR, 11:00

Penalty: 17 (1 pair, 6 minFilled, 10 prefPen)

### minnunmber
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

/hc10
There are two special " practice bookings" CMSA U12T1S and CMSA 
U13T1S that must be scheduled Tuesdays / Thursdays 18:00-19:00. CMSA 
U12T1S is not allowed to overlap with any practices/games of CMSA U12T1 
and CMSA U13T1S is not allowed to overlap with any practices/games of 
CMSA U13T1. These two "practice bookings" are a way of schedule special 
showcase tryouts series for these divisions’ players for selection to special 
provincial teams for Alberta Games. These bookings are only triggered if the 
respective game booking for CMSA U12T1 or CMSA U13T1 is requested. 