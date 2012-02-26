
  require(["jquery", "routers/root"], function($, Router) {
    return $(document).ready(function() {
      new Router;
      return Backbone.history.start();
    });
  });
