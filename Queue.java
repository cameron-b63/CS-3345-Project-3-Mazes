public class Queue{
    /*
     * This is my implementation of a queue using a linked list. I've purpose-written both classes for my specific use case.
     * 
     * This is a fairly standard q implementation.
     */
    private class LinkedList{
        private class ListNode{
            int element;
            ListNode next;
            public ListNode(int ele){
                element = ele;
                next = null;
            }

            public void setNext(ListNode newNext) { this.next = newNext; }
            public int getElement() { return this.element; }
        }

        private ListNode head;
        private ListNode tail;
        private int size;

        public LinkedList(){
            head = tail = null;
            size = 0;
        }

        public void insertOnTail(int ele){
            if(head == null){
                head = tail = new ListNode(ele);
                size++;
                return;
            }

            ListNode toPush = new ListNode(ele);
            tail.setNext(toPush);
            tail = toPush;
            size++;
        }

        public int removeFromHead(){
            if(size == 0) throw new NullPointerException("Null pointer passed to removeFromHead()");
            int ret = head.getElement();
            head = (head != tail) ? head.next : null;
            if(head == null) tail = null;
            size--;
            return ret;
        }

        public int getSize() { return this.size; }
    }

    private LinkedList q;

    public Queue(){
        q = new LinkedList();
    }

    // I'm assuming I'm good enough at programming to use these properly without error checking. Big gamble...
    public void enqueue(int element) { q.insertOnTail(element); }
    public int dequeue() { return q.removeFromHead(); }
    public boolean isEmpty() { return q.getSize() == 0; }
}