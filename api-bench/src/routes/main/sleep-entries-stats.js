module.exports = (apiRootUrl,) => ({
  id: 'sleep-entries-stats',
  main: {
    method: 'get',
    url: apiRootUrl + '/api/sleep-entries/stats',
    maxDuration: 1000,
  },
  pre: ['^cookie',],
  post: ['^cookie', '^status-2xx', '^json-validator', '^max-time',],
});
