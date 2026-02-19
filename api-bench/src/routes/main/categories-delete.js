module.exports = (apiRootUrl,) => ({
  id: 'categories-delete',
  main: {
    method: 'delete',
    url: apiRootUrl + '/api/categories/bench-category-id?confirm=true',
    maxDuration: 500,
  },
  pre: ['^cookie',],
  post: ['^cookie', '^status-2xx', '^max-time',],
});
