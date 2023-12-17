import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.lang.String;

public interface JSON {
    public static JSON getJSON(String input) {
        input = removeWhiteSpace(input);
        if (input.charAt(0) == '[') {
            return new Array(input);
        } else if(input.charAt(0) == '{') {
            return new Object(input);
        } else {
            boolean quotes = false;
            boolean escape = false;
            String str = null;
            Boolean bool = null;
            Double floatNumber = null;
            Long integerNumber = null;
            for (int i = 0; i < input.length(); i++) {
                if (input.charAt(i) == '\\' && !escape) {
                    escape = true;
                    continue;
                }
                if (input.charAt(i) == '\"' && !escape)
                    quotes = !quotes;
                if (quotes && str == null)
                    str = "";
                if (quotes && (input.charAt(i) != '\"' || escape))
                    str += input.charAt(i);
                escape = false;
            }
            if (str == null) {
                try {
                    if(input.contains(".")) {
                        floatNumber = Double.parseDouble(input);
                    }else {
                        integerNumber = Long.parseLong(input);
                    };
                } catch (NumberFormatException e) {
                    if (input.equals("true"))
                        bool = true;
                    if (input.equals("false"))
                        bool = false;
                }
            }
            if (quotes || escape)
                throw new IllegalArgumentException("Input Incompleat");
            if (str != null)
                return new JSON.Value.String(str);
            if (bool != null)
                return new JSON.Value.Boolean(bool);
            if (floatNumber != null)
                return new JSON.Value.Float(floatNumber);
            if (integerNumber != null)
                return new JSON.Value.Integer(integerNumber);
        }
        return null;
    }

    public static String removeWhiteSpace(String str) {
        String out = "";
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isWhitespace(str.charAt(i)))
                out += str.charAt(i);
        }
        return out;
    }

    public static JSON getJSON(File file) {
        try {
            FileInputStream input = new FileInputStream(file);
            String str = new String(input.readAllBytes());

            input.close();
            return getJSON(str);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static class Array extends ArrayList<JSON> implements JSON {
        public Array() {}

        public Array(String input) {
            String str = "";
            int index = 1;
            int level = 0;
            while (index < (input.length() - 1)) {
                if (input.charAt(index) == ',' && level <= 0) {
                    add(JSON.getJSON(str));
                    str = "";
                } else
                    str += input.charAt(index);
                if (input.charAt(index) == '[' || input.charAt(index) == '{')
                    level++;
                if (input.charAt(index) == ']' || input.charAt(index) == '}')
                    level--;
                index++;
            }
            add(JSON.getJSON(str));
        }

        @Override
        public String toJSON() {
            String str = "[";
            int last = size() - 1;
            for(int i = 0; i <= last; i++) {
                str += get(i).toJSON();
                if(i != last) str += ",";
            }
            str += "]";
            return str;
        }
    }

    public static class Object extends ArrayList<JSON.Object.Value> implements JSON {
        public Object() {}

        public Object(String input) {
            String name = "";
            String str = "";
            boolean isName = true;
            int index = 1;
            int level = 0;
            boolean quotes = false;
            boolean escape = false;
            while (index < (input.length() - 1)) {
                if (isName) {
                    if (!quotes && input.charAt(index) == ':')
                        isName = false;
                    if (input.charAt(index) == '\\' && !escape) {
                        escape = true;
                        continue;
                    }
                    if (input.charAt(index) == '\"' && !escape)
                        quotes = !quotes;
                    if (quotes && str == null)
                        name = "";
                    if (quotes && (input.charAt(index) != '\"' || escape))
                        name += input.charAt(index);
                    escape = false;
                } else {
                    if (input.charAt(index) == ',' && level <= 0) {
                        add(new Value(name, JSON.getJSON(str)));
                        name = "";
                        str = "";
                        isName = true;
                    } else
                        str += input.charAt(index);
                    if (input.charAt(index) == '[' || input.charAt(index) == '{')
                        level++;
                    if (input.charAt(index) == ']' || input.charAt(index) == '}')
                        level--;
                }
                index++;
            }
            add(new Value(name, JSON.getJSON(str)));
        }

        public JSON getItem(String name) {
            for(Value item : this) {
                if(item.name.equals(name)) return item.value;
            }
            return null;
        }

        public static class Value {
            public String name;
            public JSON value;

            public Value(String name, JSON value) {
                this.name = name;
                this.value = value;
            }
        }
    
        @Override
        public String toJSON() {
            String str = "{";
            int last = size() - 1;
            for(int i = 0; i <= last; i++) {
                str += "\"" + get(i).name + "\"";
                str += ":";
                str += get(i).value.toJSON();
                if(i != last) str += ",";
            }
            str += "}";
            return str;
        }
    }

    public interface Value extends JSON {
        public class Float implements Value {
            public double value;

            public Float(double num) {
                value = num;
            }

            public double getFloat() {
                return value;
            }

            @Override
            public java.lang.String toJSON() {
                return java.lang.String.valueOf(value);
            }
        }

        public class Integer implements Value {
            public long value;

            public Integer(long num) {
                value = num;
            }

            public long getInteger() {
                return value;
            }

            @Override
            public java.lang.String toJSON() {
                return java.lang.String.valueOf(value);
            }
        }

        public class Boolean implements Value {
            public boolean value;

            public Boolean(boolean bool) {
                value = bool;
            }

            public boolean getBoolean() {
                return value;
            }

            @Override
            public java.lang.String toJSON() {
                return (value ? "true" : "false");
            }
        }

        public class String implements Value {
            public java.lang.String value;

            public String(java.lang.String str) {
                value = str;
            }

            public java.lang.String getString() {
                return value;
            }

            @Override
            public java.lang.String toJSON() {
                return "\"" + value + "\"";
            }
        }
    }

    public abstract String toJSON();
}