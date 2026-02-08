module.exports = (apiRootUrl,) => ({
  id: 'reminder-settings-update',
  main: {
    method: 'put',
    url: apiRootUrl + '/api/reminder-settings',
    autohandle: 'json',
    body: {
      sleepReminderEnabled: true,
      sleepReminderTime: '22:00:00',
      diaryReminderEnabled: true,
      diaryReminderTime: '20:00:00',
      emotionReminderEnabled: false,
      emotionReminderCount: 3,
      wakingHoursStart: '07:00:00',
      wakingHoursEnd: '23:00:00',
    },
    maxDuration: 500,
  },
  pre: ['^cookie',],
  post: ['^cookie', '^status-2xx', '^json-validator', '^max-time',],
});
