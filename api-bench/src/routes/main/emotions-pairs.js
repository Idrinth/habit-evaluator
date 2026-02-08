module.exports = (apiRootUrl,) => ({
  id: 'emotions-pairs',
  main: {
    method: 'get',
    url: apiRootUrl + '/api/emotions/pairs',
    maxDuration: 500,
  },
  pre: ['^cookie',],
  post: ['^cookie', '^status-2xx', '^json-validator', '^max-time',],
});
