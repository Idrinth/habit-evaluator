module.exports = (apiRootUrl,) => ({
  id: 'init-defaults',
  main: {
    method: 'post',
    url: apiRootUrl + '/api/init-defaults?language=en',
  },
  pre: ['^cookie',],
  post: ['^cookie', '^status-2xx', '^json-validator',],
});
