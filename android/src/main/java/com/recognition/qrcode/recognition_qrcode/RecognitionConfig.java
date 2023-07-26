package com.recognition.qrcode.recognition_qrcode;

import java.util.Map;

public class RecognitionConfig {
    private static  RecognitionConfig instance = new RecognitionConfig();
    public double iconHeight;
    public double iconWidth;
    public double cancelTitleFontSize;
    public String cancelTitle;
    public byte[] icon;
    private RecognitionConfig(){

    }
    public static RecognitionConfig getInstance(){
        return instance;
    }
    public void setConfig(Map config){
        if(config.get("iconWidth") != null){
            this.iconWidth = (double)config.get("iconWidth");
        }
        if(config.get("iconHeight")!= null){
            this.iconHeight = (double)config.get("iconHeight");
        }
        if(config.get("cancelTitleFontSize") != null){
            this.cancelTitleFontSize = (double)config.get("cancelTitleFontSize");
        }
        if(config.get("cancelTitle") != null){
            this.cancelTitle = (String)config.get("cancelTitle");
        }
//    if([config[@"backgroundColor"] isKindOfClass:[NSString class]]){
//        self.backgroundColor = config[@"backgroundColor"];
//    }
        if(config.get("icon") != null){
            this.icon = (byte[])config.get("icon");
        }
    }
}
