({
  appDir: './',
  baseUrl: './',
  dir: '../scripts.min',
  optimize: 'uglify', // none || uglify
  paths: {
    jquery: 'require-jquery'
  },

  modules: [
    { name: 'header', exclude: ['jquery'] },
    { name: 'bundle', exclude: ['jquery'] },
    { name: 'index', exclude: ['jquery'] }
  ]
})
