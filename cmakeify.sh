#!/bin/bash
# cmakeify installs and runs CMake with different configurations


CMAKEIFY_CMAKE_VERSION=cmake-3.7.20161217-g65aad

# install cross-targeting prereqs
if ["$CMAKE_SYSTEM_NAME" == "Windows"]; then
  wget http://sourceforge.net/projects/mingw-w64/files/Toolchains%20targetting%20Win64/Automated%20Builds/mingw-w64-bin_x86_64-linux_20131228.tar.bz2/download -O mingw-w64.tar.bz2/download
fi





# install a CMake 
mkdir prebuilts/
if [[ "$(uname -s)" == 'Darwin' ]]; then
wget --no-check-certificate https://cmake.org/files/dev/${CMAKEIFY_CMAKE_VERSION}-Darwin-x86_64.tar.gz -O cmake.tar.gz
tar xvfz cmake.tar.gz -C prebuilts/ 
mv ${CMAKEIFY_CMAKE_VERSION}-Darwin-x86_64 cmake
else
wget --no-check-certificate https://cmake.org/files/dev/${CMAKEIFY_CMAKE_VERSION}-Linux-x86_64.tar.gz -O cmake.tar.gz
tar xvfz cmake.tar.gz -C prebuilts/ > untar.cmake.txt
mv prebuilts/${CMAKEIFY_CMAKE_VERSION}-Linux-x86_64 prebuilts/cmake
fi

prebuilts/cmake/bin/cmake --version



# build
mkdir build/
cd build
../prebuilts/cmake/bin/cmake .. \
 -DCMAKE_ARCHIVE_OUTPUT_DIRECTORY=bin \
 -DCMAKE_LIBRARY_OUTPUT_DIRECTORY=bin \
 -DCMAKE_RUNTIME_OUTPUT_DIRECTORY=bin \
 -DCMAKE_SYSTEM_NAME=${CMAKE_SYSTEM_NAME}
make
ls bin/