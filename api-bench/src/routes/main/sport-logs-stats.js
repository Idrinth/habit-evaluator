module.exports = (apiRootUrl,) => ({
  id: 'sport-logs-stats',
  main: {
    method: 'get',
    url: apiRootUrl + '/api/sport-logs/stats',
    maxDuration: 1000,
  },
  pre: ['^cookie',],
  post: ['^cookie', '^status-2xx', '^json-validator', '^max-time',],
});
