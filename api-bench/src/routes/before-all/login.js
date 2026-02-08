module.exports = (apiRootUrl,) => ({
  id: 'auth-login',
  main: {
    method: 'post',
    url: apiRootUrl + '/api/auth/login',
    autohandle: 'json',
    body: {
      username: 'demo',
      password: 'demo123',
    },
  },
  pre: ['^cookie',],
  post: ['^cookie', '^status-2xx', '^json-validator',],
});
