module.exports = (apiRootUrl,) => ({
  id: 'stats-emotion-scatter',
  main: {
    method: 'get',
    url: apiRootUrl + '/api/stats/emotion-scatter',
    maxDuration: 1000,
  },
  pre: ['^cookie',],
  post: ['^cookie', '^status-2xx', '^json-validator', '^max-time',],
});
