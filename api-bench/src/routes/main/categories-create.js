module.exports = (apiRootUrl,) => ({
  id: 'categories-create',
  main: {
    method: 'post',
    url: apiRootUrl + '/api/categories',
    autohandle: 'json',
    body: {
      name: 'Bench Test Category',
      description: 'Created by API benchmark',
      color: '#4A90D9',
    },
    maxDuration: 500,
  },
  pre: ['^cookie',],
  post: ['^cookie', '^status-2xx', '^json-validator', '^max-time',],
});
