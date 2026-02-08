module.exports = (apiRootUrl,) => ({
  id: 'habits-create',
  main: {
    method: 'post',
    url: apiRootUrl + '/api/habits',
    autohandle: 'json',
    body: {
      name: 'Bench Test Habit',
      description: 'Created by API benchmark',
      frequencyType: 'DAILY',
      targetFrequency: 1,
      maxEntriesPerDay: 3,
      positiveScoring: true,
    },
    maxDuration: 500,
  },
  pre: ['^cookie',],
  post: ['^cookie', '^status-2xx', '^json-validator', '^max-time',],
});
