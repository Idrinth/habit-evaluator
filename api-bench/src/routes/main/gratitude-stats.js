module.exports = (apiRootUrl,) => ({
  id: 'gratitude-stats',
  main: {
    method: 'get',
    url: apiRootUrl + '/api/gratitude/stats',
    maxDuration: 1000,
  },
  pre: ['^cookie',],
  post: ['^cookie', '^status-2xx', '^json-validator', '^max-time',],
});
