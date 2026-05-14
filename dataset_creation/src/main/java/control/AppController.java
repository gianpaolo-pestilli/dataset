package control;

import exception.ControllerException;

public interface AppController {
    public void start() throws ControllerException;
    public void finish() throws ControllerException;
}
