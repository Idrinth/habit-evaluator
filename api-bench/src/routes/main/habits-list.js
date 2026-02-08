module.exports = (apiRootUrl,) => ({
  id: 'habits-list',
  main: {
    method: 'get',
    url: apiRootUrl + '/api/habits',
    maxDuration: 500,
  },
  pre: ['^cookie',],
  post: ['^cookie', '^status-2xx', '^json-validator', '^max-time',],
});
