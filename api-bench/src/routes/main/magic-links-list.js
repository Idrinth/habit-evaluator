module.exports = (apiRootUrl,) => ({
  id: 'magic-links-list',
  main: {
    method: 'get',
    url: apiRootUrl + '/api/magic-links',
    maxDuration: 500,
  },
  pre: ['^cookie',],
  post: ['^cookie', '^status-2xx', '^json-validator', '^max-time',],
});
