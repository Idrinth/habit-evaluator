module.exports = (apiRootUrl,) => ({
  id: 'activity-logs-suggestions',
  main: {
    method: 'get',
    url: apiRootUrl + '/api/activity-logs/suggestions',
    maxDuration: 500,
  },
  pre: ['^cookie',],
  post: ['^cookie', '^status-2xx', '^json-validator', '^max-time',],
});
