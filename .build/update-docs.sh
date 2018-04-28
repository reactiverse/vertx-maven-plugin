#!/usr/bin/env bash
set -e

echo ===========================================
echo Deploying vertx-maven-plugin documentation
echo ===========================================

mvn -Pdoc-html
git clone -b gh-pages git@github.com:reactiverse/vertx-maven-plugin gh-pages
cp -rv target/generated-docs/* gh-pages
cd gh-pages
git add --ignore-errors *
git commit -m "generated documentation"
git push origin gh-pages
cd ..
rm -rf gh-pages target
