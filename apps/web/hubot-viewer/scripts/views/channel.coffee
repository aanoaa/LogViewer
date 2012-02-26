define [
  "jquery",
  "order!http://cdnjs.cloudflare.com/ajax/libs/underscore.js/1.3.1/underscore-min.js",
  "order!http://cdnjs.cloudflare.com/ajax/libs/backbone.js/0.9.1/backbone-min.js"
], ($) ->
  class ChannelView extends Backbone.View
    id: 'channels'
    tagName: 'ul'
    initialize: ->
      @render()
    template: _.template($('#channel-item-template').html())
    render: =>
      html = ''
      for channel in @options.data
        html += @template { channel: channel.split('#')[1] }
      $(@el).html(html)
      $('header').html(@el)
      @delegateEvents()
      @
