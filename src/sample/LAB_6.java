package sample;

import java.util.HashMap;

// Синглтон и Пул объектов
class ObjectPool{
    // Fields
    private static ObjectPool instance;
    private HashMap<RecyclableMaterials, Boolean> reusable;
    private SimpleFactory simpleFactory;

    // Methods
    private ObjectPool(){
        this.reusable = new HashMap<>();
        this.simpleFactory = new SimpleFactory();
    }

    RecyclableMaterials acquireMaterial( String name ){
        for( RecyclableMaterials current :this.reusable.keySet()){
            if( current.getClass().getName().equals( name ) &&
                    this.reusable.get( current )){
                this.reusable.replace(current, false);
                return current;
            }
        }
        RecyclableMaterials newMaterial = this.simpleFactory.createMaterial( name );

        if( newMaterial.getClass().getName().equals( EpoxyPlastic.class.getName())){
            newMaterial = new AdapterEpoxyPlastic( (EpoxyPlastic) newMaterial);
        }
        this.reusable.put( newMaterial, false);
        return newMaterial;
    }

    void releaseObject( RecyclableMaterials oldMaterial ){
        oldMaterial.clear();
        this.reusable.replace(oldMaterial, true);
    }

    public static ObjectPool getInstance(){
        if( instance == null ){
            System.out.println("Пул создан.");
            instance = new ObjectPool();
        } else {
            System.out.println("Пул уже был создан ранее.");
        }
        return instance;
    }

}

// Строитель
interface Builder{
    void reset();
    void buildPartBoiler( Controller controller );
    void buildConveyor();
    FacadeFactory getResult();
}

class ComunityBuilder implements Builder{
    // Fields
    private FacadeFactory factory;

    // Methods
    @Override
    public void reset() {
        this.factory = new FacadeFactory();
    }

    @Override
    public void buildPartBoiler( Controller controller ) {
        ConvectionOven boiler = new ConvectionOven();
        boiler.attach( controller );
        boiler.setPower( 2 );
        this.factory.selectPartBoiler( boiler );
    }

    @Override
    public void buildConveyor() {
        this.factory.selectPartConveyor( new ConveyorLine() );
    }

    public FacadeFactory getResult(){
        return this.factory;
    }
}

class LuxuryBuilder implements Builder{
    // Fields
    private FacadeFactory factory;

    // Methods
    @Override
    public void reset( ) {
        this.factory = new FacadeFactory();
    }

    @Override
    public void buildPartBoiler( Controller controller) {
        Boiler boiler = new Boiler();
        boiler.attach( controller );
        boiler.setPower( 3 );
        this.factory.selectPartBoiler( boiler );
    }

    @Override
    public void buildConveyor() {
        Conveyor conveyor = new ConveyorLine();
        conveyor = new Sorting( conveyor );
        conveyor = new Shredder( conveyor );
        this.factory.selectPartConveyor( conveyor );
    }

    public FacadeFactory getResult(){
        return this.factory;
    }
}

class Director{
    // Field
    Builder builder;

    // Methods
    public Director( Builder builder ){
        this.builder = builder;
    }

    public FacadeFactory construct( Controller controller){
        this.builder.reset();
        this.builder.buildConveyor();
        this.builder.buildPartBoiler( controller );
        return builder.getResult();
    }
}
