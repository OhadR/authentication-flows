<html>
<head>
	<title>Account Created Successfully</title>
</head>

<body>
	<h3>Account Created Successfully</h3>
			<div style="margin-top:  25px ;position: relative; font:15px">
					An activation message was sent to <span style="font-weight:bold"><%= request.getParameter("email") %></span><br>
					Please follow the instructions in the message to complete the authentication process. 
			</div>

</body>
</html>