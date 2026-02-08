module.exports = (apiRootUrl,) => ({
  id: 'emotions-graph',
  main: {
    method: 'get',
    url: apiRootUrl + '/api/emotions/graph',
    maxDuration: 1000,
  },
  pre: ['^cookie',],
  post: ['^cookie', '^status-2xx', '^json-validator', '^max-time',],
});
