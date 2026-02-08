module.exports = (apiRootUrl,) => ({
  id: 'sport-logs-graph',
  main: {
    method: 'get',
    url: apiRootUrl + '/api/sport-logs/graph',
    maxDuration: 1000,
  },
  pre: ['^cookie',],
  post: ['^cookie', '^status-2xx', '^json-validator', '^max-time',],
});
