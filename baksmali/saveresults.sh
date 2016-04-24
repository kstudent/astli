#!/bin/bash
today=$(date '+%b-%d-%H-%M')
mkdir log/$today
cp log/*.org log/$today/
echo "saved results to" 
echo "$(pwd)/log/$today/result.org"
echo "$(pwd)/log/$today/info.org"
echo "$(pwd)/log/$today/debug.org"
