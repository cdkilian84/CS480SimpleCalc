/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs480.calculator.project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 *
 * @author Chris
 */
public class Calculator {
    
    Map<String, Integer> precedenceMap;
    
    public Calculator(){
        precedenceMap = new HashMap<>();
        precedenceMap.put("*", 3);
        precedenceMap.put("/", 3);
        precedenceMap.put("+", 2);
        precedenceMap.put("-", 2);
    }
    
    public String calculate(String input){
        String[] postFixInput = parseUserInput(input);
        String result = evalPostFix(postFixInput);
        return result;
    }
    
    //implementation of the shunting-yard algorithm to transform string to postfix notation
    private String[] parseUserInput(String input){
        List<String> postFix = new ArrayList<>();
        String[] tokenizedInput = tokenizeInput(input);
        Stack<String> operatorStack = new Stack();
        
        
        for(String theToken : tokenizedInput){
            System.out.println("tokenizedInput value is: " + theToken);
            if(theToken.matches("\\-?\\d+")){ //if theToken is a number, append to output string
                postFix.add(theToken);
            }
            else if(theToken.matches("[\\+\\-\\/\\*]")){ //else if theToken is an operator
                //(there is an operator at the top of the operator stack with greater precedence) 
                // or (the operator at the top of the operator stack has equal precedence and the operator is left associative)) 
                // and (the operator at the top of the stack is not a left bracket):
                while(checkForLeftParen(operatorStack) && checkPrecedence(theToken, operatorStack)){ 
                    postFix.add(operatorStack.pop()); //pop operators from the operator stack, onto the output queue.
                }
                operatorStack.push(theToken); //push the read operator onto the operator stack.
            }
            else if(theToken.matches("\\(")){ //if token is left parentheses
                operatorStack.push(theToken); //push left parentheses to operator stack
            }
            else if(theToken.matches("\\)")){ //if token is right parentheses
                while(checkForLeftParen(operatorStack)){ //the operator at the top of the operator stack is not a left parentheses
                    postFix.add(operatorStack.pop()); //pop operators from the operator stack, onto the output queue.
                }
                if(!operatorStack.empty()){ //if we haven't reached the end of the stack
                    operatorStack.pop(); //pop off left parentheses (must be because of "checkForLeftParen" logic)
                }else{
                    throw new RuntimeException("There were mismatched parentheses in your operation! This is illegal!");//STACK EMPTY - ERROR
                }
            }
        }
        
        while(!operatorStack.empty()){
            if(operatorStack.peek().equals("(") || operatorStack.peek().equals(")")){
                throw new RuntimeException("There were mismatched parentheses in your operation! This is illegal!");//MISMATCHED PARENTHESES LEFT ON STACK - ERROR
            }else{
                postFix.add(operatorStack.pop());
            }
        }
        
        System.out.println("Results of parseUserInput: " + postFix.toString());
        return postFix.toArray(new String[0]);
    }
    
    
    private String[] tokenizeInput(String input){
        List<String> tokenList = new ArrayList<>();
        String[] tokenizedInput = input.split("");
        StringBuilder currentWorkingNumber = new StringBuilder();
        
        System.out.println("In tokenizeInput!");
        
        for(String token : tokenizedInput){
            System.out.println("token now is: " + token);
            if(token.matches("\\d")){ //if theToken is a number, append to output string
                currentWorkingNumber.append(token);
            }
            else if(token.matches("[\\+\\-\\/\\*\\(\\)]")){
                if(currentWorkingNumber.length() > 0){
                    tokenList.add(currentWorkingNumber.toString());
                    currentWorkingNumber = new StringBuilder(); //new stringbuilder clears out old number being stored since it's been added to the token list already
                }
                tokenList.add(token);
            }
        }
        if(currentWorkingNumber.length() > 0){
            tokenList.add(currentWorkingNumber.toString());
        }
        
        return tokenList.toArray(new String[0]);
    }
    
    
    //return true if theToken is lower or equal precedence to the top of the operator stack AND stack isn't empty
    private boolean checkPrecedence(String currentOperator, Stack<String> operatorStack){
        boolean precedenceResult = false;
        if(!operatorStack.empty()){
            //System.out.println("currentOperator is: " + currentOperator);
            //System.out.println("top of operatorStack is: " + operatorStack.peek());
            if(precedenceMap.get(operatorStack.peek()) >= precedenceMap.get(currentOperator)){
                precedenceResult = true;
            }
        }
        return precedenceResult;
    }
    
    //return true if stack is NOT empty AND operator on top of stack isn't left parenthesis
    private boolean checkForLeftParen(Stack<String> operatorStack){
        boolean operatorResult = false;
        if(!operatorStack.empty() && !operatorStack.peek().equals("(")){
            operatorResult = true;
        }
        return operatorResult;
    }
    
    //evaluate postfix
    private String evalPostFix(String[] postFixEquation){
        Stack<String> evalStack = new Stack();
        String result = "";
        
        System.out.println("Now in evalPostFix!");
        
        for(String token : postFixEquation){
            System.out.println("evalPostFix token is: " + token);
            if(token.matches("[\\+\\-\\/\\*]")){
                if(evalStack.size() >= 2){
                    String operand2 = evalStack.pop();
                    String operand1 = evalStack.pop();
                    evalStack.push(evalOperation(operand1, operand2, token));
                }else{
                    throw new RuntimeException("Operators are in an illegal order! Equation is invalid!");
                }
            }
            else if(token.matches("\\-?\\d+")){
                evalStack.push(token);
            }
        }
        
        if(!evalStack.empty()){
            result = evalStack.pop();
        }else{
            throw new RuntimeException("An unknown error occurred during calculation!!");
        }
        
        return result;
    }
    
    private String evalOperation(String operand1, String operand2, String operator){
        String result = "";
        
        if(operand1.matches("\\-?\\d+") && operand2.matches("\\-?\\d+")){ //check operands are numbers
            switch(operator){
                case "+": result = addFunction(operand1, operand2);
                break;
            
                case "-": result = subFunction(operand1, operand2);
                break;
            
                case "*": result = multFunction(operand1, operand2);
                break;
            
                case "/": result = divFunction(operand1, operand2);
                break;
            
                default: throw new RuntimeException("An illegal operator was found in the string! You cannot perform anything but +, -, *, or /.");
            }
        }else{
            throw new RuntimeException("Invalid operands present! Double check your equation is correct!");
        }
        
        

        return result;
    }
    
    //Note: By the time these are called, operand1 and operand2 should already be checked for being numeric values only
    private String addFunction(String operand1, String operand2){
        String result = "";
        int op1 = Integer.parseInt(operand1);
        int op2 = Integer.parseInt(operand2);
        
        int numResult = op1+op2;
        result = Integer.toString(numResult);
        
        return result;
    }
    
    //Note: By the time these are called, operand1 and operand2 should already be checked for being numeric values only
    private String subFunction(String operand1, String operand2){
        String result = "";
        int op1 = Integer.parseInt(operand1);
        int op2 = Integer.parseInt(operand2);
        
        int numResult = op1-op2;
        result = Integer.toString(numResult);
        System.out.println("Result of subFunction is: " + result);
        return result;
    }
    
    //Note: By the time these are called, operand1 and operand2 should already be checked for being numeric values only
    private String multFunction(String operand1, String operand2){
        String result = "";
        int op1 = Integer.parseInt(operand1);
        int op2 = Integer.parseInt(operand2);
        
        int numResult = op1*op2;
        result = Integer.toString(numResult);
        
        return result;
    }
    
    //Note: By the time these are called, operand1 and operand2 should already be checked for being numeric values only
    private String divFunction(String operand1, String operand2){
        String result = "";
        int op1 = Integer.parseInt(operand1);
        int op2 = Integer.parseInt(operand2);
        
        int numResult = op1/op2;
        result = Integer.toString(numResult);
        
        return result;
    }
    
}
