package Tree;

import java.util.Stack;

import Exceptions.DuplicateItemException;

//RedBlackTree class
//
//CONSTRUCTION: with no parameters
//
//******************PUBLIC OPERATIONS*********************
//void insert( x )       --> Insert x
//void remove( x )       --> Remove x (Mostly implemented by Christian Silivestru)
//Comparable find( x )   --> Return item that matches x
//Comparable findMin( )  --> Return smallest item
//Comparable findMax( )  --> Return largest item
//boolean isEmpty( )     --> Return true if empty; else false
//void makeEmpty( )      --> Remove all items
//void printTree( )      --> Print all items
//******************ERRORS********************************
//Exceptions are thrown by insert if warranted and remove.


/**
* Implements a red-black tree.
* Note that all "matching" is based on the compareTo method.
* @author Mark Allen Weiss
* 
* Remove method written by Christian Silivestru
* <code>printTree</code> altered by Christian Silivestru
*/
public class RedBlackTree<AnyType extends Comparable<? super AnyType>>
{
	private RedBlackNode<AnyType> header;
	 private RedBlackNode<AnyType> nullNode;

	 private static final int BLACK = 1;    // BLACK must be 1
	 private static final int RED   = 0;

	     // Used in insert routine and its helpers
	 private RedBlackNode<AnyType> current;
	 private RedBlackNode<AnyType> parent;
	 private RedBlackNode<AnyType> grand;
	 private RedBlackNode<AnyType> great;
	 
 /**
  * Construct the tree.
  */
 public RedBlackTree( )
 {
     nullNode = new RedBlackNode<AnyType>( null );
     nullNode.left = nullNode.right = nullNode;
     header      = new RedBlackNode<AnyType>( null );
     header.left = header.right = nullNode;
 }

 /**
  * Compare item and t.element, using compareTo, with
  * caveat that if t is header, then item is always larger.
  * This routine is called if is possible that t is header.
  * If it is not possible for t to be header, use compareTo directly.
  */
 private final int compare( AnyType item, RedBlackNode<AnyType> t )
 {
     if( t == header )
         return 1;
     else
         return item.compareTo( t.element );    
 }
 
 /**
  * Insert into the tree.
  * @param item the item to insert.
  * @throws DuplicateItemException if item is already present.
  */
 public void insert( AnyType item )
 {
     current = parent = grand = header;
     nullNode.element = item;

     while( compare( item, current ) != 0 )
     {
         great = grand; grand = parent; parent = current;
         current = compare( item, current ) < 0 ?
                      current.left : current.right;

             // Check if two red children; fix if so
         if( current.left.color == RED && current.right.color == RED )
              handleReorient( item );
     }

         // Insertion fails if already present
     if( current != nullNode )
         throw new DuplicateItemException( item.toString( ) );
     current = new RedBlackNode<AnyType>( item, nullNode, nullNode );

         // Attach to parent
     if( compare( item, parent ) < 0 )
         parent.left = current;
     else
         parent.right = current;
     handleReorient( item );
 }

 /**
  * Remove from the tree.
  * @param x the item to remove.
  * @throws UnsupportedOperationException if called.
  */
 public void remove( AnyType x )
 {
	 find(x);
	 
	 /*
	  * If the node we're looking to delete is a red leaf, deal with it accordingly
	  */
	 if (current.color == RedBlackTree.RED && current.right == nullNode && current.left == nullNode) {
		removeCaseRedLeaf(header, header.right, x);
	 }
	 
	 //If the node has one child
	 else if ((current.right != nullNode && current.left == nullNode) || current.right == nullNode && current.left != nullNode) {
		 removeCaseOneChild(header, header.right, x);
	 }
	 
	 //If the node is black and has two black children
	 else if (current.color == RedBlackTree.BLACK && current.right.color == RedBlackTree.BLACK && current.left.color == RedBlackTree.BLACK) {
		 removeCaseCurrentBlackAndTwoBlackChildren(header, header.right, x);
	 }
	 
	 //If the node is black and one of it's children is red
	 else if (current.color == RedBlackTree.BLACK && (current.right.color == RedBlackTree.RED || current.left.color == RedBlackTree.RED)) {
		 removeCaseCurrentBlackAndOneRedChild(header, header.right, x);
	 }
	 
	 //If the node is red and has 2 children
	 else if (current.color == RedBlackTree.RED && current.right != nullNode && current.left != nullNode) {
		 RedBlackNode temp = current;
		 
		 current = header.right;
		 parent = header;
		//Set the current node and the parent to pass it over to the auxilary function
		 while (current != temp) {
			 parent = current;
			 if (compare(x,current) > 0)  
				 current = current.right;
			 
			 
			 else 
				 current = current.left;
		 }
		 removeCaseCurrentRedAndTwoChildren(parent, current, x);
	 }

	 	
 }
 
/**
 * Removes a red leaf from the tree
 * @param P The parent of the current node
 * @param C The current node
 * @param x The element we are removing
 */
 private void removeCaseRedLeaf(RedBlackNode P, RedBlackNode C, AnyType x) {
	//P is the parent
	//C is the current node
	 //x is what we're comparing
	 
	 parent = P;
	 current = C;
	
	 //Find the actual current node we're looking for while keeping track of the parent node
	 if (compare(x,C) > 0) {
		removeCaseRedLeaf(C,C.right,x);
	 }
	 
	 else if (compare(x,C) < 0) {
		 removeCaseRedLeaf(C,C.left,x);
	 }
	 
	 //If found just remove the link to it
	 else {
		 if (P.right == C)
			 P.right = nullNode;
		 
		 else
			 P.left = nullNode;
	 }
 }

 /**
  * Removes the node when it has one child only
  * @param P The parent to the current node
  * @param C The current node
  * @param x The element to remove
  */
 private void removeCaseOneChild(RedBlackNode P, RedBlackNode C, AnyType x) {
	 
	 parent = P;
	 current = C;
	 
	 if (compare(x,C) > 0) {
		 removeCaseOneChild(C,C.right,x);
	 }
	 
	 else if (compare(x,C) < 0) {
		 removeCaseOneChild(C,C.left,x);
	 }
	 
	 else {
		 if (P.right == C) {
			 if (C.right == nullNode) 
				 P.right = C.left;
			 else
				 P.right = C.right;
			 
			 C.right.color = C.color;
		 }
		 
		 else {
			 if (C.right == nullNode)
				 P.left = C.left;
			 else
				 P.left = C.right;
			 
			 C.right.color =C.color;
		 }
	 }
 }
 
 /**
  * Removes the node if it's black with two black children
  * @param P The parent of the current node
  * @param C The current node
  * @param x The item to be removed
  */
 private void removeCaseCurrentBlackAndTwoBlackChildren(RedBlackNode P, RedBlackNode C, AnyType x) {
	 
	 parent = P;
	 current = C;
	 
	 //Find the node to remvoe while keeping track of the parent
	 if (compare(x,C) > 0) {
			removeCaseCurrentBlackAndTwoBlackChildren(C,C.right,x);
	 }
		 
	 else if (compare(x,C) < 0) {
		 	removeCaseCurrentBlackAndTwoBlackChildren(C,C.left,x);
	 }
	 
	 //What to do is based on the sibling
	 else {
		 if (parent.left == current) {
			 //If the sibling has 2 black children call auxilary method
			 if (parent.right.left.color == RedBlackTree.BLACK && parent.right.right.color == RedBlackTree.BLACK) {
				 removeSubCaseSiblingHasTwoBlackChildren(parent.right, x);
			 }
			 
			 //If the sibling's outer child is red, double rotate and start over
			 else if (parent.right.left.color == RedBlackTree.RED) {
				 rotateWithLeftChild(parent.right);
				 rotateWithRightChild(parent.right);
				 remove(x);
			 }
			 
			 //If the sibling's inner child is red, single rotate and start over
			 else if (parent.right.right.color == RedBlackTree.RED) {
				 rotateWithRightChild(parent.right);
				 remove(x);
			 }
		 }
		 
		 /*
		  * Thes bottom three cases are the mirror image of the above three cases
		  */
		 else if (parent.right == current) {
			 if (parent.left.left.color == RedBlackTree.BLACK && parent.left.right.color == RedBlackTree.BLACK) {
				 removeSubCaseSiblingHasTwoBlackChildren(parent.left, x);
			 }
			 
			 else if (parent.left.right.color == RedBlackTree.RED) {
				 rotateWithRightChild(parent.left);
				 rotateWithLeftChild(parent.left);
				 remove(x);
			 }
			 
			 else if (parent.left.left.color == RedBlackTree.RED) {
				 rotateWithLeftChild(parent.left);
				 remove(x);
			 }
		 }
	 }
 }

 /**
  * Auxilary method that removes the node only of its sibling has 2 black children
  * @param sibling The sibling of the node to remove
  * @param x The item to remvoe
  */
 private void removeSubCaseSiblingHasTwoBlackChildren(RedBlackNode sibling, AnyType x) {
	 
	 //Colour flip in the one direction
	 if (parent.color == RedBlackTree.BLACK) {
		 parent.color = RedBlackTree.RED;
		 current.color = RedBlackTree.BLACK;
		 sibling.color = RedBlackTree.BLACK;
	 }
	 
	 //Colour flip in the other direction
	 else {
		 parent.color = RedBlackTree.BLACK;
		 current.color = RedBlackTree.RED;
		 sibling.color = RedBlackTree.RED;
	 }
	 
	 //Repeat delete algorithm
	 remove(x);
 }

 /**
  * Removes the node if it's red and has 2 children
  * @param P The parent of the current node
  * @param C The current node
  * @param x The item used to identify the node to remove
  */
 private void removeCaseCurrentRedAndTwoChildren(RedBlackNode P, RedBlackNode C, AnyType x) {
	 	current = C;
	 	parent = P; 
	 	//Implement the findMin method using the current's larger child has the starting point
	 	//We do this to find the minimum element in the right subtree of C to replace C
	 	RedBlackNode itr = C.right;
	     RedBlackNode ParentToItr = itr;
	
	     while( itr.left != nullNode ) {
	    	 ParentToItr = itr;
	         itr = itr.left;
	     }
	     
	     //Replace C
		 C.element = itr.element;
		 C.color = itr.color;
		 
		 //Based on what child of P we're replacing, change the pointers accordingly
		 if (C.right == itr)
			 C.right = itr.right;
		 
		 if (ParentToItr != itr)
			 ParentToItr.left = nullNode;
		 
		 //Re-colour
		 current = C;
		 handleReorient(x);

 }

 /**
  * Removes the node if it's black and has one red child
  * @param P The parent of the current node
  * @param C The current node
  * @param x The item used to identify the node to be removed
  */
 private void removeCaseCurrentBlackAndOneRedChild(RedBlackNode P, RedBlackNode C, AnyType x) {
	 current = C;
	 parent = P;
	 
	 //Find the node to remve while keeping track of the parent
	 if (compare(x,C) > 0) {
		 removeCaseCurrentBlackAndOneRedChild(C,C.right,x);
	 }
		 
	 else if (compare(x,C) < 0) {
		 removeCaseCurrentBlackAndOneRedChild(C,C.left,x);
	 }
	 
	 //Case 1
	 if (current.left.color == RedBlackTree.RED)
		 removeCaseCurrentRedAndTwoChildren(parent, current, x);
	 
	 //Case 2. Rotate and repeat
	 else {
		 rotateWithRightChild(parent);
		 remove(x);
	 }
 }
 /**
  * Find the smallest item  the tree.
  * @return the smallest item or null if empty.
  */
 public AnyType findMin( )
 {
     if( isEmpty( ) )
         return null;

     RedBlackNode<AnyType> itr = header.right;

     while( itr.left != nullNode )
         itr = itr.left;

     return itr.element;
 }

 /**
  * Find the largest item in the tree.
  * @return the largest item or null if empty.
  */
 public AnyType findMax( )
 {
     if( isEmpty( ) )
         return null;

     RedBlackNode<AnyType> itr = header.right;

     while( itr.right != nullNode )
         itr = itr.right;

     return itr.element;
 }

 /**
  * Find an item in the tree.
  * @param x the item to search for.
  * @return the matching item or null if not found.
  */
 public AnyType find( AnyType x )
 {
     nullNode.element = x;
     current = header.right;

     for( ; ; )
     {
         if( x.compareTo( current.element ) < 0 )
             current = current.left;
         else if( x.compareTo( current.element ) > 0 ) 
             current = current.right;
         else if( current != nullNode )
             return current.element;
         else
             return null;
     }
 }

 /**
  * Make the tree logically empty.
  */
 public void makeEmpty( )
 {
     header.right = nullNode;
 }

 /**
  * Print all items.
  */
 public void printTree( )
 {
     printTree( header.right );
 }
 
 /**
  * Internal method to print a subtree in sorted order.
  * @param t the node that roots the tree.
  * Altered by Christian Silivestru
  */
 private void printTree( RedBlackNode t )
 {
	 String Colour = "Black";
     if( t != nullNode )
     {
         printTree( t.left );
         Element temp = (Element)t.element;
         if (t.color == 0)
        	 Colour = "Red";
         System.out.println( temp.key + "\t" + Colour + "\t" + t.height);
         printTree( t.right );
     }
 }
  
 /**
  * Test if the tree is logically empty.
  * @return true if empty, false otherwise.
  */
 public boolean isEmpty( )
 {
     return header.right == nullNode;
 }

 /**
  * Internal routine that is called during an insertion
  * if a node has two red children. Performs flip and rotations.
  * @param item the item being inserted.
  */
 private void handleReorient( AnyType item )
 {
         // Do the color flip
     current.color = RED;
     current.left.color = BLACK;
     current.right.color = BLACK;

     if( parent.color == RED )   // Have to rotate
     {
         grand.color = RED;
         if( ( compare( item, grand ) < 0 ) !=
             ( compare( item, parent ) < 0 ) )
             parent = rotate( item, grand );  // Start dbl rotate
         current = rotate( item, great );
         current.color = BLACK;
     }
     header.right.color = BLACK; // Make root black
 }

 /**
  * Internal routine that performs a single or double rotation.
  * Because the result is attached to the parent, there are four cases.
  * Called by handleReorient.
  * @param item the item in handleReorient.
  * @param parent the parent of the root of the rotated subtree.
  * @return the root of the rotated subtree.
  */
 private RedBlackNode<AnyType> rotate( AnyType item, RedBlackNode<AnyType> parent )
 {
     if( compare( item, parent ) < 0 )
         return parent.left = compare( item, parent.left ) < 0 ?
             rotateWithLeftChild( parent.left )  :  // LL
             rotateWithRightChild( parent.left ) ;  // LR
     else
         return parent.right = compare( item, parent.right ) < 0 ?
             rotateWithLeftChild( parent.right ) :  // RL
             rotateWithRightChild( parent.right );  // RR
 }

 /**
  * Rotate binary tree node with left child.
  */
 private static <AnyType> RedBlackNode<AnyType> rotateWithLeftChild( RedBlackNode<AnyType> k2 )
 {
     RedBlackNode<AnyType> k1 = k2.left;
     k2.left = k1.right;
     k1.right = k2;
     k2.height = max( height( k2.left ), height( k2.right ) ) + 1;
     k1.height = max( height( k1.left ), k2.height ) + 1;
     return k1;
 }

 /**
  * Rotate binary tree node with right child.
  */
 private static <AnyType> RedBlackNode<AnyType> rotateWithRightChild( RedBlackNode<AnyType> k1 )
 {
     RedBlackNode<AnyType> k2 = k1.right;
     k1.right = k2.left;
     k2.left = k1;
     k1.height = max( height( k1.left ), height( k1.right ) ) + 1;
     k2.height = max( height( k2.right ), k1.height ) + 1;
     return k2;
 }
 
 private static int height( RedBlackNode t )
 {
     return t == null ? -1 : t.height;
 }
 
 private static int max( int lhs, int rhs )
 {
     return lhs > rhs ? lhs : rhs;
 }

 private static class RedBlackNode<AnyType>
 {
         // Constructors
     RedBlackNode( AnyType theElement )
     {
         this( theElement, null, null );
     }

     RedBlackNode( AnyType theElement, RedBlackNode<AnyType> lt, RedBlackNode<AnyType> rt )
     {
         element  = theElement;
         left     = lt;
         right    = rt;
         color    = RedBlackTree.BLACK;
         
     }

     AnyType               element;    // The data in the node
     RedBlackNode<AnyType> left;       // Left child
     RedBlackNode<AnyType> right;      // Right child
     int                   color;      // Color
     int				   height = 0;
 }
 
 /**
  * Prints a visual representation of the Tree
  * Originally designed for a BST. Altered by Christian Silivestru
  *
  */
 public void displayTree()
 {
 Stack globalStack = new Stack();
 globalStack.push(header.right);
 int nBlanks = 32;
 boolean isRowEmpty = false;
 System.out.println(
 "......................................................");
 while(isRowEmpty==false)
    {
    Stack localStack = new Stack();
    isRowEmpty = true;

    for(int j=0; j<nBlanks; j++)
       System.out.print(' ');

    while(globalStack.isEmpty()==false)
       {
       RedBlackNode temp = (RedBlackNode)globalStack.pop();
	      String Colour = "B";
	 	  if (temp.color == 0)
	 		  Colour = "R";
	 	  Element tempElement = (Element)temp.element;
       if(temp != nullNode)
          {
    	  
          System.out.print(tempElement.key + " | " + Colour);
          localStack.push(temp.left); 
          localStack.push(temp.right);

          if(temp.left != nullNode ||
                              temp.right != nullNode)
             isRowEmpty = false;
          }
       else
          {
          System.out.print("--");
          localStack.push(nullNode);
          localStack.push(nullNode);
          }
       for(int j=0; j<nBlanks*2-2; j++)
          System.out.print(' ');
       }  // end while globalStack not empty
    System.out.println();
    nBlanks /= 2;
    while(localStack.isEmpty()==false)
       globalStack.push( localStack.pop() );
    }  // end while isRowEmpty is false
 System.out.println(
 "......................................................");
 }  // end displayTree()
 


 
}