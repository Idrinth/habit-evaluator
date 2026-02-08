module.exports = (apiRootUrl,) => ({
  id: 'auth-me',
  main: {
    method: 'get',
    url: apiRootUrl + '/api/auth/me',
    maxDuration: 500,
  },
  pre: ['^cookie',],
  post: ['^cookie', '^status-2xx', '^json-validator', '^max-time',],
});
