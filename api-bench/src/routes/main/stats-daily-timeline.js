module.exports = (apiRootUrl,) => ({
  id: 'stats-daily-timeline',
  main: {
    method: 'get',
    url: apiRootUrl + '/api/stats/daily-timeline',
    maxDuration: 1000,
  },
  pre: ['^cookie',],
  post: ['^cookie', '^status-2xx', '^json-validator', '^max-time',],
});
