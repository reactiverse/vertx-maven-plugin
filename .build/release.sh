#!/usr/bin/env bash
set -e

RED='\033[0;31m'
NC='\033[0m' # No Color
YELLOW='\033[0;33m'
BLUE='\033[0;34m'

# Extract versions
LAST_TAG=$(git tag -l --sort=-v:refname | head -n 1)
NEW_VERSION=""
NEW_TAG=""
NEXT_DEV_VERSION=""
TAG=""

echo "Last tag: ${LAST_TAG}"
regex="v([0-9]+).([0-9]+).([0-9]+)"
if [[ ${LAST_TAG} =~ $regex ]]
then
    major="${BASH_REMATCH[1]}"
    minor="${BASH_REMATCH[2]}"
    micro="${BASH_REMATCH[3]}"
    VERSION=${major}.${minor}.$(($micro +1))
    TAG="v${NEW_VERSION}"
    NEXT_DEV_VERSION="${major}.${minor}-SNAPSHOT"
    echo -e "${BLUE}Release version: ${YELLOW}${VERSION} ${NC}"
    echo -e "${BLUE}Next development version: ${YELLOW}${NEXT_DEV_VERSION} ${NC}"
else
    echo -e "${RED}Invalid last tag ${NC}"
    exit -1
fi

# Update version in pom.xml file
echo -e "${BLUE}Updating project version to: ${YELLOW} ${VERSION} ${NC}"
mvn versions:set -DnewVersion=${VERSION} >& bump-version-dev.log
echo -e "${BLUE}Issuing a verification build${NC}"
mvn clean install -DskipTests >& fast-build.log

echo -e "${BLUE}Committing changes${NC}"
git commit -am "Releasing version ${VERSION}"

echo -e "${BLUE}Creating the tag ${YELLOW}${TAG}${NC}"
git tag -a ${TAG} -m "Releasing ${TAG}"

echo -e "${BLUE}Updating project version to: ${YELLOW}${NEXT_DEV_VERSION}${NC}"
mvn versions:set -DnewVersion=${NEXT_DEV_VERSION} >& bump-version-dev.log

mvn clean install -DskipTests >& fast-build-dev.log

echo -e "${BLUE}Committing changes${NC}"
git commit -am "Bumping version to ${NEXT_DEV_VERSION}"

echo -e "${BLUE}Pushing changes${NC}"
git push origin master --tags


