package org.bbop.util;

/*
Copyright 2006 Jerry Huxtable

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

import java.awt.*;
import java.awt.geom.*;

import org.apache.log4j.*;

public class ZigZagStroke implements Stroke {

	//initialize logger
	protected final static Logger logger = Logger.getLogger(ZigZagStroke.class);
	private float amplitude = 5.0f;
	private float wavelength = 10.0f;
    private Stroke stroke;
	private static final float FLATNESS = 1;

	public ZigZagStroke( Stroke stroke, float amplitude, float wavelength ) {
        this.stroke = stroke;
        this.amplitude = amplitude;
        this.wavelength = wavelength;
	}

	@Override
	public Shape createStrokedShape( Shape shape ) {
		GeneralPath result = new GeneralPath();
		PathIterator it = new FlatteningPathIterator( shape.getPathIterator( null ), FLATNESS );
		float points[] = new float[6];
		float moveX = 0, moveY = 0;
		float lastX = 0, lastY = 0;
		float thisX = 0, thisY = 0;
		int type = 0;
		float next = 0;
        int phase = 0;


		while ( !it.isDone() ) {
			type = it.currentSegment( points );
			switch( type ){
			case PathIterator.SEG_MOVETO:
				moveX = lastX = points[0];
				moveY = lastY = points[1];
				result.moveTo( moveX, moveY );
                next = wavelength/2;
				break;

			case PathIterator.SEG_CLOSE:
				points[0] = moveX;
				points[1] = moveY;
				// Fall into....

			case PathIterator.SEG_LINETO:
				thisX = points[0];
				thisY = points[1];
				float dx = thisX-lastX;
				float dy = thisY-lastY;
				float distance = (float)Math.sqrt( dx*dx + dy*dy );
				if ( distance >= next ) {
					float r = 1.0f/distance;
					while ( distance >= next ) {
						float x = lastX + next*dx*r;
						float y = lastY + next*dy*r;
                        float intermediate_x;
                        float intermediate_y;
						if ( (phase & 1) == 0 ) { 
							intermediate_x = x+amplitude*dy*r;
							intermediate_y = y-amplitude*dx*r;
						} else {
                            intermediate_x = x-amplitude*dy*r;
                            intermediate_y = y+amplitude*dx*r ;
						}
                        result.lineTo(intermediate_x, intermediate_y);
						next += wavelength;
						phase++;
					}
				}
				next -= distance;
				lastX = thisX;
				lastY = thisY;
                if ( type == PathIterator.SEG_CLOSE )
                    result.closePath();
				break;
			}
			it.next();
		}

		return stroke.createStrokedShape( result );
	}

}