module.exports = (apiRootUrl,) => ({
  id: 'stats-dashboard',
  main: {
    method: 'get',
    url: apiRootUrl + '/api/stats/dashboard',
    maxDuration: 1000,
  },
  pre: ['^cookie',],
  post: ['^cookie', '^status-2xx', '^json-validator', '^max-time',],
});
