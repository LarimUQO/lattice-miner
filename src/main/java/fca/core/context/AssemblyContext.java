package fca.core.context;

import fca.exception.AlreadyExistsException;
import fca.exception.InvalidTypeException;
/**
 * Interface permettant d'assembler deux contextes
 * @author Linda Bogni
 * @version 1.0
 */

public interface AssemblyContext {
/*
 * 	Assembly of 2 contexts by attributes	
 */
Context apposition (Context c1, Context c2) throws InvalidTypeException, CloneNotSupportedException, AlreadyExistsException ;

/*
 * 	Assembly of 2 contexts by objects	
 */
Context subposition (Context c1, Context c2) throws InvalidTypeException, CloneNotSupportedException, AlreadyExistsException ;

}