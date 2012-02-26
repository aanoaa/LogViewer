define [
  "jquery",
  "order!http://cdnjs.cloudflare.com/ajax/libs/underscore.js/1.3.1/underscore-min.js",
  "order!http://cdnjs.cloudflare.com/ajax/libs/backbone.js/0.9.1/backbone-min.js"
], ($) ->
  class LogView extends Backbone.View
    initialize: ->
      @render()
    template: _.template($('#messages-item-template').html())
    render: =>
      html = _.template($('#messages-template').html()) {}
      messages = ''
      _.each @collection.models, (log) =>
        dt = new Date(log.get('timestamp'))
        log.set { time: "#{dt.getHours()}:#{dt.getMinutes()}" }
        messages += @template log.attributes
      $(@el).html(html).find('tbody').append(messages)
      $('#content').html(@el)
      @delegateEvents()
      @
