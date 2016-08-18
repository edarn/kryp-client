<%@ Page Language="C#" AutoEventWireup="true" Inherits="Download" Title="Vindinfo::download" Codebehind="Download.aspx.cs" %>
    <br /><br /><br />
    <br /><br /><br />
    <h3>Download section: </h3> <br />
    <a href='./DownloadAs.aspx?file=Windmaster-productsheet.pdf'>Wind master product sheet</a> <br />
    <a href='./DownloadAs.aspx?file=Windmaster-guide.pdf'>Wind master guide</a> <br />

<script src="https://www.gstatic.com/firebasejs/3.2.1/firebase.js"></script>
<script>
  // Initialize Firebase
  var config = {
    apiKey: "AIzaSyAQ93BuMzxnGt_rFR01xTVhHo0TQTBEp1E",
    authDomain: "surfvind.firebaseapp.com",
    databaseURL: "https://surfvind.firebaseio.com",
    storageBucket: "surfvind.appspot.com",
  };
  firebase.initializeApp(config);
</script>


