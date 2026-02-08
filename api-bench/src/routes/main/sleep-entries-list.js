module.exports = (apiRootUrl,) => ({
  id: 'sleep-entries-list',
  main: {
    method: 'get',
    url: apiRootUrl + '/api/sleep-entries',
    maxDuration: 500,
  },
  pre: ['^cookie',],
  post: ['^cookie', '^status-2xx', '^json-validator', '^max-time',],
});
