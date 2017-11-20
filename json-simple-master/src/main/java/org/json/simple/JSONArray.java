/*
 * $Id: JSONArray.java,v 1.1 2006/04/15 14:10:48 platform Exp $
 * Created on 2006-4-10
 */
package org.json.simple;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * A JSON array. JSONObject supports java.util.List interface.
 * 
 * @author FangYidong<fangyidong@yahoo.com.cn>
 */
@SuppressWarnings("unchecked")
public class JSONArray extends ArrayList implements JSONAware, JSONStreamAware {
	private static final long serialVersionUID = 3957988303675231981L;
	
	/**
	 * Constructs an empty JSONArray.
	 */
	public JSONArray(){
		super();
	}
	
	/**
	 * Constructs a JSONArray containing the elements of the specified
	 * collection, in the order they are returned by the collection's iterator.
	 * 
	 * @param c the collection whose elements are to be placed into this JSONArray
	 */
	public JSONArray( final Collection c){
		super(c);
	}
	
    /**
     * Encode a list into JSON text and write it to out. 
     * If this list is also a JSONStreamAware or a JSONAware, JSONStreamAware and JSONAware specific behaviours will be ignored at this top level.
     * 
     * @see org.json.simple.JSONValue#writeJSONString(Object, Writer)
     * 
     * @param collection
     * @param out
     */
	public static void writeJSONString( final Collection collection, final Writer out) throws IOException{
		if(collection == null){
			out.write("null");
			return;
		}
		
		boolean first = true;
		Iterator iter=collection.iterator();
		
        out.write('[');
		while(iter.hasNext()){
            if(first)
                first = false;
            else
                out.write(',');
            
			Object value=iter.next();
			if(value == null){
				out.write("null");
				continue;
			}
			
			JSONValue.writeJSONString(value, out);
		}
		out.write(']');
	}
	
	public void writeJSONString( final Writer out) throws IOException{
		writeJSONString(this, out);
	}
	
	/**
	 * Convert a list to JSON text. The result is a JSON array. 
	 * If this list is also a JSONAware, JSONAware specific behaviours will be omitted at this top level.
	 * 
	 * @see org.json.simple.JSONValue#toJSONString(Object)
	 * 
	 * @param collection
	 * @return JSON text, or "null" if list is null.
	 */
	public static String toJSONString( final Collection collection){
		final StringWriter writer = new StringWriter();
		
		try {
			writeJSONString(collection, writer);
			return writer.toString();
		} catch(IOException e){
			// This should never happen for a StringWriter
			throw new RuntimeException(e);
		}
	}

	public static void writeJSONString( final byte[] array, final Writer out) throws IOException{
		if(array == null){
			out.write("null");
		} else if(array.length == 0) {
			out.write("[]");
		} else {
			out.write("[");
			out.write(String.valueOf(array[0]));
			
			for(int i = 1; i < array.length; i++){
				out.write(",");
				out.write(String.valueOf(array[i]));
			}
			
			out.write("]");
		}
	}
	
	public static String toJSONString( final byte[] array){
		final StringWriter writer = new StringWriter();
		
		try {
			writeJSONString(array, writer);
			return writer.toString();
		} catch(IOException e){
			// This should never happen for a StringWriter
			throw new RuntimeException(e);
		}
	}
	
	public static void writeJSONString( final short[] array, final Writer out) throws IOException{
		if(array == null){
			out.write("null");
		} else if(array.length == 0) {
			out.write("[]");
		} else {
			out.write("[");
			out.write(String.valueOf(array[0]));
			
			for(int i = 1; i < array.length; i++){
				out.write(",");
				out.write(String.valueOf(array[i]));
			}
			
			out.write("]");
		}
	}
	
	public static String toJSONString( final short[] array){
		final StringWriter writer = new StringWriter();
		
		try {
			writeJSONString(array, writer);
			return writer.toString();
		} catch(IOException e){
			// This should never happen for a StringWriter
			throw new RuntimeException(e);
		}
	}
	
	public static void writeJSONString( final int[] array, final Writer out) throws IOException{
		if(array == null){
			out.write("null");
		} else if(array.length == 0) {
			out.write("[]");
		} else {
			out.write("[");
			out.write(String.valueOf(array[0]));
			
			for(int i = 1; i < array.length; i++){
				out.write(",");
				out.write(String.valueOf(array[i]));
			}
			
			out.write("]");
		}
	}
	
	public static String toJSONString( final int[] array){
		final StringWriter writer = new StringWriter();
		
		try {
			writeJSONString(array, writer);
			return writer.toString();
		} catch(IOException e){
			// This should never happen for a StringWriter
			throw new RuntimeException(e);
		}
	}
	
	public static void writeJSONString( final long[] array, final Writer out) throws IOException{
		if(array == null){
			out.write("null");
		} else if(array.length == 0) {
			out.write("[]");
		} else {
			out.write("[");
			out.write(String.valueOf(array[0]));
			
			for(int i = 1; i < array.length; i++){
				out.write(",");
				out.write(String.valueOf(array[i]));
			}
			
			out.write("]");
		}
	}
	
	public static String toJSONString( final long[] array){
		final StringWriter writer = new StringWriter();
		
		try {
			writeJSONString(array, writer);
			return writer.toString();
		} catch(IOException e){
			// This should never happen for a StringWriter
			throw new RuntimeException(e);
		}
	}
	
	public static void writeJSONString( final float[] array, final Writer out) throws IOException{
		if(array == null){
			out.write("null");
		} else if(array.length == 0) {
			out.write("[]");
		} else {
			out.write("[");
			out.write(String.valueOf(array[0]));
			
			for(int i = 1; i < array.length; i++){
				out.write(",");
				out.write(String.valueOf(array[i]));
			}
			
			out.write("]");
		}
	}
	
	public static String toJSONString( final float[] array){
		final StringWriter writer = new StringWriter();
		
		try {
			writeJSONString(array, writer);
			return writer.toString();
		} catch(IOException e){
			// This should never happen for a StringWriter
			throw new RuntimeException(e);
		}
	}
	
	public static void writeJSONString( final double[] array, final Writer out) throws IOException{
		if(array == null){
			out.write("null");
		} else if(array.length == 0) {
			out.write("[]");
		} else {
			out.write("[");
			out.write(String.valueOf(array[0]));
			
			for(int i = 1; i < array.length; i++){
				out.write(",");
				out.write(String.valueOf(array[i]));
			}
			
			out.write("]");
		}
	}
	
	public static String toJSONString( final double[] array){
		final StringWriter writer = new StringWriter();
		
		try {
			writeJSONString(array, writer);
			return writer.toString();
		} catch(IOException e){
			// This should never happen for a StringWriter
			throw new RuntimeException(e);
		}
	}
	
	public static void writeJSONString( final boolean[] array, final Writer out) throws IOException{
		if(array == null){
			out.write("null");
		} else if(array.length == 0) {
			out.write("[]");
		} else {
			out.write("[");
			out.write(String.valueOf(array[0]));
			
			for(int i = 1; i < array.length; i++){
				out.write(",");
				out.write(String.valueOf(array[i]));
			}
			
			out.write("]");
		}
	}
	
	public static String toJSONString( final boolean[] array){
		final StringWriter writer = new StringWriter();
		
		try {
			writeJSONString(array, writer);
			return writer.toString();
		} catch(IOException e){
			// This should never happen for a StringWriter
			throw new RuntimeException(e);
		}
	}
	
	public static void writeJSONString( final char[] array, final Writer out) throws IOException{
		if(array == null){
			out.write("null");
		} else if(array.length == 0) {
			out.write("[]");
		} else {
			out.write("[\"");
			out.write(String.valueOf(array[0]));
			
			for(int i = 1; i < array.length; i++){
				out.write("\",\"");
				out.write(String.valueOf(array[i]));
			}
			
			out.write("\"]");
		}
	}
	
	public static String toJSONString( final char[] array){
		final StringWriter writer = new StringWriter();
		
		try {
			writeJSONString(array, writer);
			return writer.toString();
		} catch(IOException e){
			// This should never happen for a StringWriter
			throw new RuntimeException(e);
		}
	}
	
	public static void writeJSONString( final Object[] array, final Writer out) throws IOException{
		if(array == null){
			out.write("null");
		} else if(array.length == 0) {
			out.write("[]");
		} else {
			out.write("[");
			JSONValue.writeJSONString(array[0], out);
			
			for(int i = 1; i < array.length; i++){
				out.write(",");
				JSONValue.writeJSONString(array[i], out);
			}
			
			out.write("]");
		}
	}
	
	public static String toJSONString( final Object[] array){
		final StringWriter writer = new StringWriter();
		
		try {
			writeJSONString(array, writer);
			return writer.toString();
		} catch(IOException e){
			// This should never happen for a StringWriter
			throw new RuntimeException(e);
		}
	}
	
	public String toJSONString(){
		return toJSONString(this);
	}

	/**
	 * Returns a string representation of this array. This is equivalent to
	 * calling {@link JSONArray#toJSONString()}.
	 */
	public String toString() {
		return toJSONString();
	}
}
