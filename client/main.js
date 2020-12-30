
function fetchNews(name) {
    //Since we display the news items as nodes, removes the existing nodes for every category change 
    const nodes = document.getElementById("newsList");
    nodes.innerHTML = '';
    document.getElementById("loader").style.display = "block";
    var tablename = name;
    console.log(tablename);
    //Makes an API call to the Advanced I/O function to fetch the news item from a particular table 
    $.ajax({
        url: "/server/NewsApp_AIO/fetchData?tablename=" + tablename, //If you initialized the Advanced I/O function in Java, call 'NewsApp_AIO'. If you initialized the Advanced I/O function in Node.js, replace 'NewsApp_AIO' with 'news_app_function' in this line.
        type: "get",
        success: function (response) {
            var data = response;
            var i;
            document.getElementById("loader").style.display = "none";
            //Parses the response from the Advanced I/O function, and renders it in the HTML page by creating a list of nodes
            for (i = 0; i < data.content.length; i++) {
                var list = document.getElementById('newsList');
                var anchor = document.createElement('a');
                var li = document.createElement('li');
                var linebreak = document.createElement("br");
                anchor.href = data.content[i][tablename].url;
                anchor.innerText = data.content[i][tablename].title;
                anchor.setAttribute('target', '_blank');
                li.appendChild(anchor);
                list.appendChild(li);
                list.appendChild(linebreak);
            }
        }, error: function (error) {
            alert(error.data);
        }
    });
}
