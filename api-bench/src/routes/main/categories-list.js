module.exports = (apiRootUrl,) => ({
  id: 'categories-list',
  main: {
    method: 'get',
    url: apiRootUrl + '/api/categories',
    maxDuration: 500,
  },
  pre: ['^cookie',],
  post: ['^cookie', '^status-2xx', '^json-validator', '^max-time',],
});
