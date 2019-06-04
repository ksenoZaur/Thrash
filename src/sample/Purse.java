package sample;

import javafx.scene.control.Button;
import javafx.scene.image.Image;

import java.io.*;
import java.util.ArrayList;

public class Purse {
    // Fields
    private int total;

    // Methods
    public Purse(){
        this.total = 0;
    }

    public void add( int amount ){
        this.total += amount;
    }

    public boolean takeOff( int amount ){
        if( this.total < amount ){
            System.out.println("Недостаточно средств!");
            return false;
        } else {
            this.total -= amount;
            return true;
        }
    }

    public  int getTotal(){
        return this.total;
    }
}

interface Command{
    void execute();
}

class ChangeButtonState implements  Command{
    // Fields
    Button concreteButton;

    // Methods
    public ChangeButtonState( Button concreteButton ){
        this.concreteButton = concreteButton;
    }

    @Override
    public void execute() {
        this.concreteButton.setDisable( !this.concreteButton.isDisable() );
    }
}

interface Visitor{
    void visitGlass( Glass component );
    void visitOrganic( Organic component );
    void visitPaper( Paper component );
    void visitPolyesterPlastic( PolyesterPlastic component );
    void visitEpoxyPlastic( EpoxyPlastic component );
}

class Cleaning implements Visitor{

    @Override
    public void visitGlass(Glass component) {
        if( !component.isClear ) {
            component.isClear = true;
            component.repeatablePart = FactoryRepeatablePart.getRepeatablePart("Glass", "src/resource/brokenGlass.png");
        }
    }

    @Override
    public void visitOrganic(Organic component) {
        if( !component.isClear ) {
            component.isClear = true;
            component.repeatablePart = FactoryRepeatablePart.getRepeatablePart("Organic", "src/resource/organic.png");
        }
    }

    @Override
    public void visitPaper(Paper component) {
        if( !component.isClear ) {
            component.repeatablePart = FactoryRepeatablePart.getRepeatablePart("Paper", "src/resource/paper.png");
        }
    }

    @Override
    public void visitPolyesterPlastic(PolyesterPlastic component) {
        if( !component.isClear ) {
            component.isClear = true;
            component.repeatablePart =
                    FactoryRepeatablePart.getRepeatablePart("PolyesterPlastic", "src/resource/plasticWeste2.png");
        }
    }

    @Override
    public void visitEpoxyPlastic(EpoxyPlastic component) {
        if( !component.isClear ) {
            component.repeatablePart =
                    FactoryRepeatablePart.getRepeatablePart("EpoxyPlastic", "src/resource/plasticWeste.png");
        }

    }
}

// Выгружает в файл
class Unloading{
    // Fields
    Serializable object;
    FileOutputStream fos;
    FileInputStream fis;
    ObjectOutputStream oos;
    ObjectInputStream oin;
    ArrayList<Product> goods;
    String name;

    // Methods
    public Unloading(Serializable object, ArrayList<Product> goods, String name) {
        this.object = object;
        this.goods = new ArrayList<>( goods );
        this.name = name;
//        this.controller = controller;
    }

    public Unloading(Serializable object, String name) {
        this.object = object;
        this.name = name;
//        this.controller = controller;
    }

    public Unloading(String name) {
        this.name = name;
    }

    public boolean unloading(){
        boolean success = true;
        try {
            this.fos = new FileOutputStream(this.name);
            this.oos = new ObjectOutputStream(fos);
            this.oos.writeObject( this.object );
            this.oos.flush();
            this.oos.close();
        } catch (IOException e) {
            e.printStackTrace();
            success = false;
        }
        return success;
    }

    public  Object loading(){
        Object object = null;
        try {
            this.fis = new FileInputStream( this.name );
            this.oin = new ObjectInputStream( this.fis );
            object =  this.oin.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return object;
    }

    public ArrayList<Product> getGoods(){
        return this.goods;
    }

    public void setObject( Serializable object, ArrayList<Product> goods ){
        this.object = object;
        this.goods = new ArrayList<>( goods );
    }

    public void setObject( Serializable object ){
        this.object = object;
    }
}