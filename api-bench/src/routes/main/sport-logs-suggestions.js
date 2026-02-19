module.exports = (apiRootUrl,) => ({
  id: 'sport-logs-suggestions',
  main: {
    method: 'get',
    url: apiRootUrl + '/api/sport-logs/suggestions',
    maxDuration: 500,
  },
  pre: ['^cookie',],
  post: ['^cookie', '^status-2xx', '^json-validator', '^max-time',],
});
