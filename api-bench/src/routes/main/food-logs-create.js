module.exports = (apiRootUrl,) => ({
  id: 'food-logs-create',
  main: {
    method: 'post',
    url: apiRootUrl + '/api/food-logs',
    autohandle: 'json',
    body: {
      dateTime: new Date().toISOString(),
      foodItemList: ['Oatmeal', 'Banana',],
      kcal: 350,
      carbohydrates: 60,
      protein: 10,
    },
    maxDuration: 500,
  },
  pre: ['^cookie',],
  post: ['^cookie', '^status-2xx', '^json-validator', '^max-time',],
});
