cmake_minimum_required(VERSION 3.7)
project (Hello)
add_library(hello STATIC hello.c)
file(COPY hello.h DESTINATION ${CMAKEIFY_REDIST_INCLUDE_DIRECTORY})