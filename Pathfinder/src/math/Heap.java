package math;

import entities.Node;

// credit goes to https://www.youtube.com/watch?v=3Dw5d7PlcTM (Sebastian Lague's video, C# implementation)
public class Heap {

    private Node[] items;
    private int itemCount;

    public Heap(int maxHeapSize) {
        items = new Node[maxHeapSize];
    }

    public void add(Node item) {
        item.setHeapIndex(itemCount);
        items[itemCount] = item;
        sortUp(item);
        itemCount++;
    }

    public Node removeBottom() {
        Node firstItem = items[0];
        itemCount--;
        items[0] = items[itemCount];
        items[0].setHeapIndex(0);
        sortDown(items[0]);
        return firstItem;
    }

    public void updateItem(Node item) {
        sortUp(item);
    }
    
    public boolean contains(Node item) {
    	boolean flag = false;
		for(int i = 0; i <= itemCount - 1; i++) {
			if(items[i].getX() == item.getX() && items[i].getY() == item.getY()) {
				flag = true;
				break;
			}
		}
		return flag;
    }

    void sortDown(Node item) {
        while(true) {
            int leftChildIndex = item.getHeapIndex() * 2 + 1;
            int rightChildIndex = item.getHeapIndex() * 2 + 2;
            int swapIndex = 0;

            if(leftChildIndex < itemCount) {
                swapIndex = leftChildIndex;

                if(rightChildIndex < itemCount) {
                    if(items[leftChildIndex].compareNodes(items[rightChildIndex]) < 0) {
                        swapIndex = rightChildIndex;
                    }
                }

                if(item.compareNodes(items[swapIndex]) < 0) {
                    swap(item, items[swapIndex]);
                }
                else
                    return;
            }
            else
                return;
        }
    }

    void sortUp(Node item) {
        int parentIndex = (item.getHeapIndex() - 1) / 2;

        while(true) {
            Node parentItem = items[parentIndex];
            if(item.compareNodes(parentItem) > 0) {
                swap(item, parentItem);
            }
            else
            	break;
            
            parentIndex = (item.getHeapIndex() - 1) / 2;
        }
    }

    void swap(Node a, Node b) {
        items[a.getHeapIndex()] = b;
        items[b.getHeapIndex()] = a;
        int aIndex = a.getHeapIndex();
        a.setHeapIndex(b.getHeapIndex());
        b.setHeapIndex(aIndex);
    }

    public int getCount() {
        return itemCount;
    }

}