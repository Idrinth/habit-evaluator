module.exports = (apiRootUrl,) => ({
  id: 'sleep-entries-create',
  main: {
    method: 'post',
    url: apiRootUrl + '/api/sleep-entries',
    autohandle: 'json',
    body: {
      date: new Date().toISOString().split('T')[0],
      fromTime: '23:00:00',
      untilTime: '07:00:00',
      notes: 'Bench test sleep entry',
    },
    maxDuration: 500,
  },
  pre: ['^cookie',],
  post: ['^cookie',],
});
