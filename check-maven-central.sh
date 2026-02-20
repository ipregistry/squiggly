#!/bin/bash

version="$1"

if [ "$version" == "" ]; then
    version=$(grep '^version = ' build.gradle.kts | sed 's/version = "\(.*\)"/\1/')
fi

echo -n "Checking Version $version on GitHub Packages: "

status=$(curl -s -o /dev/null -I -w "%{http_code}" \
    "https://maven.pkg.github.com/ipregistry/squiggly/com/github/ipregistry/squiggly-filter-jackson/${version}/squiggly-filter-jackson-${version}.jar")

if [ "$status" == "" ]; then
    echo "Error Unknown"
    exit 1
fi

if [ "$status" == "200" ]; then
    echo "Found"
    exit 0
fi

if [ "$status" == "404" ]; then
    echo "Not Found"
    exit 1
fi

echo "ERROR $status"
exit 1
