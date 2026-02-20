#!/bin/bash

# Helper script to create a new release.

releaseVersion="$1"
nextVersion="$2"

if [ "$releaseVersion" == "" ]; then
    echo "Usage: ./release.sh release-version next-snapshot-version"
    exit 1
fi

if [ "$nextVersion" == "" ]; then
    echo "Usage: ./release.sh release-version next-snapshot-version"
    exit 1
fi

echo "Setting new version to ${releaseVersion}"
sed -i "s/^version = \".*\"/version = \"${releaseVersion}\"/" build.gradle.kts

if [ $? -gt 0 ]; then
    exit 1;
fi

echo "Performing release"
./gradlew clean build publish

if [ $? -gt 0 ]; then
    exit 1;
fi

echo "Committing version ${releaseVersion}"
git commit -a -m "Setting version to $releaseVersion."

if [ $? -gt 0 ]; then
    exit 1;
fi

echo "Tagging version ${releaseVersion}"
git tag ${releaseVersion}

if [ $? -gt 0 ]; then
    exit 1;
fi

echo "Setting next snapshot version ${nextVersion}"
sed -i "s/^version = \".*\"/version = \"${nextVersion}\"/" build.gradle.kts

if [ $? -gt 0 ]; then
    exit 1;
fi

echo "Committing version ${nextVersion}"
git commit -a -m "Setting version to $nextVersion."

echo "Pushing commits and tags"
git push origin && git push origin --tags

if [ $? -gt 0 ]; then
    exit 1;
fi

echo "done."
