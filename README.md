# VoiceMinus

### Huge thanks to VoicePlus from Cyanogenmod for the Google Voice authorization and token code. ###

This is a simple way to get Google Voice SMS working with Android Wear. You need AutoWear and Tasker to get the spoken text from the Android Wear device and act on it.

# Tasker Intent Example    
    Action: com.ihelp101.voiceminus.Voice.START      
    Extra: name:%name (Variable from AutoVoice/AutoWear)
    Package: com.ihelp101.voiceminus      

# AutoVoice/AutoWear Example (Recognized)     
    Event Behavior: Checked      
    Command Filter: Send Text Message To (?<name>.+)
    Use Regex: Checked     
