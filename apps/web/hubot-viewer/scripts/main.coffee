require [
  "jquery",
  "routers/root"
], ($, Router) ->
  $(document).ready ->
    new Router
    Backbone.history.start()
