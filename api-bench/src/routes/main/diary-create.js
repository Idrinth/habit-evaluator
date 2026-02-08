module.exports = (apiRootUrl,) => ({
  id: 'diary-create',
  main: {
    method: 'post',
    url: apiRootUrl + '/api/diary',
    autohandle: 'json',
    body: {
      description: 'Bench test diary entry',
      significance: 'NORMAL',
      eventDate: new Date().toISOString().split('T')[0],
    },
    maxDuration: 500,
  },
  pre: ['^cookie',],
  post: ['^cookie', '^status-2xx', '^json-validator', '^max-time',],
});
