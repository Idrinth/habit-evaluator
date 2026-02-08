module.exports = (apiRootUrl,) => ({
  id: 'reminder-settings-get',
  main: {
    method: 'get',
    url: apiRootUrl + '/api/reminder-settings',
    maxDuration: 500,
  },
  pre: ['^cookie',],
  post: ['^cookie', '^status-2xx', '^json-validator', '^max-time',],
});
