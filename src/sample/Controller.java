package sample;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;

import java.io.File;
import java.net.MalformedURLException;
import java.util.*;

// Класс для переработки материалов
interface Conveyor{
    Product recycling( RecyclableMaterials material );
    ArrayList<Image> getImage();
    void setPower( Double power);
    boolean isWorked();
}

class ConveyorLine implements Conveyor{
    // Fields
    private Image conveyorImage;
    private AbstractFactory sf2;
    private Double power;
    private boolean worked;

    // Methods
    public ConveyorLine(){
        try {
            File file = new File("src/resource/conveyorLentMini228.png");
            String localUrl = file.toURI().toURL().toString();
            this.conveyorImage = new Image( localUrl );
           // Абстрактная фабрика. Меняем чтобы получать бутылки или ящики.
//            this.sf2 = new FactoryBottle();
            this.sf2 = new FactoryBox();
            this.worked = true;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
    @Override
    public Product recycling(RecyclableMaterials material) {
        System.out.print( material.sayType() );
        System.out.print(" ");
        if( material.performReuse() ){
            System.out.print("Поступило на конвейерную ленту ");
            Product result = sf2.createProduct( material );
            return result;
        }
        return null;
    }
    @Override
    public ArrayList<Image> getImage() {
        ArrayList<Image> images = new ArrayList<>();
        images.add( this.conveyorImage );
        return images;
    }

    @Override
    public void setPower(Double power) {
        this.power = power;
    }

    @Override
    public boolean isWorked() {
        return this.worked;
    }
}

abstract class DecoratorConveyor implements Conveyor{
    // Fields
    protected Conveyor core;
    protected Double power;
    protected boolean worked;

    // Methods
    protected DecoratorConveyor( Conveyor core ){
        this.core = core;
    }
     @Override
     public Product recycling(RecyclableMaterials material) {
         return this.core.recycling( material );
     }
 }

class Shredder extends DecoratorConveyor{
    private Image shrederImage;

    @Override
    public ArrayList<Image> getImage(){
        ArrayList<Image> images = this.core.getImage();
        images.add( this.shrederImage );
        return images;
    }

    @Override
    public void setPower(Double power) {
        this.power = power;
    }

    @Override
    public boolean isWorked() {
        return this.worked;
    }

    public Shredder(Conveyor core) {
        super(core);
        try {
            File file = new File("src/resource/shrederMini.png");
            String localUrl = file.toURI().toURL().toString();
            this.shrederImage = new Image( localUrl );
            this.worked = true;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Product recycling(RecyclableMaterials material) {
        Product result = this.core.recycling( material );
        System.out.print(", измельчено в шредере");
        return result;
    }
}

class Sorting  extends DecoratorConveyor{
    Image sortingImage;
    public Sorting(Conveyor core) {
        super(core);
        try {
            File file = new File("src/resource/sortingMini2.png");
            String localUrl = file.toURI().toURL().toString();
            this.sortingImage = new Image( localUrl );
            this.worked = true;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
    @Override
    public Product recycling(RecyclableMaterials material) {
        Product result = this.core.recycling( material );
        System.out.print(", отсортировано");
        return result;
    }

    @Override
    public ArrayList<Image> getImage() {
        ArrayList<Image> images = this.core.getImage();
        images.add( this.sortingImage );
        return images;
    }

    @Override
    public void setPower(Double power) {
        this.power = power;
    }

    @Override
    public boolean isWorked() {
        return this.worked;
    }
}

// Паттерн Адаптер
// Адаптируемый класс
class EpoxyPlastic extends RecyclableMaterials{

    public EpoxyPlastic(){
        this.getEnergy = new NotForGenerateEnergy();
        this.repeatedUse = new ForRepeatedUse();
        this.repeatablePart = FactoryRepeatablePart.getRepeatablePart("EpoxyPlastic","src/resource/dirtyPlasticWeste.png");
    }

    @Override
    public void clear() {
        this.motion = false;
        this.coordX = 400.;
        this.coordY = 0.;
        this.engine = new EngineLine(12);
        this.repeatablePart =
                FactoryRepeatablePart.getRepeatablePart("EpoxyPlastic","src/resource/dirtyPlasticWeste.png");
        this.isClear = false;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitEpoxyPlastic( this );
    }

    public Image getImage(){
        return this.repeatablePart.getImage();
    }

    @Override
    public String sayType() {
        return this.repeatablePart.getType();
    }
}
// Адаптер

class AdapterEpoxyPlastic extends RecyclableMaterials implements ITrash{
    // Fields
    private EpoxyPlastic plastic;

    // Methods
    @Override
    public Image getImage(){
        return this.plastic.getImage();
    }

    @Override
    public String sayType(){
        return this.plastic.sayType();
    }

    @Override
    public void clear() {
        this.plastic.clear();
    }

    @Override
    public void accept(Visitor visitor) {
        this.plastic.accept( visitor );
    }

    @Override
    boolean performReuse() {
        return this.plastic.performReuse();
    }

    @Override
    boolean performGenerate() {
        return this.plastic.performGenerate();
    }

    public AdapterEpoxyPlastic(EpoxyPlastic plastic ){
        this.plastic = plastic;
    }

    @Override
    public Double getVolumeOfEnergy() {
        return 0.;
    }
    @Override
    public Double getWeight() {
        return 10.;
    }
    @Override
    public ArrayList<ITrash> getChildren() {
        return null;
    }
}

// Паттерн Декортатор
// Компоновщик: Мусор стостоит из мусора и стекла, бумаги, пластика и прочего.
// Интерфейс
interface ITrash{
    Double getVolumeOfEnergy();
    Double getWeight();
    ArrayList<ITrash> getChildren();
}

// Куча мусора - композит
class HeapOfTrash implements ITrash, IAggregate{
    // Fields
    private ArrayList<ITrash> someTrash;

    // Methods
    @Override
    public ArrayList<ITrash> getChildren(){
        return this.someTrash;
    }

    public HeapOfTrash(){
        this.someTrash = new ArrayList<>();
    }

    public void add( ITrash newTrash ){
        this.someTrash.add(0, newTrash );
    }

    public void remove( ITrash trash ){
        this.someTrash.remove( trash );
    }
    @Override
    public Double getVolumeOfEnergy() {
        Double totalEnergy = 0.;
        for( ITrash current : this.someTrash){
            totalEnergy += current.getVolumeOfEnergy();
        }
        return totalEnergy;
    }
    @Override
    public Double getWeight() {
        Double totalWeight = 0.;
        for( ITrash current : this.someTrash){
            totalWeight += current.getWeight();
        }
        return totalWeight;
    }

    @Override
    public IteratorHeapTrash createIterator() {
        return new IteratorHeapTrash(this);
    }
}

// Итератор
interface IAggregate{
    IIterator createIterator();
}

interface IIterator{
    void remove( ITrash element );
    ITrash next();
    Boolean hasNext();
}

class IteratorHeapTrash implements  IIterator{
    private HeapOfTrash heapOfTrash;
    private Stack<HashMap<HeapOfTrash, Integer>> returnAddres;
    private Integer index;

    public IteratorHeapTrash( HeapOfTrash heapOfTrash ){
        this.heapOfTrash = heapOfTrash;
        this.index = 0;
        this.returnAddres = new Stack<>();
    }


    @Override
    public void remove( ITrash element ) {
        if( this.hasNext() ){
            ITrash node = this.heapOfTrash.getChildren().get( this.index++ );
            if( node == element ){
                this.heapOfTrash.remove( node );
                HashMap<HeapOfTrash, Integer> back;
                while( !this.returnAddres.empty() ){
                    back = this.returnAddres.pop();
                    this.heapOfTrash = (back.keySet()).iterator().next();
                }
                this.index = 0;
            }else if( node.getChildren() == null ) {
                this.remove( element );
            }else{
                HashMap<HeapOfTrash, Integer> back = new HashMap<>();
                back.put(this.heapOfTrash, this.index);
                this.returnAddres.push( back );
                this.heapOfTrash = (HeapOfTrash)node;
                this.index = 0;
                this.remove( element );
            }
        }
        this.index = 0;
    }

    @Override
    public ITrash next() {
        if( this.hasNext() ){
            ITrash node = this.heapOfTrash.getChildren().get( this.index++ );
            if( node.getChildren() == null ) {
                return node;
            }else{
                HashMap<HeapOfTrash, Integer> back = new HashMap<>();
                back.put(this.heapOfTrash, this.index);
                this.returnAddres.push( back );
                this.heapOfTrash = (HeapOfTrash)node;
                this.index = 0;
                return this.next();
            }
        }else {
            this.index = 0;
            return null;
        }
    }

    @Override
    public Boolean hasNext() {
        if( this.index < this.heapOfTrash.getChildren().size() ){
            return true;
        }else if( !this.returnAddres.empty() ){
            HashMap<HeapOfTrash, Integer> back = this.returnAddres.pop();
            this.heapOfTrash = (back.keySet()).iterator().next();
            this.index = back.get( this.heapOfTrash );
            return this.hasNext();
        }
        return false;
    }
}

public class Controller implements Observer{
    @FXML
    public Canvas middlePart;
    @FXML
    public Button buttonFaсade;
    @FXML
    public Canvas placeOne;
    @FXML
    public Canvas placeTwo;
    @FXML
    public Canvas placeThree;
    @FXML
    public Canvas placeFour;
    @FXML
    public TextArea textWindow;
    @FXML
    public Button allSellButton;
    @FXML
    public Button recyclingButton;
    @FXML
    public Text moneyText;
    @FXML
    public Text powerText;
//    public Label labelPower;

    // Fields
    private GraphicsContext middleContext;
    private HeapOfTrash heap;
    private RecyclableMaterials currentMaterial;
    private Engine currentEngine;
    private Timer timer;
    private TimerTask timerTask;
    private FacadeFactory factory;
    public static final Integer SPEED = 12;
    private SimpleFactory simpleFactory;
    private FabricMethod fabricMethod;
    private Storehouse storehouse;

    private ObjectPool pool;
    private Director director;

    private Memento snapshot;

    private String message;

    private Cleaning cleaning;

    private Unloading unloading;
    private Unloading buttonUnloading;

    // Methods
    public void setMessage( String message ){
        this.message = message;
    }

    public void appendMessage(){
        this.textWindow.appendText( this.message );
    }

    public Controller(){
        this.cleaning = new Cleaning();
        this.pool = ObjectPool.getInstance();
        // Проверка работы паттерна Singleton
        ObjectPool test = ObjectPool.getInstance();

        this.unloading = new Unloading("saveStore.out");
        this.buttonUnloading = new Unloading("saveButtonState.out");

        // КОММАНДА!
        this.storehouse = new Storehouse( null );
        this.simpleFactory = new SimpleFactory();
//        this.factory = new FacadeFactory();

        Builder builder ;

        builder = new LuxuryBuilder();
        //builder = new ComunityBuilder();

        this.director = new Director( builder );
        this.factory = this.director.construct( this );

        this.fabricMethod = new FabricELine2();
        this.currentEngine = this.fabricMethod.createEngine();
//        this.currentEngine = new EngineLine2( SPEED );

        this.currentMaterial = this.pool.acquireMaterial(EpoxyPlastic.class.getName());
        this.currentMaterial.setEngine( this.currentEngine );
//        Engine currentLoader = new EngineLine2( speed );
        this.heap = null;
    }

    @FXML
    public void initialize() {
        this.middleContext = this.middlePart.getGraphicsContext2D();
        this.reprintCanvas();
        this.storehouse.setController( this );
//        this.moneyText.setText( "0$" );
        this.storehouse.setCommand( new ChangeButtonState( this.allSellButton ));
        this.powerText.setText( String.valueOf( 3 ) + " POW." );
//        this.labelPower.setText( this.powerVolume );
//        this.allSellButton.setBackground( new Background( new BackgroundImage( new Image("src/resource/truck.png"),
//                BackgroundRepeat.NO_REPEAT,BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
    }

    public void decorShredAction(ActionEvent actionEvent) {
        this.factory.upgrade( FacadeFactory.UPGRADE_SHREDDER );
        this.reprintCanvas();
    }

    public void decorSortAction(ActionEvent actionEvent) {
        this.factory.upgrade( FacadeFactory.UPGRADE_SORTING );
        this.reprintCanvas();
    }

    public void newTrashAction(ActionEvent actionEvent) {
        RecyclableMaterials temp = null;
        Integer indexType = new Random().nextInt( 5 );
        Integer biasX = (new Random()).nextInt( 100 );
        Integer biasY = (new Random()).nextInt( 100 );

        switch ( indexType ){
            case 0: temp = this.pool.acquireMaterial(Glass.class.getName()); break;
            case 1: temp = this.pool.acquireMaterial(Paper.class.getName()); break;
            case 2: temp = this.pool.acquireMaterial(EpoxyPlastic.class.getName()); break;
            case 3: temp = this.pool.acquireMaterial(PolyesterPlastic.class.getName()); break;
            case 4: temp = this.pool.acquireMaterial(Organic.class.getName()); break;
        }

        temp.setEngine( this.currentEngine );


        Double x = temp.getCoord().get( 0 );
        Double y = temp.getCoord().get( 1 );

        if( this.heap == null ){
            if( this.currentMaterial != null ){
                temp.setCoord(x + biasX, y + biasY);
                this.heap = new HeapOfTrash();
                this.heap.add((ITrash)this.currentMaterial);
                this.heap.add((ITrash)temp);
                this.currentMaterial = null;
            } else {
                this.currentMaterial = temp;
            }
        } else {
            temp.setCoord(x + biasX, y + biasY);
            this.heap.add((ITrash)temp);
        }
        this.reprintCanvas();
    }

    // Перерисовка экрана
    public void reprintCanvas(){
//        this.middleContext.clearRect(0,0,this.middlePart.getWidth(), this.middlePart.getHeight());
        try {
            this.middleContext.drawImage(new Image( new File("src/resource/wall.jpg").toURI().toURL().toString()),0,0,this.middlePart.getWidth(), this.middlePart.getHeight());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        // Прорисовка мусора из кучи
        if( this.heap != null ){
            IteratorHeapTrash iter = this.heap.createIterator();
            RecyclableMaterials curr;
            while( (curr = ((RecyclableMaterials)iter.next())) != null  ){
                this.middleContext.drawImage(curr.getImage(),
                        curr.getCoord().get(0), curr.getCoord().get(1));
            }
        }
        // Прорисовка отдельного мусора
        if( this.currentMaterial != null )  {
            this.middleContext.drawImage(this.currentMaterial.getImage(),
                    this.currentMaterial.getCoord().get(0), this.currentMaterial.getCoord().get(1));
        }
        // Рисуем конвейер
        ArrayList<Image> conveyorImages = this.factory.getImageConveyor();
        for(Image layer : conveyorImages) {
            this.middleContext.drawImage(layer, 10, 0);
        }
        // Рисуем отопитель
        this.middleContext.drawImage( this.factory.getImageBoiler(),
                this.middlePart.getWidth() - this.factory.getImageBoiler().getWidth(), 0);

        // Рисуем "склад"
        ArrayList<Canvas> marketCanvas = new ArrayList<>();
        marketCanvas.add( this.placeOne );
        marketCanvas.add( this.placeTwo );
        marketCanvas.add( this.placeThree );
        marketCanvas.add( this.placeFour );
        GraphicsContext сurrentContext;

        for ( int i = 0; i < marketCanvas.size(); i++ ){
            сurrentContext = marketCanvas.get( i ).getGraphicsContext2D();
            сurrentContext.clearRect(0,0,marketCanvas.get( i ).getWidth(), marketCanvas.get( i ).getHeight());
            if( i < this.storehouse.getGoods().size() ) {
                    сurrentContext.drawImage(this.storehouse.getGoods().get( i ).getImage(),0, 0);
            }
        }
    }

    public void clearDecorAction(ActionEvent actionEvent) {
        this.factory.clear();
        this.reprintCanvas();
    }

    public void recyclingAction(ActionEvent actionEvent) {
        if( this.heap != null && this.currentMaterial == null ){
            IteratorHeapTrash iter = this.heap.createIterator();
            ITrash temp = iter.next();
            iter = this.heap.createIterator();
            iter.remove( temp );
            this.currentMaterial = (RecyclableMaterials)temp;
//            loaderInit();
        }
        if( !this.currentMaterial.motion ){
            this.currentMaterial.motion = true;
            this.timerTask = new TimerTask() {
                @Override
                public void run() {
                    currentMaterial.moveLeft();

                    if( currentMaterial.getCoord().get( 0 ) <= 10 ){
                        Product result = factory.sendToFactory(currentMaterial, FacadeFactory.CONVEYOR );
                        if( result != null){
                            storehouse.add( result );
                            if( message != null ){
                                textWindow.appendText( message );
                            }
                        }

//                        currentEngine = currentMaterial.getEngine();
                        System.out.println();
                        timer.cancel();
                        timer.purge();

                        pool.releaseObject( currentMaterial );
                        currentMaterial = null;

                        if( heap == null ){
                            newTrashAction( new ActionEvent());
                        }else{
                            IteratorHeapTrash iter = heap.createIterator();
                            ITrash temp = iter.next();
                            if( temp == null ){
                                heap = null;
//                                buttonFaсade.setDisable( true );
                                newTrashAction( new ActionEvent());
                            }
                        }
                    }
                    reprintCanvas();
                }
            };
            this.timer = new java.util.Timer();
            this.timer.schedule(this.timerTask, 0, 30);
        }
    }

    public void goToBurnAction(ActionEvent actionEvent) {

        if( this.heap != null && this.currentMaterial == null ){
            IteratorHeapTrash iter = this.heap.createIterator();
            ITrash temp = iter.next();
            iter = this.heap.createIterator();
            iter.remove( temp );
            this.currentMaterial = (RecyclableMaterials)temp;
//            loaderInit();
        }

        if( !this.currentMaterial.motion ){
            this.currentMaterial.motion = true;
            this.timerTask = new TimerTask() {
                @Override
                public void run() {
                    currentMaterial.moveRight();
                    if( currentMaterial.getCoord().get( 0 ) + currentMaterial.getImage().getWidth() >= middlePart.getWidth()){
//                        currentEngine = currentMaterial.getEngine();
                        factory.sendToFactory(currentMaterial, FacadeFactory.BOILER );
                        timer.cancel();
                        timer.purge();

                        pool.releaseObject( currentMaterial );

                        currentMaterial = null;
                        if(heap == null ){
                            newTrashAction( new ActionEvent());
                        }else {
                            IteratorHeapTrash iter = heap.createIterator();
                            ITrash temp = iter.next();
                            if( temp == null ){
                                heap = null;
//                                buttonFaсade.setDisable( true );
                                newTrashAction( new ActionEvent());
                            }
                        }
                    }
                    reprintCanvas();
                }
            };

            this.timer = new java.util.Timer();
            this.timer.schedule(this.timerTask, 0, 30);
        }
    }

    public void weighAction(ActionEvent actionEvent) {
        ITrash test;
        if( this.currentMaterial != null ){
            test = (ITrash)this.currentMaterial;
        }else{
            test = (ITrash)this.heap;
        }
        this.textWindow.appendText("Вес мусора = " + test.getWeight());
        this.textWindow.appendText("\n");
    }

    public void facadeButtonAction(ActionEvent actionEvent) {

        if( this.currentEngine.getClass() == EngineLine.class ){
            System.out.println( EngineLine2.class );
            this.fabricMethod = new FabricELine2();
//            this.currentEngine = new EngineLine2( SPEED );
//            if( this.currentMaterial != null) {
//                this.currentMaterial.setEngine( this.currentEngine );
//            }
//            if( this.heap != null ){
//                IteratorHeapTrash iter = this.heap.createIterator();
//                ITrash temp = iter.next();
//                while ( temp != null ){
//                    ((RecyclableMaterials)temp).setEngine( this.currentEngine );
//                    temp = iter.next();
//                }
//            }

        }else if(  this.currentEngine.getClass() ==  EngineLine2.class  ){

            System.out.println( EngineSin.class );
            this.fabricMethod = new FabricESin();
//            this.currentEngine = new EngineSin( SPEED );
//            if( this.currentMaterial != null) {
//                this.currentMaterial.setEngine( this.currentEngine );
//            }
//            if( this.heap != null ){
//                IteratorHeapTrash iter = this.heap.createIterator();
//                ITrash temp = iter.next();
//                while ( temp != null ){
//                    ((RecyclableMaterials)temp).setEngine( this.currentEngine );
//                    temp = iter.next();
//                }
//            }
        }else {
            System.out.println( EngineLine.class );
            this.fabricMethod = new FabricELine();
//            this.currentEngine = new EngineLine( SPEED );
//            if( this.currentMaterial != null) {
//                this.currentMaterial.setEngine( this.currentEngine );
//            }
//            if( this.heap != null ){
//                IteratorHeapTrash iter = this.heap.createIterator();
//                ITrash temp = iter.next();
//                while ( temp != null ){
//                    ((RecyclableMaterials)temp).setEngine( this.currentEngine );
//                    temp = iter.next();
//                }
//            }
        }
        this.currentEngine = this.fabricMethod.createEngine();
        if( this.currentMaterial != null) {
            this.currentMaterial.setEngine( this.currentEngine );
        }
        if( this.heap != null ){
            IteratorHeapTrash iter = this.heap.createIterator();
            ITrash temp = iter.next();
            while ( temp != null ){
                ((RecyclableMaterials)temp).setEngine( this.currentEngine );
                temp = iter.next();
            }
        }
    }

    public void allSellAction(ActionEvent actionEvent) {
        this.storehouse.sellAll();
        if( message != null ){
            textWindow.appendText( message );
        }
        this.reprintCanvas();
    }

    public void keyPressedAction(KeyEvent keyEvent) {
        if( keyEvent.getText().equals("c") ){
            this.factory.changeBoiler( this );
            this.reprintCanvas();
        }
    }

    public void actionButtonSave(ActionEvent actionEvent) {
        this.snapshot = this.storehouse.save();
        this.unloading.setObject( this.snapshot, this.snapshot.getGoods() );
        this.unloading.unloading();
        this.buttonUnloading.setObject( (Boolean)this.allSellButton.isDisable() );
        this.buttonUnloading.unloading();
    }

    public void actionButtonRestore(ActionEvent actionEvent) {
        this.snapshot = (Memento) this.unloading.loading();
        this.storehouse.restore( this.snapshot );
        this.storehouse.setController( this );
        this.storehouse.setGoods( new ArrayList<>( this.unloading.getGoods() ));
        Boolean loadDisable = (Boolean) this.buttonUnloading.loading();
        this.allSellButton.setDisable( loadDisable );
        this.reprintCanvas();
    }

    @Override
    public void update(int amount) {
        if( this.powerText != null ){
            powerText.setText( String.valueOf( amount  ) + " POW.");
//            return;
        }
        if( this.recyclingButton != null ) {
            if (amount == 0 && !this.recyclingButton.isDisable()) {
                this.recyclingButton.setDisable(true);
//                System.out.println("Disable");
            } else if (amount != 0 && this.recyclingButton.isDisable()) {
                this.recyclingButton.setDisable(false);
//                System.out.println("Enable");
            }
//            this.textWindow.appendText("Осталось энергии: " + amount);
//            this.textWindow.appendText("\n");
        }
//        Platform.runLater(new Runnable() {
//
//            @Override
//            public void run() {
//                labelPower.setText( String.valueOf( amount ) );
//            }
//        });

    }

    public void actionGarbageDisposal(ActionEvent actionEvent) {
        // Прорисовка мусора из кучи
        if( this.heap != null ){
            IteratorHeapTrash iter = this.heap.createIterator();
            RecyclableMaterials curr;
            while( (curr = ((RecyclableMaterials)iter.next())) != null  ){
                curr.accept( this.cleaning );
            }
        }
        // Прорисовка отдельного мусора
        if( this.currentMaterial != null )  {
            this.currentMaterial.accept( this.cleaning );
        }
        this.reprintCanvas();
    }
}
