module.exports = (apiRootUrl,) => ({
  id: 'gratitude-list',
  main: {
    method: 'get',
    url: apiRootUrl + '/api/gratitude',
    maxDuration: 500,
  },
  pre: ['^cookie',],
  post: ['^cookie', '^status-2xx', '^json-validator', '^max-time',],
});
