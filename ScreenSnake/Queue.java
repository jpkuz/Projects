public class Queue{
    public Node first, last;
    
    public Queue(int x, int y){
        Node n = new Node(x, y);
        first = n;
        last = n;
    }
    
    public void append(int x, int y){
        Node n = new Node(x, y);
        append(n);
    }
    
    public void append(Node n){
        try{
            last.next = n;
            n.prev = last;
            last = n;
        }
        catch (NullPointerException e){
            first = n;
            last = n;
        }
    }
    
    public Node remove(){
        Node ret = first;
        try{
            Node n = first.next;
            n.prev = null;
            first = n;
        }
        catch (NullPointerException e){
            first = null;
            last = null;
        }
        return ret;
    }
    
    public void print(){
        Node n = first;
        while (n != null){
            System.out.printf("%d %d\n",n.x_c, n.y_c);
            n = n.next;
        }
    }
    
}