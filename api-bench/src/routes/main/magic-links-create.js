module.exports = (apiRootUrl,) => ({
  id: 'magic-links-create',
  main: {
    method: 'post',
    url: apiRootUrl + '/api/magic-links',
    autohandle: 'json',
    body: {},
    maxDuration: 500,
  },
  pre: ['^cookie',],
  post: ['^cookie', '^status-2xx', '^json-validator', '^max-time',],
});
