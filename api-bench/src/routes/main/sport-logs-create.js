module.exports = (apiRootUrl,) => ({
  id: 'sport-logs-create',
  main: {
    method: 'post',
    url: apiRootUrl + '/api/sport-logs',
    autohandle: 'json',
    body: {
      date: new Date().toISOString().split('T')[0],
      name: 'Running',
      durationHours: 0.5,
      measurement: 5.0,
      measurementUnit: 'km',
    },
    maxDuration: 500,
  },
  pre: ['^cookie',],
  post: ['^cookie', '^status-2xx', '^json-validator', '^max-time',],
});
