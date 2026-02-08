module.exports = (apiRootUrl,) => ({
  id: 'diary-list',
  main: {
    method: 'get',
    url: apiRootUrl + '/api/diary',
    maxDuration: 500,
  },
  pre: ['^cookie',],
  post: ['^cookie', '^status-2xx', '^json-validator', '^max-time',],
});
