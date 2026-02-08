module.exports = (apiRootUrl,) => ({
  id: 'habits-predict',
  main: {
    method: 'get',
    url: apiRootUrl + '/api/habits/predict',
    maxDuration: 1000,
  },
  pre: ['^cookie',],
  post: ['^cookie', '^status-2xx', '^json-validator', '^max-time',],
});
