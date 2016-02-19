var quote = require('quote');
var Clay = require('clay');
var clayConfig = require('config.json');

var clay = new Clay(clayConfig, null, { autoHandleEvents: false });

Pebble.addEventListener('ready', function(e) {

    //Check for saved data, if we have some then we can assume it's all there...
    //Though in reality you would be best to check the settings
    if (localStorage.length > 0) {
        //create an object to store the settings in
        var quoteData = {};

        //load the background colors
        quoteData.enableBackground = localStorage.getItem('enableBackground');
        quoteData.background = localStorage.getItem('background');

        //check if we have a custom quote saved
        if (localStorage.getItem('enableCustomQuote')) {

            //load the quote from storage
            quoteData.quote = localStorage.getItem('quote');
            quoteData.author = localStorage.getItem('author');

            //Send the quote
            sendQuote(quoteData);
        } else {

            //Retrieve a quote from the web
            quote.retrieveQuote(function(quote, author) {
                quoteData.quote = quote;
                quoteData.author = author;
                sendQuote(quoteData);
            }, function() {
                quoteData.quote = "There was an error fetching the quote!";
                quoteData.error = true;
                sendQuote(quoteData);
            });
        }
    } else {
        //Prompt the user to open the settings page for first time configuration
        sendQuote({
            error: true,
            quote: "Please open the settings to configure the app"
        });
    }

});

Pebble.addEventListener('showConfiguration', function(e) {
    //Have cla build our configuration page
    Pebble.openURL(clay.generateUrl());
});

Pebble.addEventListener('webviewclosed', function(e) {
    if (e && !e.response) {
        return;
    }

    //Get the settings that clay provides us
    var settings = clay.getSettings(e.response);

    //check if custom quotes are enabled
    if (settings.enableCustomQuote) {
        //send the custom quote to the pebble
        sendQuote(settings);
    } else {
        //Retrieve a quote from the web
        quote.retrieveQuote(function(quote, author) {
            settings.quote = quote;
            settings.author = author;
            sendQuote(settings);
        }, function() {
            settings.quote = "There was an error fetching the quote of the day!";
            settings.error = true;
            sendQuote(settings);
        });
    }
});

function sendQuote(data) {
    //Format the quote, if it is not an error message
    if (!data.error) {
        data.quote = '"' + data.quote + '" \n\n-' + data.author;
    }

    //Sends an appmessage to the pebble with the quote data
    Pebble.sendAppMessage(data, function(e) {
        console.log('Sent config data to Pebble');
    }, function() {
        console.log('Failed to send config data!');
    });
}