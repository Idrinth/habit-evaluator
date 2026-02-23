module.exports = (apiRootUrl,) => ({
  id: 'meetings-suggestions',
  main: {
    method: 'get',
    url: apiRootUrl + '/api/meetings/suggestions',
    maxDuration: 500,
  },
  pre: ['^cookie',],
  post: ['^cookie', '^status-2xx', '^json-validator', '^max-time',],
});
