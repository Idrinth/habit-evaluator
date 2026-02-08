module.exports = (apiRootUrl,) => ({
  id: 'sport-logs-list',
  main: {
    method: 'get',
    url: apiRootUrl + '/api/sport-logs',
    maxDuration: 500,
  },
  pre: ['^cookie',],
  post: ['^cookie', '^status-2xx', '^json-validator', '^max-time',],
});
