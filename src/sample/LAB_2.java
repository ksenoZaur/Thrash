package sample;

import javafx.scene.image.Image;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;

interface ISendForRecycling {
    boolean send();
}

interface ISendToBoilerRoom {
    boolean getEnergy();
}

interface IGenerator {
    Double burn ( RecyclableMaterials oil );
    IGenerator clone();
    Image getImage();
    void setPower( int amount );
    void usePower( int amount );
}

// Классы делегаты
class ForRepeatedUse implements ISendForRecycling {
    public boolean send() {
        System.out.println("переработка сырья");
        return true;
    }
}

class NotForRepeatedUse implements ISendForRecycling {
    public boolean send() {
        System.out.println("сырье не переработано");
        return false;
    }
}

class ForGenerateEnergy implements ISendToBoilerRoom {
    public boolean getEnergy(){
        System.out.println("сырье сгенерировало энергию.");
        return true;
    }
}

class NotForGenerateEnergy implements ISendToBoilerRoom {
    public boolean getEnergy(){
        System.out.println("сырье невозможно использовать в целях получения энергии.");
        return false;
    }
}

// Нагреватель
class Boiler implements IGenerator, Publisher {
    // Fields
    private Image image;
    private Double kpd;
    private int power;
    private Observer myObserver;

    // Methods
    public Boiler(){
        try {
            File file = new File("src/resource/boiler.png");
            String localUrl = file.toURI().toURL().toString();
            this.image = new Image( localUrl );
            this.kpd = 1.0;
            this.power = 0;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public Boiler( Boiler prototyp ){
        this.image = prototyp.image;
        this.kpd = prototyp.kpd;
    }

    @Override
    public Image getImage(){
        return this.image;
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
            System.out.println( "Выделено " + ((ITrash)oil).getVolumeOfEnergy() * this.kpd + " калорий.");
            this.setPower( 3 );
            return ((ITrash)oil).getVolumeOfEnergy() * this.kpd;
        }
        return 0.0;
    }

    @Override
    public IGenerator clone() {
        return new Boiler( this );
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

// Прокси
class ProtectedProxyBoiler implements IGenerator {
    // Fields
    private Boiler realBoiler;
    private int power;

    public ProtectedProxyBoiler(ProtectedProxyBoiler protectedProxyBoiler) {
        this.realBoiler = (Boiler)protectedProxyBoiler.realBoiler.clone();
    }

    // Methods
    @Override
    public Double burn(RecyclableMaterials oil ){
        if( oil == null ){
            System.out.println("Сырье не поступило.");
            return 0.0;
        }else {
            if (this.realBoiler == null) {
                this.realBoiler = new Boiler();
            }
            this.setPower( 3 );
            return this.realBoiler.burn( oil );
        }
    }

    @Override
    public IGenerator clone() {
        return new ProtectedProxyBoiler( this );
    }

    @Override
    public Image getImage() {
        return this.realBoiler.getImage();
    }

    @Override
    public void setPower(int amount) {
        this.power += amount;
        System.out.println("Current power = " + this.power);
    }

    @Override
    public void usePower(int amount) {
        this.power = Math.max(0, this.power - amount );
    }
}

// Класс от которого наследуются остальные
abstract class RecyclableMaterials{
    // Fields
    protected ISendToBoilerRoom getEnergy;
    protected ISendForRecycling repeatedUse;
    protected Boolean motion;
    protected Double coordX;
    protected Double coordY;
    protected RepeatablePart repeatablePart;
    protected Engine engine;
    protected boolean isClear;

    // Methods
    public ArrayList<Double> getCoord(){
        ArrayList<Double> coord = new ArrayList<>();
        coord.add( this.coordX );
        coord.add( this.coordY );
        return coord;
    }

    public void setCoord(Double x, Double y){this.coordX = x; this.coordY = y;}

    public Image getImage(){
        return this.repeatablePart.getImage();
//        return this.image;
    };

    boolean performGenerate(){
        return this.getEnergy.getEnergy();
    }

    boolean performReuse(){
        return this.repeatedUse.send();
    }

    public String sayType(){
        return this.repeatablePart.getType();
//        return "Unknown";
    }

    public  Boolean getMotion(){return this.motion;}

    public RecyclableMaterials(){
        this.motion = false;
        this.coordX = 400.;
        this.coordY = 0.;
        this.engine = new EngineLine(12);
    }

    public Engine getEngine(){
        return this.engine;
    }

    public Double getCoordX(){
        return this.coordX;
    }

    public void setCoordX( Double coordX ){
        this.coordX = coordX;
    }

    public Double getCoordY(){
        return this.coordY;
    }

    public void setCoordY( Double coordY ){
        this.coordY = coordY;
    }

    public void moveLeft(){
        HashMap<String, Double> result = this.engine.nextLeft( this.coordX, this.coordY);
        this.coordX = result.get("x");
        this.coordY = result.get("y");
    }

    public void moveRight(){
        HashMap<String, Double> result = this.engine.nextRight( this.coordX, this.coordY);
        this.coordX = result.get("x");
        this.coordY = result.get("y");
    }

    public void setEngine( Engine newEngine ){
        this.engine = newEngine;
    }

    abstract public void clear();

    abstract public void accept( Visitor visitor );
}

// Конкретный класс "Стекло",обьекты которого можно переработать
class Glass extends RecyclableMaterials implements ITrash{
    // Methods
    public Glass(){
        this.getEnergy = new NotForGenerateEnergy();
        this.repeatedUse = new ForRepeatedUse();
        this.repeatablePart = FactoryRepeatablePart.getRepeatablePart("Glass","src/resource/dirtyBrokenGlass.png");
    }

    @Override
    public void clear() {
        this.motion = false;
        this.coordX = 400.;
        this.coordY = 0.;
        this.engine = new EngineLine(12);
        this.repeatablePart = FactoryRepeatablePart.getRepeatablePart("Glass","src/resource/dirtyBrokenGlass.png");
        this.isClear = false;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitGlass( this );
    }

    @Override
    public Double getVolumeOfEnergy() {
        return 0.;
    }

    @Override
    public Double getWeight() {
        return 20.;
    }

    @Override
    public ArrayList<ITrash> getChildren() {
        return null;
    }
}

// Конкретный класс "Бумага", который можно использовать как источник энергии
class Paper extends RecyclableMaterials implements ITrash{
    public Paper(){
        this.getEnergy = new ForGenerateEnergy();
        this.repeatedUse = new ForRepeatedUse();
        this.repeatablePart = FactoryRepeatablePart.getRepeatablePart("Paper","src/resource/dirtyPaper.png");
    }

    @Override
    public void clear() {
        this.motion = false;
        this.coordX = 400.;
        this.coordY = 0.;
        this.engine = new EngineLine(12);
        this.repeatablePart = FactoryRepeatablePart.getRepeatablePart("Paper","src/resource/dirtyPaper.png");
        this.isClear = false;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitPaper( this );
    }

    @Override
    public Double getVolumeOfEnergy() {
        return 5.;
    }

    @Override
    public Double getWeight() {
        return 0.1;
    }

    @Override
    public ArrayList<ITrash> getChildren() {
        return null;
    }
}

// Конкретный класс "Эпоксидный пластик", который можно переработать
class PolyesterPlastic extends RecyclableMaterials implements ITrash{
    PolyesterPlastic(){
        this.getEnergy = new NotForGenerateEnergy();
        this.repeatedUse = new ForRepeatedUse();
        this.repeatablePart =
                FactoryRepeatablePart.getRepeatablePart("PolyesterPlastic","src/resource/dirtyPlasticWeste2.png");
    }

    @Override
    public void clear() {
        this.motion = false;
        this.coordX = 400.;
        this.coordY = 0.;
        this.engine = new EngineLine(12);
        this.repeatablePart =
                FactoryRepeatablePart.getRepeatablePart("PolyesterPlastic","src/resource/dirtyPlasticWeste2.png");
        this.isClear = false;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitPolyesterPlastic( this );
    }

    @Override
    public Double getVolumeOfEnergy() {
        return 0.;
    }
    @Override
    public Double getWeight() {
        return 15.;
    }

    @Override
    public ArrayList<ITrash> getChildren() {
        return null;
    }
}

// Конкретный класс "Биологические отходы", который можно использовать только как источник энергии
class Organic extends RecyclableMaterials implements ITrash{
    public Organic(){
        this.getEnergy = new ForGenerateEnergy();
        this.repeatedUse = new NotForRepeatedUse();
        this.repeatablePart = FactoryRepeatablePart.getRepeatablePart("Organic","src/resource/dirtyOrganic.png");
    }

    @Override
    public void clear() {
        this.motion = false;
        this.coordX = 400.;
        this.coordY = 0.;
        this.engine = new EngineLine(12);
        this.repeatablePart =
                FactoryRepeatablePart.getRepeatablePart("Organic","src/resource/dirtyOrganic.png");
        this.isClear = false;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitOrganic( this );
    }

    @Override
    public Double getVolumeOfEnergy() {
        return 10.;
    }

    @Override
    public Double getWeight() {
        return 25.;
    }

    @Override
    public ArrayList<ITrash> getChildren() {
        return null;
    }
}

