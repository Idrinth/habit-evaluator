module.exports = (apiRootUrl,) => ({
  id: 'stats-food-distribution',
  main: {
    method: 'get',
    url: apiRootUrl + '/api/stats/food-distribution',
    maxDuration: 1000,
  },
  pre: ['^cookie',],
  post: ['^cookie', '^status-2xx', '^json-validator', '^max-time',],
});
