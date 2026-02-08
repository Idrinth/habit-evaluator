module.exports = (apiRootUrl,) => ({
  id: 'sync',
  main: {
    method: 'post',
    url: apiRootUrl + '/api/sync',
    autohandle: 'json',
    body: {
      habits: [],
    },
    maxDuration: 1000,
  },
  pre: ['^cookie',],
  post: ['^cookie', '^status-2xx', '^json-validator', '^max-time',],
});
