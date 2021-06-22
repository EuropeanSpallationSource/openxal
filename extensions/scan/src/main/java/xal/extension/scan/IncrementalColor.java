package xal.extension.scan;

import java.awt.*;

public class IncrementalColor{

	//Color.black,
	//Color.blue,
	//Color.green,
	//Color.cyan,
	//Color.magenta,
	//Color.pink,
	//Color.orange,
	//Color.yellow,
	//Color.red

    private static Color[] incrColor = {
	Color.blue,
	Color.green,
	Color.red,
	Color.magenta,
	Color.yellow,
	Color.orange
    };

    private IncrementalColor(){
    }

    public static Color getColor(int index){
	index = index %  incrColor.length;
        return incrColor[index];
    }
}
