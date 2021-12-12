package Subscriber;

import java.util.concurrent.TimeUnit;

public class InputFromKeyBoard {
    public String inputFromKeyBoard = "";
    public int isEndText = 0;
    public String middle = "";

    public void setIsEndText(int i) {
        isEndText = i;
    }

    public int getIsEndText() {
        return isEndText;
    }

    public void setInputFromKeyBoard(String s) {
        inputFromKeyBoard = s;
    }

    public String getInputFromKeyBoard() {
        return inputFromKeyBoard;
    }

    public String getStringFromKeyBoard() {
        try {
            while(true) {
                TimeUnit.MILLISECONDS.sleep(10);
                if(isEndText == 1) {
                    isEndText = 0;
                    middle = inputFromKeyBoard;
                    inputFromKeyBoard = "";
                    return middle;
                }
            }
        } catch (InterruptedException e) {
            System.err.println(e);
            return "";
        }
    }
}
