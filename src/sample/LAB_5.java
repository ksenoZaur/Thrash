package sample;

import javafx.scene.control.TextArea;
import javafx.scene.image.Image;

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayList;

import static sample.Controller.SPEED;
// Простая фабрика
class SimpleFactory{
    public RecyclableMaterials createMaterial( String name ){
        if( name.equals(Glass.class.getName()) ){
            return new Glass();
        }
        if( name.equals(Paper.class.getName()) ){
            return new Paper();
        }
        if( name.equals(Organic.class.getName())){
            return new Organic();
        }
        if( name.equals(PolyesterPlastic.class.getName())){
            return new PolyesterPlastic();
        }
        if( name.equals(EpoxyPlastic.class.getName())){
            return new EpoxyPlastic();
        }
        System.out.println("Неопознанный тип.");
        return null;
    }
}

// Фабричный метод
abstract class FabricMethod{
    public abstract Engine createEngine();
}

class FabricELine extends  FabricMethod{
    @Override
    public Engine createEngine() {
        return new EngineLine(SPEED);
    }
}

class FabricESin extends FabricMethod{
    @Override
    public Engine createEngine() {
        return new EngineSin(SPEED);
    }
}

class FabricELine2 extends FabricMethod {
    @Override
    public Engine createEngine() {
        return new EngineLine2(SPEED);
    }
}

interface Product{
    // Methods
    public Double getPrice();
    public Image getImage();
}

class PaperProduct implements Serializable, Product{
    // Fields
    protected Double price;
    protected Image image;

    //Methods
    public Double getPrice(){
        return this.price;
    }
    public Image getImage(){return this.image;};
}

class GlassProduct implements Serializable, Product{
    // Fields
    protected Double price;
    protected Image image;
    //Methods
    public Double getPrice(){return this.price;};
    public Image getImage(){return this.image;};
}

class PlasticProduct implements Serializable, Product{
    // Fields
    protected Double price;
    Image image;
    //Methods
    public Double getPrice(){return this.price;}
    public Image getImage(){return this.image;}
}

class PaperBottle extends PaperProduct{
    public PaperBottle(){
        this.price = 1.0;
        try {
            File file = new File("src/resource/Bottle/paper.png");
            String localUrl = file.toURI().toURL().toString();
            this.image = new Image( localUrl );
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}

class GlassBottle extends GlassProduct{
    public GlassBottle(){
        this.price = 2.0;
        try {
            File file = new File("src/resource/Bottle/glass.png");
            String localUrl = file.toURI().toURL().toString();
            this.image = new Image( localUrl );
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}

class PlasticBottle  extends PlasticProduct{
    public PlasticBottle(){
        this.price = 1.0;
        try {
            File file = new File("src/resource/Bottle/plastic.png");
            String localUrl = file.toURI().toURL().toString();
            this.image = new Image( localUrl );
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}

class PaperBox extends PaperProduct{
    public PaperBox(){
        this.price = 10.0;
        try {
            File file = new File("src/resource/Box/paper.png");
            String localUrl = file.toURI().toURL().toString();
            this.image = new Image( localUrl );
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}

class GlassBox extends GlassProduct{
    public GlassBox(){
        this.price = 20.0;
        try {
            File file = new File("src/resource/Box/glass.png");
            String localUrl = file.toURI().toURL().toString();
            this.image = new Image( localUrl );
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}

class PlasticBox extends PlasticProduct{
    public PlasticBox(){
        this.price = 30.0;
        try {
            File file = new File("src/resource/Box/plastic.png");
            String localUrl = file.toURI().toURL().toString();
            this.image = new Image( localUrl );
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}

abstract class AbstractFactory{
    public abstract Product createProduct( RecyclableMaterials material);
}

class FactoryBottle extends AbstractFactory{
    @Override
    public Product createProduct(RecyclableMaterials material) {
        if( material.sayType().contains("Glass")){
            return new GlassBottle();
        }
        if( material.sayType().contains("Paper")){
            return new PaperBottle();
        }
        if( material.sayType().contains("Plastic")){
            return new PlasticBottle();
        }
        System.out.println("Неопознанный тип.");
        return null;
    }
}

class FactoryBox extends AbstractFactory{

    @Override
    public Product createProduct(RecyclableMaterials material) {
        if( material.sayType().contains("Glass")){
            return new GlassBox();
        }
        if( material.sayType().contains("Paper")){
            return new PaperBox();
        }
        if( material.sayType().contains("Plastic")){
            return new PlasticBox();
        }
        System.out.println("Неопознанный тип.");
        return null;
    }
}

// Invoker
class Storehouse{
    // Fields
    private Integer maxSize;
    private ArrayList<Product> goods;
    private StateStorehouse stateStorehouse;
    private Controller controller;

    private Command changeButtonState;

    // Methods
    public void setController(Controller controller){
        this.controller = controller;
    }

    public Controller getController(){
        return this.controller;
    }

    public Storehouse( Command currentCommand ){
        this.goods = new ArrayList<>();
        this.maxSize = 4;
        this.setState( new StateNotFullStorehouse() );
        this.changeButtonState = currentCommand;
    }

    public void setCommand( Command currentCommand ){
        this.changeButtonState = currentCommand;
    }

    public int getMaxSize(){
        return this.maxSize;
    }

    public Boolean isEmpty(){
        return goods.isEmpty();
    }

    private Boolean isFull(){
        if( this.goods.size() == maxSize ){
            return true;
        }
        return false;
    }

    public Boolean add( Product newGoods ){
        boolean change = false;
        if( this.isEmpty() ){
            change = true;
         }
         boolean result = this.stateStorehouse.add( newGoods, this);
         // Пример вызова метода комманды
         if( result && change ){
             this.changeButtonState.execute();
         }
        return result;
    }

    public Double sellAll(){
        // Пример вызова метода комманды 2
        this.changeButtonState.execute();
        return this.stateStorehouse.sellAll( this );
    }

    public ArrayList<Product> getGoods() {
        return this.goods;
    }

    public void setGoods( ArrayList<Product> goods ){
        this.goods = goods;
    }

    public void setState( StateStorehouse newState) {
        this.stateStorehouse = newState;
    }

    public Memento save(){
        return new Memento(this.maxSize, this.goods, this.stateStorehouse, this.controller);
    }

    public void restore( Memento snapshot ){
        this.maxSize = snapshot.getMaxSize();
        try {
            this.goods = new ArrayList<>( snapshot.getGoods() );
        } catch ( NullPointerException np ){
            this.goods = null;
        }
        this.stateStorehouse = snapshot.getStateStorehouse();
        try{
            this.controller = snapshot.getController();
        } catch ( NullPointerException np ){
            this.controller = null;
        }
        this.changeButtonState.execute();
    }

}

class ConvectionOven implements IGenerator, Publisher {
    // Fields
    private Image image;
    private Double kpd;
    private static final Double TRANS =  4.184;
    private int power;
    private Observer myObserver;

    // Methods
    public ConvectionOven(){
        try {
            File file = new File("src/resource/boiler2.png");
            String localUrl = file.toURI().toURL().toString();
            this.image = new Image( localUrl );
            this.kpd = 1.5;
            this.power = 0;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public ConvectionOven(ConvectionOven convectionOven) {
        this.image = convectionOven.image;
        this.kpd = convectionOven.kpd;
    }

    public Image getImage() {
        return image;
    }

    @Override
    public void setPower(int amount) {
        this.power += amount;
        System.out.println("Current power = " + this.power);
        notifyAbout();
    }

    @Override
    public void usePower(int amount) {
        this.power = Math.max(0, this.power - amount );
        System.out.println("Current power = " + this.power);
        notifyAbout();
    }

    @Override
    public Double burn(RecyclableMaterials oil) {
        if( oil.performGenerate() ){
            System.out.println( "Выделено " + ((ITrash)oil).getVolumeOfEnergy() * this.kpd * TRANS + " Джоулей.");
            this.setPower( 1 );
            return ((ITrash)oil).getVolumeOfEnergy() * this.kpd;
        }
        return 0.0;
    }

    @Override
    public IGenerator clone() {
        return new ConvectionOven( this );
    }

    @Override
    public void attach(Observer observer) {
        this.myObserver = observer;
    }

    @Override
    public void detach(Observer observer) {
        if( this.myObserver == observer ){
            this.myObserver = null;
        }
    }

    @Override
    public void notifyAbout() {
        if( this.myObserver != null ){
            this.myObserver.update( this.power );
        }
    }
}
