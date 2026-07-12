/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package network;

import java.io.Serializable;

/**
 *
 * @author Lenovo
 */
public class Request implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    private String module, action;
    private Object data;
    public Request()
    {
        
    }
    
    public Request(String module, String action, Object data)
    {
        this.module=module;
        this.action=action;
        this.data=data;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
    
    @Override
    public String toString()
    {
        return "Request{module=" + module + ", action=" + action + ", data=" + data + "}";
    }
}
