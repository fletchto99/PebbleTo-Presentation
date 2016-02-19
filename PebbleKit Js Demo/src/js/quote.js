module.exports.retrieveQuote = function(success, fail) {
    xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function() {
        if (xhr.readyState == 4 && xhr.status == 200) {
            try {
                var response = JSON.parse(xhr.responseText);
                success(response.quote, response.author);
            } catch(e) {
                fail();
            }
        } else if (xhr.readyState == 4) {
            fail();
        }
    };
    xhr.open("GET", "https://fletchto99.com/other/pebble/pebbleto/quote.json", true);
    xhr.send();
};