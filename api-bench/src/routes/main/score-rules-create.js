module.exports = (apiRootUrl,) => ({
  id: 'score-rules-create',
  main: {
    method: 'post',
    url: apiRootUrl + '/api/score-rules',
    autohandle: 'json',
    body: {
      name: 'Bench Test Rule',
      thresholdFor1Point: 1,
      thresholdFor2Points: 2,
      thresholdFor4Points: 4,
      thresholdFor8Points: 7,
    },
    maxDuration: 500,
  },
  pre: ['^cookie',],
  post: ['^cookie', '^status-2xx', '^json-validator', '^max-time',],
});
