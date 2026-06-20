package control;

import bean.MessageBean;
import boundary.UserInterface;
import exception.ControllerException;

// Every controller has this features

public abstract class AppController {

    // Every controller needs to know who is the graphic interface
    protected UserInterface userBoundary;

    public void setGraphicInterface(UserInterface ui){
        this.userBoundary = ui;
    }

    public void notifyController(MessageBean message){
        userBoundary.printMessage(message);
    }

    public abstract void start() throws ControllerException;
    public abstract void finish() throws ControllerException;
}
