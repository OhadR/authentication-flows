var get_policy_backend_url = 		'../createAccountPage';

function InitCreateAccount()
{
	getPasswordPolicy();
}


function getPasswordPolicy()
{
	//AJAX call to get the password policy:
	$.ajax({
		url : get_policy_backend_url,
		type: 'GET',
//		dataType: "json",
		success: function(response)
		{
			populateResult(response);
		}
	});
}


function populateResult(response)
{
	if (response.indexOf("OK|") == -1)
		return;
}

//////////////////////////////////////////////////////////////////////////////////////


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

