#!/usr/bin/env bash
# Compiles and runs every program of Assignment 1 and saves each console output
# to TaskNN/output/<program>.txt (the command lines are recorded at the top).
#
# Usage:  bash run_all.sh            (from the repository root)
# Needs:  JDK 17 or newer (developed and tested on OpenJDK 21)
set -u
ROOT="$(cd "$(dirname "$0")" && pwd)"
unset JAVA_TOOL_OPTIONS 2>/dev/null || true

# run <task-folder> <output-name> <compile command> <run command>
run() {
  local dir="$1" name="$2" compile="$3" exec="$4"
  cd "$ROOT/$dir" || exit 1
  mkdir -p output
  local out="output/$name.txt"
  {
    echo "\$ cd $dir"
    echo "\$ $compile"
    eval "$compile" 2>&1
    echo "\$ $exec"
    eval "$exec" 2>&1
  } > "$out"
  echo "  $dir/$out"
}

echo "Java: $(java -version 2>&1 | head -1)"
echo "Writing outputs:"
run Task01 ObjectsVsParallelArrays "javac -d out ObjectsVsParallelArrays.java" "java -cp out ObjectsVsParallelArrays input.txt"
run Task02 DataRepresentation      "javac -d out DataRepresentation.java"      "java -cp out DataRepresentation < input.txt"
run Task03 ParkingFacility         "javac -d out ParkingFacility.java"         "java -cp out ParkingFacility"
run Task03 FeeSlabCalculator       "javac -d out FeeSlabCalculator.java"       "java -cp out FeeSlabCalculator input.txt"
run Task03 FreeStackFacility       "javac -d out ParkingFacility.java FreeStackFacility.java" "java -cp out FreeStackFacility"
run Task04 ConstructorDemo         "javac -d out *.java"                       "java -cp out ConstructorDemo input.txt"
run Task05 StaticMembersDemo       "javac -d out StaticMembersDemo.java"       "java -cp out StaticMembersDemo input.txt"
run Task06 InheritanceDemo         "javac -d out InheritanceDemo.java"         "java -cp out InheritanceDemo input.txt"
run Task07 OverridingDemo          "javac -d out OverridingDemo.java"          "java -cp out OverridingDemo input.txt"
run Task08 FeeDemo                 "javac -d out FeeDemo.java"                 "java -cp out FeeDemo"
run Task08 PolymorphismDemo        "javac -d out FeeDemo.java PolymorphismDemo.java" "java -cp out PolymorphismDemo input.txt"
run Task09 AbstractionDemo         "javac -d out AbstractionDemo.java"         "java -cp out AbstractionDemo input.txt"
run Task10 Main                    'javac -d out $(find src -name "*.java")'   "java -cp out parking.app.Main input.txt"
run Task11 ParkingApp_day_scenario 'javac -d out $(find src -name "*.java")'   "java -cp out parking.app.ParkingApp input/day_scenario.txt"
run Task11 TestRunner              'javac -d out $(find src -name "*.java")'   "java -cp out parking.app.TestRunner"
run Task11 TicketStoreBenchmark    'javac -d out $(find src -name "*.java")'   "java -cp out parking.app.TicketStoreBenchmark"
run Task12 ParkingApp_day_scenario_v2 'javac -d out $(find src -name "*.java")' "java -cp out parking.app.ParkingApp input/day_scenario_v2.txt"
run Task12 ParkingApp_regression_day1 'javac -d out $(find src -name "*.java")' "java -cp out parking.app.ParkingApp input/day_scenario.txt"
run Task12 TestRunner              'javac -d out $(find src -name "*.java")'   "java -cp out parking.app.TestRunner"
echo "Done."
