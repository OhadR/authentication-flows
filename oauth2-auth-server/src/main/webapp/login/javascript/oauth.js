var backend_url = '../createAccountPage';
var SOLVE 	= 'Solve!';
var BACK	= 'Back';

function InitCreateAccount()
{
	getPasswordPolicy();
}

function getPolicy()
{
	if(buttonText == SOLVE)
	{
		var puzzle = getTableAsJson();

		$.ajax({
			url : backend_url,
			type: 'GET',
			data: 
			{
				puzzle: puzzle,
				command: 'solve'
			},
			dataType: "json",
			success: function(response)
			{
				populateResult(response);
			}
		});

		document.getElementById('solve_and_reset').value = BACK;

	}
	else if(buttonText == BACK)
	{
		//clear the puzzle and set again the button the "Solve!":
		var x, y;
		for(x=0; x<9; ++x)
		{
			for(y=0; y<9; ++y)
			{
				var xStr = x+''; 
				var yStr = y+'';
				var indexStr = xStr + yStr;
				var element = document.getElementById('_' + indexStr);
				element.value = '';
			}
		}

		setButtonToSolve();
	}


}

function populateResult(response)
{
//	$('#status').html( response.message );
	if (response.indexOf("OK|") == -1)
		return;
}


this.InitPassReq = function()
{
   if (m_PasswordMaxLength != 100)
      return;
   var PasswordMaxLength=100;
   var PasswordMinLength=0;
   var PasswordMinLoCaseLetters=0;
   var PasswordMinNumbers=0;
   var PasswordMinSpecialSymbols=0;
   var PasswordMinUpCaseLetters=0;

   try
   { 
      eval (self.GetPassInstructions().replace(/\|/g,";"));    

      m_PasswordMaxLength=PasswordMaxLength;
      m_PasswordMinLength=PasswordMinLength;
      m_PasswordMinLoCaseLetters=PasswordMinLoCaseLetters;
      m_PasswordMinNumbers=PasswordMinNumbers;
      m_PasswordMinSpecialSymbols=PasswordMinSpecialSymbols;
      m_PasswordMinUpCaseLetters=PasswordMinUpCaseLetters;
    } catch (e) {}
}


this.GetPassInstructions = function()
{
   if (m_szPassInstructions == "")
      self.InitQuestionsAndPasswordInstructions();
   return m_szPassInstructions;
}



this.InitQuestionsAndPasswordInstructions = function ()
{
   var szReqText = g_WDLogin.XMLHTTPRequest("/oauth-srv/createAccountPage" ,"");

   if (szReqText.indexOf("OK|") == -1)
      return;

   szReqText = szReqText.substring(3);

   var nPos = szReqText.indexOf("||");
   m_szQuestions = szReqText.substring(0,nPos);
   m_szPassInstructions = szReqText.substring(nPos+2);
}

function getPasswordPolicy()
{
	$.ajax({
		url : backend_url,
		type: 'GET',
//		dataType: "json",
		success: function(response)
		{
			populateResult(response);
		}
	});
}
