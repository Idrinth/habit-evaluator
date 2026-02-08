module.exports = (apiRootUrl,) => ({
  id: 'diary-stats',
  main: {
    method: 'get',
    url: apiRootUrl + '/api/diary/stats',
    maxDuration: 1000,
  },
  pre: ['^cookie',],
  post: ['^cookie', '^status-2xx', '^json-validator', '^max-time',],
});
