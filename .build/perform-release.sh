#!/usr/bin/env bash
set -e

RED='\033[0;31m'
NC='\033[0m' # No Color
YELLOW='\033[0;33m'
BLUE='\033[0;34m'

# Extract versions
LAST_TAG=$(git tag -l --sort=-v:refname | head -n 1)

echo -e "${BLUE}Last tag:${YELLOW} ${LAST_TAG} ${NC}"

cd target || exit
export GPG_TTY=$(tty)
echo -e "${BLUE}Cloning repo${NC}"
git clone git@github.com:reactiverse/vertx-maven-plugin release-work
cd release-work
echo -e "${BLUE}Switching to tag${NC}"
git checkout ${LAST_TAG}
echo -e "${BLUE}Running deployment${NC}"
mvn deploy -Psonatype,release

echo -e "${BLUE}Build doc${NC}"
mvn -Pdoc-html
echo -e "${BLUE}Deploying doc${NC}"
git clone -b gh-pages git@github.com:reactiverse/vertx-maven-plugin gh-pages
cp -rv target/generated-docs/* gh-pages
cd gh-pages
git add --ignore-errors *
git commit -m "generated documentation"
git push origin gh-pages
cd ..  || exit
rm -Rf gh-pages

