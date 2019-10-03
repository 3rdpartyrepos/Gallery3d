package it.pgp.misc;

import java.io.File;
import java.util.*;


public class DirTreeWalker implements Iterator<File> {
    protected final Stack<File> stack = new Stack<>();
    protected boolean filesOnly;
    protected FilenamePredicate filter;

    @FunctionalInterface
    public interface FilenamePredicate {
        boolean test(String s);
    }

    public static Iterable<File> traverse(boolean filesOnly_, FilenamePredicate filter_, Collection<File> collection) {
        return () -> new DirTreeWalker(){{
            filesOnly = filesOnly_;
            filter = filter_;
            for (File x : collection)
                stack.push(x);
        }};
    }

    public static final Set<String> allowedImageFormats = new HashSet<>(Arrays.asList(".bmp",".gif",".jpg",".png"));
    public static final FilenamePredicate filterImages = s -> s.length()>= 4 && allowedImageFormats.contains(s.substring(s.length()-4));

    public static Iterable<File> traverse(boolean filesOnly_, FilenamePredicate filter_, File... args) {
        return traverse(filesOnly_, filter_, Arrays.asList(args));
    }

    @Override
    public boolean hasNext() {
        if(filesOnly) {
            // get next regular file from stack (if file is dir, expand it and go on)
            try {
                for(;;) {
                    File f = stack.pop();
                    while(f.isDirectory()) {
                        File[] l = f.listFiles();
                        if(l != null)
                            for(File x : l) stack.push(x);
                        f = stack.pop();
                    }

                    if(filter==null || filter.test(f.getName())) {
                        stack.push(f); // f is a regular file here
                        return true;
                    }
                }
            }
            catch(EmptyStackException e) {
                return false;
            }
        }
        else return !stack.isEmpty();
    }

    @Override
    public File next() {
        File f = stack.pop();
        if (f.isDirectory()) {
            File[] l = f.listFiles();
            if(l != null)
                for (File x : l) stack.push(x);
        }
        return f;
    }
}