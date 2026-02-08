module.exports = (apiRootUrl,) => ({
  id: 'food-logs-list',
  main: {
    method: 'get',
    url: apiRootUrl + '/api/food-logs',
    maxDuration: 500,
  },
  pre: ['^cookie',],
  post: ['^cookie', '^status-2xx', '^json-validator', '^max-time',],
});
