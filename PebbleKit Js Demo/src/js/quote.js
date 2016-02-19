//Export the function
module.exports.retrieveQuote = function(success, fail) {
    // create the XML HTTP Request
    var xhr = new XMLHttpRequest();

    //Listen for a response
    xhr.onreadystatechange = function() {
        if (xhr.readyState == 4 && xhr.status == 200) {
            try {
                var response = JSON.parse(xhr.responseText);
                success(response.contents.quotes[0].quote, response.contents.quotes[0].author);
            } catch(e) {
                fail();
            }
        } else if (xhr.readyState == 4) {
            fail();
        }
    };

    //Send the request
    xhr.open("GET", "http://quotes.rest/qod.json", true);
    xhr.send();
};