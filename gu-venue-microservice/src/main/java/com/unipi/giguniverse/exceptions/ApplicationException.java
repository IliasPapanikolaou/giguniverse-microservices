package com.unipi.giguniverse.exceptions;

public class ApplicationException extends RuntimeException {

    //RestExceptionHandler restExceptionHandler;

   public ApplicationException(String exMessage, Exception exception) {
       super(exMessage, exception);
   }

   public ApplicationException(String exMessage){
       super(exMessage);
   }
}
