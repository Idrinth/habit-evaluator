module.exports = (apiRootUrl,) => ({
  id: 'gratitude-create',
  main: {
    method: 'post',
    url: apiRootUrl + '/api/gratitude',
    autohandle: 'json',
    body: {
      description: 'I am grateful for this benchmark test',
      eventDate: new Date().toISOString().split('T')[0],
    },
    maxDuration: 500,
  },
  pre: ['^cookie',],
  post: ['^cookie', '^status-2xx', '^json-validator', '^max-time',],
});
