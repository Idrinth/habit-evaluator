module.exports = (apiRootUrl,) => ({
  id: 'diary-suggestions',
  main: {
    method: 'get',
    url: apiRootUrl + '/api/diary/suggestions',
    maxDuration: 500,
  },
  pre: ['^cookie',],
  post: ['^cookie', '^status-2xx', '^json-validator', '^max-time',],
});
