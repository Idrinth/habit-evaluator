module.exports = (apiRootUrl,) => ({
  id: 'stats-correlations',
  main: {
    method: 'get',
    url: apiRootUrl + '/api/stats/correlations',
    maxDuration: 2000,
  },
  pre: ['^cookie',],
  post: ['^cookie', '^status-2xx', '^json-validator', '^max-time',],
});
