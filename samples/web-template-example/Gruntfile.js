module.exports = function (grunt) {
    grunt.loadNpmTasks('grunt-contrib-coffee');
    grunt.initConfig({
        coffee: {
            compile: {
                options: {
                    expand: true,
                    flatten: true,
                    sourceMap: true,
                    bare: true
                },
                files: {
                    'target/classes/webroot/math.js': 'src/main/coffee/*'
                }

            }
        }
    });
    grunt.registerTask('default', ['coffee']);
};