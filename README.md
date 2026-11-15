# CSA09 – Assignment 1: Campus Smart-Parking System

Design, implement and validate an object-oriented model for a campus smart-parking facility (CO1 – Java fundamentals and OOP).

| | |
|---|---|
| **Name** | `<YOUR NAME>` |
| **Register Number** | `<REGISTER NUMBER>` |
| **Java version** | OpenJDK 21 (any JDK 17 or newer works – the code uses records, switch expressions and `instanceof` patterns) |
| **Report** | [`report/Assignment1_Report.pdf`](report/Assignment1_Report.pdf) (also `.docx` and `.md`) |

## Repository layout

```
TaskNN/
├── *.java  or  src/parking/{model,service,app,pricing}/*.java   source code
├── input.txt  or  input/*.txt                                    input files
├── output/*.txt                                                  console output (incl. compile + run commands)
└── screenshots/*.png                                             output screenshots
report/      Assignment1_Report.{pdf,docx,md}, uml/*.png (+ Graphviz .dot sources)
run_all.sh   compiles and runs every program and refreshes output/*.txt
tools/       render_screenshots.py (output .txt -> .png), build_report.py
```

## How to compile and run

Run everything at once (Linux / macOS / Git Bash on Windows):

```bash
bash run_all.sh
```

Or run each program on its own from inside its task folder:

| Task | Compile | Run |
|---|---|---|
| 01 | `javac -d out ObjectsVsParallelArrays.java` | `java -cp out ObjectsVsParallelArrays input.txt` |
| 02 | `javac -d out DataRepresentation.java` | `java -cp out DataRepresentation < input.txt` |
| 03 *(Complete the Code)* | `javac -d out ParkingFacility.java` | `java -cp out ParkingFacility` |
| 03 | `javac -d out FeeSlabCalculator.java` | `java -cp out FeeSlabCalculator input.txt` |
| 03 | `javac -d out ParkingFacility.java FreeStackFacility.java` | `java -cp out FreeStackFacility` |
| 04 | `javac -d out *.java` | `java -cp out ConstructorDemo input.txt` |
| 05 | `javac -d out StaticMembersDemo.java` | `java -cp out StaticMembersDemo input.txt` |
| 06 | `javac -d out InheritanceDemo.java` | `java -cp out InheritanceDemo input.txt` |
| 07 | `javac -d out OverridingDemo.java` | `java -cp out OverridingDemo input.txt` |
| 08 *(Complete the Code)* | `javac -d out FeeDemo.java` | `java -cp out FeeDemo` |
| 08 | `javac -d out FeeDemo.java PolymorphismDemo.java` | `java -cp out PolymorphismDemo input.txt` |
| 09 | `javac -d out AbstractionDemo.java` | `java -cp out AbstractionDemo input.txt` |
| 10 | `javac -d out $(find src -name "*.java")` | `java -cp out parking.app.Main input.txt` |
| 11 | `javac -d out $(find src -name "*.java")` | `java -cp out parking.app.ParkingApp input/day_scenario.txt`<br>`java -cp out parking.app.TestRunner`<br>`java -cp out parking.app.TicketStoreBenchmark` |
| 12 | `javac -d out $(find src -name "*.java")` | `java -cp out parking.app.ParkingApp input/day_scenario_v2.txt`<br>`java -cp out parking.app.TestRunner` |

On Windows PowerShell replace `$(find src -name "*.java")` with `(Get-ChildItem -Recurse src -Filter *.java).FullName`.

## Fee rules (assumptions)

Two-wheeler Rs 10/h (first 30 min free), car and EV Rs 30/h; every started hour billed in full; 0 min = Rs 0; daily maximum 8 × hourly rate; EV charging Rs 12/kWh (paid even by pass holders); students/staff with a valid monthly pass park free; visitors pay. Task 12 adds: e-bike flat Rs 20, delivery van Rs 60/h for hours starting 09:00–17:00 (Rs 30 otherwise), visitor one-day pass Rs 100.

## Result of each task

| Task | Topic | Result |
|---|---|---|
| 01 | Problem analysis & objects | Same search results from object array and parallel arrays; both O(n) (200 000 vehicles: 0.77 ms vs 0.65 ms). Adding an attribute = 1 field vs 1 new array + edits everywhere. |
| 02 | Data representation | Jagged array for floors of 20/50/100 slots. Integer-division bug shown: 130 min billed Rs 60 instead of Rs 90; fixed with `(m + 59) / 60`. Naive 1-D index `f*S+s` finds the wrong slot. |
| 03 | Control logic | **Complete the Code output matches the expected output exactly.** Fee slabs tested at every edge (0, 60, 61, 420, 421, 1440, 1500 min). Free-slot stack: 43 ns vs 7 447 ns per allocation on a 20 × 1000 facility. |
| 04 | Constructors | Default, overloaded and chained constructors; copy constructor re-issues a duplicate ticket with the same number; setter-built object shown to be invalid. |
| 05 | static members | Instance counter reuses `CMP1001` across two gates; static counter gives unique numbers. Static block runs once; ticket collected by GC after last reference is dropped. |
| 06 | Inheritance | Vehicle → Car → ElectricCar and User hierarchies; constructor order traced in 6 steps; UML in `Task06/`. |
| 07 | Overriding | `@Override`, `final`, `equals`/`hashCode` on registration number – a `HashSet` keeps one of three same-plate vehicles. |
| 08 | Polymorphism | **Complete the Code output matches the expected output exactly** (30.0 / 90.0 / 210.0 / true). Dispatch trace table and overload selection shown. |
| 09 | Abstraction | Abstract `Vehicle`/`User`, interfaces `Chargeable`, `Payable`, `Reservable` with default and static methods. |
| 10 | Packages & encapsulation | `parking.model/service/app`; setters reject floor 5, slot 100, −15 min, bad plates; public-field version produces a fee of Rs −30; `Integer 500 == 500` is false. |
| 11 | Full system & testing | **37 / 37 tests pass**; day scenario total Rs 1020 equals the hand calculation; HashMap ticket lookup stays ~75 ns while the array grows to 441 µs at 100 000 tickets. |
| 12 | Design challenge | Decision **MODIFY**: 0 vehicle classes changed, 6 classes modified, 8 new (Pass, PricingRule, EBike, DeliveryVan …). **55 / 55 tests pass**, new-rules day total Rs 574 equals the hand calculation. |

## Screenshots

The PNGs in `screenshots/` are rendered from the captured console output in `output/` by `tools/render_screenshots.py`.
