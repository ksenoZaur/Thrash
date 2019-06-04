package sample;
// Требуется применить паттерны:
// Мост+
// Легковес+
// Фасад+
// Информационный эксперт+

import javafx.scene.control.TextArea;
import javafx.scene.image.Image;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;

// Легковес
class RepeatablePart{
    // Fields
    private String type;
    private Image image;

    // Methods
    public RepeatablePart( String type, String imagePath ){
        try {
            this.type = type;
            File file = new File(imagePath);
            String localUrl = file.toURI().toURL().toString();
            this.image = new Image( localUrl );
        }catch ( MalformedURLException ex ){
            ex.printStackTrace();
        }
    }

    public Image getImage() {
        return this.image;
    }

    public String getType(){ return this.type; }
}

class FactoryRepeatablePart{
    static private HashMap<String,RepeatablePart> collectionOfRP = new HashMap<>();
    static public RepeatablePart getRepeatablePart(String type, String imagePath){
        String key = type.concat( "_" + imagePath );
        RepeatablePart result;
        result = collectionOfRP.get( key );
        if( result == null ){
            result = new RepeatablePart(type, imagePath );
            collectionOfRP.put(key, result);
        }
        return result;
    }
}

// Фасад
class FacadeFactory {
    // Fields
    private Conveyor startConveyor;
    private Controller controller;
    private IGenerator startBoiler;

    public static final int  UPGRADE_SHREDDER  = 1;
    public static final int  UPGRADE_SORTING  = 2;
    public static final int  BOILER  = 3;
    public static final int  CONVEYOR  = 4;

    // Methods

    public void selectPartConveyor( Conveyor conveyor ){
        this.startConveyor = conveyor;
    }

    public void  selectPartBoiler( IGenerator generator ){
        this.startBoiler = generator;
    }

    public FacadeFactory(){
//        this.controller = controller;
        this.startConveyor = new ConveyorLine();
        this.startBoiler = new Boiler();
//        ((Boiler) this.startBoiler).attach( this.controller );
    }

    public void clear(){
        this.startBoiler = new Boiler();
        this.startConveyor = new ConveyorLine();
    }

    public void upgrade( int addition ){
        switch ( addition ){
            case UPGRADE_SHREDDER :{ this.startConveyor = new Shredder(this.startConveyor); } break;
            case UPGRADE_SORTING :{ this.startConveyor = new Sorting(this.startConveyor); } break;
            default: {System.out.println("Сгенерировать исключение неверный тип");}
        }
    }

    public Product sendToFactory( RecyclableMaterials material, int equipment){
        switch ( equipment ){
            case BOILER :{
                this.startConveyor.setPower( this.startBoiler.burn( material ) );
                return null;
            }
            case CONVEYOR :{
                if( this.startConveyor.isWorked() ){
                    this.startBoiler.usePower( 1 );
                    return this.startConveyor.recycling( material );
                } else {
                    return null;
                }
            }
            default: {
                System.out.println("Сгенерировать исключение неверный тип");
                return null;
            }
        }
    }

    public Image getImageBoiler( ){
        return this.startBoiler.getImage();
    }

    public ArrayList<Image> getImageConveyor( ){
        return this.startConveyor.getImage();
    }

    public void changeBoiler( Controller controller) {
        System.out.println(this.startBoiler.getClass() + " " + Boiler.class);
        if( this.startBoiler.getClass() == Boiler.class){
            ConvectionOven conOven = new ConvectionOven();
            this.startBoiler = conOven.clone();
            ((ConvectionOven)this.startBoiler).attach( controller );
            this.startBoiler.setPower(2);
        } else {
            Boiler conOven = new Boiler();
            this.startBoiler = conOven.clone();
            ((Boiler)this.startBoiler).attach( controller );
            this.startBoiler.setPower(3);
        }


    }
}

// Мост
// Идея такая: выносим перемещение картинок в отдельную иерархию. В реализации создаем разные конкретные виды перемещения по прямой,
// с выравниваем, по синусойде.
abstract class Engine {

    // Field
//    protected RecyclableMaterials material;
    protected Integer speed;

    // Methods
    public Engine(Integer speed){
        this.speed = speed;
    }
    public abstract HashMap<String, Double> nextRight(Double xOld, Double yOld);
    public abstract HashMap<String, Double> nextLeft(Double xOld, Double yOld);
}

class EngineLine extends Engine {
    public EngineLine(Integer speed) {
        super(speed);
    }

    @Override
    public HashMap<String, Double> nextRight(Double xOld, Double yOld) {
        HashMap<String, Double> newCoord = new HashMap<>();
        newCoord.put("x", xOld + this.speed );
        newCoord.put("y", yOld );
        return newCoord;
    }

    @Override
    public HashMap<String, Double> nextLeft(Double xOld, Double yOld) {
        HashMap<String, Double> newCoord = new HashMap<>();
        newCoord.put("x", xOld - this.speed );
        newCoord.put("y", yOld );
        return newCoord;
    }
}

class EngineSin extends Engine {
    private Double angle;

    public EngineSin(Integer speed) {
        super(speed);
        this.angle = 0.;
    }

    @Override
    public HashMap<String, Double> nextRight( Double xOld, Double yOld) {
        HashMap<String, Double> newCoord = new HashMap<>();
        newCoord.put("x", xOld + this.speed * Math.abs( Math.cos(this.angle)));
        newCoord.put("y", yOld + this.speed *  Math.sin(this.angle));
        this.angle += Math.PI / 12;
        return newCoord;
    }

    @Override
    public HashMap<String, Double> nextLeft( Double xOld, Double yOld) {
        HashMap<String, Double> newCoord = new HashMap<>();
        newCoord.put("x", xOld - this.speed * Math.abs( Math.cos(this.angle)));
        newCoord.put("y", yOld + this.speed *  Math.sin(this.angle));
        this.angle += Math.PI / 12;
        return newCoord;
    }
}

class EngineLine2 extends Engine {
    public EngineLine2(Integer speed) {
        super(speed);
    }

    @Override
    public HashMap<String, Double> nextRight( Double xOld, Double yOld ) {
        HashMap<String, Double> newCoord = new HashMap<>();
        if( yOld <= 0 ) {
            newCoord.put("x", xOld + this.speed);
            newCoord.put("y", yOld );
        } else {
            newCoord.put("x", xOld);
            newCoord.put("y", yOld  - this.speed);
        }
        return newCoord;
    }

    @Override
    public HashMap<String, Double> nextLeft( Double xOld, Double yOld ) {
        HashMap<String, Double> newCoord = new HashMap<>();
        if( yOld <= 0 ) {
            newCoord.put("x", xOld - this.speed);
            newCoord.put("y", yOld );
        } else {
            newCoord.put("x", xOld);
            newCoord.put("y", yOld  - this.speed);
        }
        return newCoord;
    }
}

public class LAB_4 {
}
