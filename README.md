# Abstract-Syntax-Tree-based Library Identification (ASTLI)

## What It Is

ASTLI finds libraries (`.jar`) in obfuscated Android applications (`.apk`). First, ASTLI learns library by extracting fingerprints that are derived from the abstract syntax tree and from method signatures. Then, ASTLI matches fingerprints against an application and gives an estimate on how likely the library is present in the application. See [1] for further explanation. Please cite [2] if you use this work. 

## Requirements

- dx.jar from Android SDK Build Tools
- Tested with OpenJDK 8

## Build

1. `git clone http://github.com`
2. `cd astli`
1. copy `dx.jar` or create symlink to `dx.jar` in `astli/lib/`
3. `gradle jar`

## Usage

Run `java -jar gradle.jar` to a list of options.

## Notice

ASTLI is a research prototype, and, although ASTLI has been extensively evaluated with FOSS applications, one may encounter edge cases that ASTLI cannot handle. 

## Sources

[1] Rabensteiner, Christof: Android Library Identification (Master's Thesis); 2017;  http://diglib.tugraz.at/download.php?id=5988e795a35ec&location=search

[2] Feichtner, Johannes, and Rabensteiner, Christof: Obfuscation-Resilient Code Recognition in Android Apps; ARES 2019;  
