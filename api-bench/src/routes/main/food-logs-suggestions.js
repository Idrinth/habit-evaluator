module.exports = (apiRootUrl,) => ({
  id: 'food-logs-suggestions',
  main: {
    method: 'get',
    url: apiRootUrl + '/api/food-logs/suggestions',
    maxDuration: 500,
  },
  pre: ['^cookie',],
  post: ['^cookie', '^status-2xx', '^json-validator', '^max-time',],
});
