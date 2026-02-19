module.exports = (apiRootUrl,) => ({
  id: 'categories-update',
  main: {
    method: 'put',
    url: apiRootUrl + '/api/categories/bench-category-id',
    autohandle: 'json',
    body: {
      name: 'Updated Bench Category',
      description: 'Updated by API benchmark',
      color: '#D94A90',
    },
    maxDuration: 500,
  },
  pre: ['^cookie',],
  post: ['^cookie', '^status-2xx', '^json-validator', '^max-time',],
});
