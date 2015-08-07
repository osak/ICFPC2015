#!/bin/bash

docker run -d \
    -v `pwd`:/var/www/sakimori \
    -v `pwd`/../problems:/var/www/problems \
    -h sakimori \
    nginx:latest \
    nginx
