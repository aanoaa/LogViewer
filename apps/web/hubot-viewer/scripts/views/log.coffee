define [
  "jquery",
  "order!http://cdnjs.cloudflare.com/ajax/libs/underscore.js/1.3.1/underscore-min.js",
  "order!http://cdnjs.cloudflare.com/ajax/libs/backbone.js/0.9.1/backbone-min.js"
], ($) ->
  class LogView extends Backbone.View
    initialize: ->
      @render()
    urlRegex: new RegExp(/(\b(https?|ftp|file):\/\/[-A-Z0-9+&@#\/%?=~_|!:,.;]*[-A-Z0-9+&@#\/%=~_|])/ig)
    scriptRegex: new RegExp(/script/i)
    template: _.template($('#messages-item-template').html())
    render: =>
      dt = new Date()
      html = _.template($('#messages-template').html()) { channel: @options.channel, date: "#{dt.getFullYear()}-#{dt.getMonth() + 1}-#{dt.getDate()}" }
      messages = ''
      _.each @collection.models, (log) =>
        dt = new Date(log.get('timestamp'))
        message = log.escape('message')
        ###
        message = log.get('message')
        if message.match(@scriptRegex)
          message = log.escape('message')
        else
          message = message.replace(@urlRegex, "<a href=\"$1\" target=\"blank\">$1</a>")
        ###
        log.set { time: "#{dt.getHours()}:#{dt.getMinutes()}", message: message }
        messages += @template log.attributes
      $(@el).html(html).find('tbody').append(messages)
      $('#content').html(@el)
      @delegateEvents()
      @
# IRC log for #mojo, 2012-02-26
