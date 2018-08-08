var packageJSON = require('./package.json');
var path = require('path');
var webpack = require('webpack');

const PATHS = {
    build: path.join(__dirname, 'target/classes/webroot', packageJSON.name),
    src: path.join(__dirname, 'src/main/javascript')
};

module.exports = {
    entry: PATHS.src + '/index.js',

    output: {
        path: PATHS.build,
        filename: 'app.js'
    }
};
