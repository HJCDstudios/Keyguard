app.CreateKeyguard = function() { return new Keyguard();}
app.EnableScreenCapture = function(boolean) { return new Keyguard().EnableScreenCapture(boolean); };
function Keyguard() {
  this.plg = _CreatePlugin("com.hjcd.keyguard.plugins.user.Keyguard");
  this.Authenticate = function(title,description,OnSuccess,OnFail) {
    this.plg.Send("Authenticate",title,description,OnSuccess,OnFail);
  };
  this.InKeyguardRestrictedInputMode = function() {
    return this._CheckAnswer(this.plg.Send("InKeyguardRestrictedInputMode"));
  };
  this.IsDeviceLocked = function() {
    return this._CheckAnswer(this.plg.Send("IsDeviceLocked"));
  };
  this.IsKeyguardLocked = function() {
    return this._CheckAnswer(this.plg.Send("IsKeyguardLocked"));
  };
  this.IsKeyguardSecure = function() {
    return this._CheckAnswer(this.plg.Send("IsKeyguardSecure"));
  };
  this.EnableScreenCapture = function(boolean) {
    this.plg.Send("EnableScreenCapture",""+boolean);
  };
  this._CheckAnswer = function(d) {
    if (d.startsWith("%E%")) {
      d = d.slice(d.indexOf("%E%")+3,d.length).split("%$%");
      app.Error(d[0],d[1],0,true);
    } else {
      return JSON.parse(d);
    }
  };
}
