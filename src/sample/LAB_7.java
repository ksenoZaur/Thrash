package sample;

import java.io.Serializable;
import java.util.ArrayList;

class Memento implements Serializable {
    // Fields
    private Integer maxSize;
    private transient ArrayList<Product> goods;
    private StateStorehouse stateStorehouse;
    private transient Controller controller;

    // Methods
    public Memento( Integer maxSize, ArrayList<Product> goods, StateStorehouse stateStorehouse, Controller controller){
        this.maxSize = maxSize;
        this.goods = new ArrayList<>(goods);
        this.stateStorehouse = stateStorehouse;
        this.controller = controller;
    }

    public Integer getMaxSize() {
        return maxSize;
    }

    public ArrayList<Product> getGoods() {
        return goods;
    }

    public StateStorehouse getStateStorehouse() {
        return stateStorehouse;
    }

    public Controller getController() {
        return this.controller;
    }
}

interface StateStorehouse{
    // Methods
    boolean add( Product newGoods, Storehouse store);
    Double sellAll( Storehouse store );
}

class StateNotFullStorehouse implements Serializable, StateStorehouse{

    @Override
    public boolean add( Product newGoods, Storehouse store) {
        store.getGoods().add( newGoods );
        if( store.getGoods().size() == store.getMaxSize() ){
            boolean eq = true;
            for( int i = 0; i < store.getGoods().size() - 1; i++ ){
                if( !store.getGoods().get( i ).getClass().getName().equals( store.getGoods().get( i + 1).getClass().getName())){
                    eq = false;
                    break;
                }
            }
            if( eq ){
                store.setState( new StateExtraFullStorehouse() );
            } else {
                store.setState( new StateFullStorehouse() );
            }
        }
        store.getController().setMessage("State1 : Товар успешно добавлен на склад." + "\n");
//        store.getController().setMessage("\n");
        return true;
    }

    @Override
    public Double sellAll( Storehouse store ) {
        Double totalPrice = 0.0;
        for(Product currentProduct: store.getGoods()){
            totalPrice += currentProduct.getPrice();
        }
        store.setGoods( new ArrayList<>() );
        store.getController().setMessage("State1 : Товар продан. Сумма продажи " + totalPrice + "\n");
//        store.getTextOut().appendText("\n");
        return totalPrice;
    }
}

class StateFullStorehouse implements  Serializable, StateStorehouse{

    @Override
    public boolean add(Product newGoods, Storehouse store) {
        store.getController().setMessage("State2 : Невозможно добавить товар. Закончилось место на складе." + "\n");
//        store.getTextOut().appendText("\n");
        return false;
    }

    @Override
    public Double sellAll( Storehouse store ) {
        Double totalPrice = 0.0;
        for(Product currentProduct: store.getGoods() ){
            totalPrice += currentProduct.getPrice();
        }
        store.setGoods( new ArrayList<>() );
        store.setState( new StateNotFullStorehouse() );
        store.getController().setMessage("State2 : Товар продан. Сумма продажи " + totalPrice + "\n");
//        store.getTextOut().appendText("\n");
        return totalPrice;
    }
}

class StateExtraFullStorehouse implements Serializable, StateStorehouse{

    @Override
    public boolean add(Product newGoods, Storehouse store) {
        store.getController().setMessage("State3 : Невозможно добавить товар. Закончилось место на складе." + "\n");
//        store.getTextOut().appendText("\n");
        return false;
    }

    @Override
    public Double sellAll(Storehouse store) {
        Double totalPrice = 0.0;
        for(Product currentProduct: store.getGoods() ){
            totalPrice += currentProduct.getPrice();
        }
        store.setGoods( new ArrayList<>() );
        store.setState( new StateNotFullStorehouse() );
        store.getController().setMessage("State3 : Прибыль в x2 размере!!! Сумма продажи " + (2 * totalPrice) + "\n");
//        store.getTextOut().appendText("\n");
        return totalPrice;
    }
}

interface Observer{
    void update( int amount );
}

interface Publisher {
    void attach( Observer observer);
    void detach( Observer observer);
    void notifyAbout();
}

public class LAB_7 {
}
