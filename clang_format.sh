#!/bin/sh
find src -type f -name "*.java" -exec clang-format -i {} +