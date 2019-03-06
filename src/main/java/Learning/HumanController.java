package Learning;

import Model.Elements.Agent;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;

public class HumanController implements RLController, KeyListener, Serializable {
    KeyEvent keyEvent;
    public JPanel simulationPanel;
    public HumanController(){
    }

    @Override
    public void pickAction(Agent a) {
        simulationPanel.requestFocus();
        while(keyEvent == null){
            try {
                Thread.sleep(Math.abs(100));
            } catch (java.lang.InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }

        switch (keyEvent.getKeyCode()){
            case KeyEvent.VK_UP:
                a.moveUp();
                break;
            case KeyEvent.VK_DOWN:
                a.moveDown();
                break;
            case KeyEvent.VK_LEFT:
                a.moveLeft();
                break;
            case KeyEvent.VK_RIGHT:
                a.moveRight();
                break;
            case KeyEvent.VK_SPACE:
                a.makeDirt();
                break;
            case KeyEvent.VK_ENTER:
                a.doNothing();
                break;
            default:
                keyPressed(keyEvent);

        }

        keyEvent = null;


/*

        System.out.println("Which of the actions would you like to do? ");
        Scanner input_scanner = new Scanner(System.in);
        String choice = input_scanner.nextLine();
        for(String s : a.possibleActions()){
            System.out.println("Choice = " + choice);
            if(s.equals(choice)){
                switch(s){
                    case "Cut Grass":
                        System.out.println("Cutting grass");
                        a.makeDirt();
                        break;
                    case "Go Down":
                        a.moveDown();
                        break;
                    case "Go Up":
                        a.moveUp();
                        break;
                    case "Go Left":
                        a.moveLeft();
                        break;
                    case "Go Right":
                        a.moveRight();
                        break;
                    case "Do Nothing":
                        a.doNothing();
                        default:
                            a.doNothing();
                }
            }
        }
        */

    }

    @Override
    public void keyTyped(KeyEvent keyEvent) {

    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        System.out.println("KEYPRESSED");
        this.keyEvent = keyEvent;
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {

    }
}
