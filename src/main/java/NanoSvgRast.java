import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class NanoSvgRast {
    /*
     * Copyright (c) 2013-14 Mikko Mononen memon@inside.org
     *
     * This software is provided 'as-is', without any express or implied
     * warranty.  In no event will the authors be held liable for any damages
     * arising from the use of this software.
     *
     * Permission is granted to anyone to use this software for any purpose,
     * including commercial applications, and to alter it and redistribute it
     * freely, subject to the following restrictions:
     *
     * 1. The origin of this software must not be misrepresented; you must not
     * claim that you wrote the original software. If you use this software
     * in a product, an acknowledgment in the product documentation would be
     * appreciated but is not required.
     * 2. Altered source versions must be plainly marked as such, and must not be
     * misrepresented as being the original software.
     * 3. This notice may not be removed or altered from any source distribution.
     *
     * The polygon rasterization is heavily based on stb_truetype rasterizer
     * by Sean Barrett - http://nothings.org/
     *
     */

    static public final float NSVG_PI = (3.14159265358979323846264338327f);


    static public final int NSVG__SUBSAMPLES	= 5;
    static public final int NSVG__FIXSHIFT		= 10;
    static public final int NSVG__FIX			= (1 << NSVG__FIXSHIFT);
    static public final int NSVG__FIXMASK		= (NSVG__FIX-1);
    static public final int NSVG__MEMPAGE_SIZE	= 1024;

    static public final int NSVG_PAINT_NONE = 0;
    static public final int NSVG_PAINT_COLOR = 1;
    static public final int NSVG_PAINT_LINEAR_GRADIENT = 2;
    static public final int NSVG_PAINT_RADIAL_GRADIENT = 3;

    static public final int NSVG_SPREAD_PAD = 0;
    static public final int NSVG_SPREAD_REFLECT = 1;
    static public final int NSVG_SPREAD_REPEAT = 2;

    static public final int NSVG_JOIN_MITER = 0;
    static public final int NSVG_JOIN_ROUND = 1;
    static public final int NSVG_JOIN_BEVEL = 2;

    static public final int NSVG_CAP_BUTT = 0;
    static public final int NSVG_CAP_ROUND = 1;
    static public final int NSVG_CAP_SQUARE = 2;

    static public final int NSVG_FILLRULE_NONZERO = 0;
    static public final int NSVG_FILLRULE_EVENODD = 1;

    static public final int NSVG_FLAGS_VISIBLE = 0x01;

    static public final int NSVG_PT_CORNER = 0x01;
    static public final int NSVG_PT_CORNER9 = 9;
    static public final int NSVG_PT_BEVEL = 0x02;
    static public final int NSVG_PT_LEFT = 0x04;

    //enum NSVGpointFlags
    //{
    //    NSVG_PT_CORNER(0x01),
    //    NSVG_PT_BEVEL(0x02),
    //    NSVG_PT_LEFT(0x04)
    //};


    //enum NSVGpaintType {
    //    NSVG_PAINT_NONE = 0,
    //    NSVG_PAINT_COLOR = 1,
    //    NSVG_PAINT_LINEAR_GRADIENT = 2,
    //    NSVG_PAINT_RADIAL_GRADIENT = 3
    //};
//
    //enum NSVGspreadType {
    //    NSVG_SPREAD_PAD = 0,
    //    NSVG_SPREAD_REFLECT = 1,
    //    NSVG_SPREAD_REPEAT = 2
    //};

    //enum NSVGlineJoin {
    //    NSVG_JOIN_MITER = 0,
    //    NSVG_JOIN_ROUND = 1,
    //    NSVG_JOIN_BEVEL = 2
    //};

    //enum NSVGlineCap {
    //    NSVG_CAP_BUTT = 0,
    //    NSVG_CAP_ROUND = 1,
    //    NSVG_CAP_SQUARE = 2
    //};

    //enum NSVGfillRule {
    //    NSVG_FILLRULE_NONZERO = 0,
    //    NSVG_FILLRULE_EVENODD = 1
    //};

    //enum NSVGflags {
    //    NSVG_FLAGS_VISIBLE = 0x01
    //};

    static public class NSVGgradientStop {
        int color;
        float offset;
    }

    static public class NSVGgradient {
        float[] xform = new float[6];
        char spread;
        float fx, fy;
        int nstops;
        NSVGgradientStop[] stops = new NSVGgradientStop[1];
    }

    static public class NSVGpaint {
        char type;
        int color;
        NSVGgradient gradient;
    }

    static public class NSVGpath
    {
        float[] pts;					// Cubic bezier points: x0,y0, [cpx1,cpx1,cpx2,cpy2,x1,y1], ...
        int npts;					// Total number of bezier points.
        boolean closed;				// Flag indicating if shapes should be treated as closed.
        float[] bounds = new float[4];			// Tight bounding box of the shape [minx,miny,maxx,maxy].
        NSVGpath next;		// Pointer to next path, or NULL if last element.
    }

    static public class NSVGshape
    {
        char[] id = new char[64];				// Optional 'id' attr of the shape or its group
        NSVGpaint fill;				// Fill paint
        NSVGpaint stroke;			// Stroke paint
        float opacity;				// Opacity of the shape.
        float strokeWidth;			// Stroke width (scaled).
        float strokeDashOffset;		// Stroke dash offset (scaled).
        float[] strokeDashArray = new float[8];			// Stroke dash array (scaled).
        char strokeDashCount;				// Number of dash values in dash array.
        char strokeLineJoin;		// Stroke join type.
        char strokeLineCap;			// Stroke cap type.
        float miterLimit;			// Miter limit
        char fillRule;				// Fill rule, see NSVGfillRule.
        int flags;		// Logical or of NSVG_FLAGS_* flags
        float[] bounds = new float[4];			// Tight bounding box of the shape [minx,miny,maxx,maxy].
        NSVGpath paths;			// Linked list of paths in the image.
        NSVGshape next;		// Pointer to next shape, or NULL if last element.
    }

    static public class NSVGimage
    {
        float width;				// Width of the image.
        float height;				// Height of the image.
        NSVGshape shapes;			// Linked list of shapes in the image.
    }


    static public class NSVGedge implements Comparable<NSVGedge> {
        float x0,y0, x1,y1;
        int dir;
        NSVGedge next;

        @Override
        public int compareTo(@NotNull NSVGedge o) {
            final NSVGedge a = this;
            final NSVGedge b = o;

            if (a.y0 < b.y0) return -1;
            if (a.y0 > b.y0) return  1;
            return 0;
        }
    }

    static public class NSVGpoint {
        float x = 0f, y = 0f;
        float dx = 0f, dy = 0f;
        float len = 0f;
        float dmx = 0f, dmy = 0f;
        int flags = 0;
        public NSVGpoint() {
        }
        public NSVGpoint(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    static public class NSVGactiveEdge {
        int x,dx;
        float ey;
        int dir;
        NSVGactiveEdge next = null;
    }

    static public class NSVGmemPage {
        byte[] mem = new byte[NSVG__MEMPAGE_SIZE];
        int size = 0;
        NSVGmemPage next = null;
    }

    static public class NSVGcachedPaint {
        char type;
        char spread;
        float[] xform = new float[6];
        int[] colors = new int[256];
    }

    static public class NSVGrasterizer
    {
        float px, py;

        float tessTol;
        float distTol;

        NSVGedge[] edges = new NSVGedge[0];
        int nedges = 0;
        int cedges = 0;

        NSVGpoint[] points = new NSVGpoint[0];
        int npoints = 0;
        int cpoints = 0;

        NSVGpoint[] points2 = new NSVGpoint[0];
        int npoints2 = 0;
        int cpoints2 = 0;

        NSVGactiveEdge freelist;
        NSVGmemPage pages;
        NSVGmemPage curpage;

        byte[] scanline = new byte[0];
        int cscanline = 0;

        byte[] bitmap;
        int width, height, stride;
    };

/* Example Usage:
	// Load SVG
	NSVGimage image;
	image = nsvgParseFromFile("test.svg", "px", 96);

	// Create rasterizer (can be used to render multiple images).
	struct NSVGrasterizer rast = nsvgCreateRasterizer();
	// Allocate memory for image
	unsigned char* img = malloc(w*h*4);
	// Rasterize
	nsvgRasterize(rast, image, 0,0,1, img, w, h, w*4);
*/

    // Allocated rasterizer context.
    public NSVGrasterizer nsvgCreateRasterizer()
    {
        NSVGrasterizer r = new NSVGrasterizer();
        r.tessTol = 0.25f;
        r.distTol = 0.01f;
        return r;
    }

    // Deletes rasterizer context.
    public void nsvgDeleteRasterizer(NSVGrasterizer r)
    {
        NSVGmemPage p;

        if (r == null) return;

        p = r.pages;
        while (p != null) {
            NSVGmemPage next = p.next;
            //free(p);
            p = null;
            p = next;
        }

        if (r.edges != null) r.edges = null; // free
        if (r.points != null) r.points = null; // free
        if (r.points2 != null) r.points2 = null; // free
        if (r.scanline != null) r.scanline = null; // free

        //free(r);
        r = null;
    }

    public NSVGmemPage nsvg__nextPage(NSVGrasterizer r, NSVGmemPage cur)
    {
        // If using existing chain, return the next page in chain
        if (cur != null && cur.next != null) {
            return cur.next;
        }

        // Alloc new page
        final NSVGmemPage newp = new NSVGmemPage();

        // Add to linked list
        if (cur != null)
            cur.next = newp;
        else
            r.pages = newp;

        return newp;
    }

    public void nsvg__resetPool(NSVGrasterizer r)
    {
        NSVGmemPage p = r.pages;
        while (p != null) {
            p.size = 0;
            p = p.next;
        }
        r.curpage = r.pages;
    }

    //static byte[] nsvg__alloc(NSVGrasterizer r, int size)
    //{
    //    byte[] buf;
    //    if (size > NSVG__MEMPAGE_SIZE) return null;
    //    if (r.curpage == null || r.curpage.size+size > NSVG__MEMPAGE_SIZE) {
    //        r.curpage = nsvg__nextPage(r, r.curpage);
    //    }
    //    buf = r.curpage.mem[r.curpage.size];
    //    r.curpage.size += size;
    //    return buf;
    //}

    public boolean nsvg__ptEquals(float x1, float y1, float x2, float y2, float tol)
    {
        float dx = x2 - x1;
        float dy = y2 - y1;
        return dx*dx + dy*dy < tol*tol;
    }

    public void nsvg__addPathPoint(NSVGrasterizer r, float x, float y, int flags)
    {
        NSVGpoint pt;

        if (r.npoints > 0) {
            pt = r.points[r.npoints-1];
            if (nsvg__ptEquals(pt.x,pt.y, x,y, r.distTol)) {
                pt.flags = (int)(pt.flags | flags);
                return;
            }
        }

        if (r.npoints+1 > r.cpoints) {
            r.cpoints = r.cpoints > 0 ? r.cpoints * 2 : 64;
            r.points = Arrays.copyOf(r.points, r.cpoints);
            if (r.points == null) return;
        }

        pt = r.points[r.npoints];
        pt.x = x;
        pt.y = y;
        pt.flags = (int)flags;
        r.npoints++;
    }

    public void nsvg__appendPathPoint(NSVGrasterizer r, NSVGpoint pt)
    {
        if (r.npoints+1 > r.cpoints) {
            r.cpoints = r.cpoints > 0 ? r.cpoints * 2 : 64;
            r.points = Arrays.copyOf(r.points, r.cpoints);
            if (r.points == null) return;
        }
        r.points[r.npoints] = pt;
        r.npoints++;
    }

    public void nsvg__duplicatePoints(NSVGrasterizer r)
    {
        if (r.npoints > r.cpoints2) {
            r.cpoints2 = r.npoints;
            r.points2 = Arrays.copyOf(r.points2, r.cpoints2);
        }

        System.arraycopy(r.points2, 0, r.points, 0, r.npoints);
        r.npoints2 = r.npoints;
    }

    static private NSVGedge[] realloc(NSVGedge[] items, int size) {
        int pos = (items != null) ? items.length : 0;
        NSVGedge[] out = (items != null) ? Arrays.copyOf(items, size) : new NSVGedge[size];
        for (int n = pos; n < size; n++) {
            out[n] = new NSVGedge();
        }
        return out;
    }

    public void nsvg__addEdge(NSVGrasterizer r, float x0, float y0, float x1, float y1)
    {
        NSVGedge e;

        // Skip horizontal edges
        if (y0 == y1)
            return;

        if (r.nedges+1 > r.cedges) {
            r.cedges = r.cedges > 0 ? r.cedges * 2 : 64;
            r.edges = realloc(r.edges, r.cedges);
        }

        e = r.edges[r.nedges];
        r.nedges++;

        if (y0 < y1) {
            e.x0 = x0;
            e.y0 = y0;
            e.x1 = x1;
            e.y1 = y1;
            e.dir = +1;
        } else {
            e.x0 = x1;
            e.y0 = y1;
            e.x1 = x0;
            e.y1 = y0;
            e.dir = -1;
        }
    }

    //static float nsvg__normalize(float[] x, float[] y)
    //{
    //    float d = sqrtf((x[0])*(x[0]) + (y[0])*(y[0]));
    //    if (d > 1e-6f) {
    //        float id = 1.0f / d;
	//	x[0] *= id;
	//	y[0] *= id;
    //    }
    //    return d;
    //}

    float normalX = 0f;
    float normalY = 0f;
    float normalD = 0f;

    public float nsvg__normalizeXY(float x, float y, int index)
    {
        float d = sqrtf((x)*(x) + (y)*(y));
        if (d > 1e-6f) {
            float id = 1.0f / d;
            x *= id;
            y *= id;
        }
        normalX = x;
        normalY = y;
        normalD = d;
        if (index == -1) return d;
        return (index == 0) ? x : y;
    }
    public float nsvg__normalizeD(float x, float y)
    {
        return nsvg__normalizeXY(x, y, -1);
    }

    public float nsvg__normalizeX(float x, float y)
    {
        return nsvg__normalizeXY(x, y, 0);
    }

    public float nsvg__normalizeY(float x, float y)
    {
        return nsvg__normalizeXY(x, y, 1);
    }

    public float nsvg__absf(float x) { return x < 0 ? -x : x; }

    public void nsvg__flattenCubicBez(
        NSVGrasterizer r,
        float x1, float y1, float x2, float y2,
        float x3, float y3, float x4, float y4,
        int level, int type
    ) {
        float x12,y12,x23,y23,x34,y34,x123,y123,x234,y234,x1234,y1234;
        float dx,dy,d2,d3;

        if (level > 10) return;

        x12 = (x1+x2)*0.5f;
        y12 = (y1+y2)*0.5f;
        x23 = (x2+x3)*0.5f;
        y23 = (y2+y3)*0.5f;
        x34 = (x3+x4)*0.5f;
        y34 = (y3+y4)*0.5f;
        x123 = (x12+x23)*0.5f;
        y123 = (y12+y23)*0.5f;

        dx = x4 - x1;
        dy = y4 - y1;
        d2 = nsvg__absf(((x2 - x4) * dy - (y2 - y4) * dx));
        d3 = nsvg__absf(((x3 - x4) * dy - (y3 - y4) * dx));

        if ((d2 + d3)*(d2 + d3) < r.tessTol * (dx*dx + dy*dy)) {
            nsvg__addPathPoint(r, x4, y4, type);
            return;
        }

        x234 = (x23+x34)*0.5f;
        y234 = (y23+y34)*0.5f;
        x1234 = (x123+x234)*0.5f;
        y1234 = (y123+y234)*0.5f;

        nsvg__flattenCubicBez(r, x1,y1, x12,y12, x123,y123, x1234,y1234, level+1, 0);
        nsvg__flattenCubicBez(r, x1234,y1234, x234,y234, x34,y34, x4,y4, level+1, type);
    }

    public void nsvg__flattenShape(NSVGrasterizer r, NSVGshape shape, float scale)
    {
        int i, j;
        NSVGpath path;

        for (path = shape.paths; path != null; path = path.next) {
            r.npoints = 0;
            // Flatten path
            nsvg__addPathPoint(r, path.pts[0]*scale, path.pts[1]*scale, 0);
            for (i = 0; i < path.npts-1; i += 3) {
                float[] p = path.pts;
                int pp = i*2;
                nsvg__flattenCubicBez(r, p[pp + 0]*scale,p[pp + 1]*scale, p[pp + 2]*scale,p[pp + 3]*scale, p[pp + 4]*scale,p[pp + 5]*scale, p[pp + 6]*scale,p[pp + 7]*scale, 0, 0);
            }
            // Close path
            nsvg__addPathPoint(r, path.pts[0]*scale, path.pts[1]*scale, 0);
            // Build edges
            for (i = 0, j = r.npoints-1; i < r.npoints; j = i++)
                nsvg__addEdge(r, r.points[j].x, r.points[j].y, r.points[i].x, r.points[i].y);
        }
    }

    public void nsvg__initClosed(NSVGpoint left, NSVGpoint right, NSVGpoint p0, NSVGpoint p1, float lineWidth)
    {
        float w = lineWidth * 0.5f;
        float dx = p1.x - p0.x;
        float dy = p1.y - p0.y;
        float len = nsvg__normalizeD(dx, dy);
        dx = normalX;
        dy = normalY;
        float px = p0.x + dx*len*0.5f, py = p0.y + dy*len*0.5f;
        float dlx = dy, dly = -dx;
        float lx = px - dlx*w, ly = py - dly*w;
        float rx = px + dlx*w, ry = py + dly*w;
        left.x = lx; left.y = ly;
        right.x = rx; right.y = ry;
    }

    public void nsvg__buttCap(NSVGrasterizer r, NSVGpoint left, NSVGpoint right, NSVGpoint p, float dx, float dy, float lineWidth, boolean connect)
    {
        float w = lineWidth * 0.5f;
        float px = p.x, py = p.y;
        float dlx = dy, dly = -dx;
        float lx = px - dlx*w, ly = py - dly*w;
        float rx = px + dlx*w, ry = py + dly*w;

        nsvg__addEdge(r, lx, ly, rx, ry);

        if (connect) {
            nsvg__addEdge(r, left.x, left.y, lx, ly);
            nsvg__addEdge(r, rx, ry, right.x, right.y);
        }
        left.x = lx; left.y = ly;
        right.x = rx; right.y = ry;
    }

    public void nsvg__squareCap(NSVGrasterizer r, NSVGpoint left, NSVGpoint right, NSVGpoint p, float dx, float dy, float lineWidth, boolean connect)
    {
        float w = lineWidth * 0.5f;
        float px = p.x - dx*w, py = p.y - dy*w;
        float dlx = dy, dly = -dx;
        float lx = px - dlx*w, ly = py - dly*w;
        float rx = px + dlx*w, ry = py + dly*w;

        nsvg__addEdge(r, lx, ly, rx, ry);

        if (connect) {
            nsvg__addEdge(r, left.x, left.y, lx, ly);
            nsvg__addEdge(r, rx, ry, right.x, right.y);
        }
        left.x = lx; left.y = ly;
        right.x = rx; right.y = ry;
    }

    public void nsvg__roundCap(NSVGrasterizer r, NSVGpoint left, NSVGpoint right, NSVGpoint p, float dx, float dy, float lineWidth, int ncap, boolean connect)
    {
        int i;
        float w = lineWidth * 0.5f;
        float px = p.x, py = p.y;
        float dlx = dy, dly = -dx;
        float lx = 0, ly = 0, rx = 0, ry = 0, prevx = 0, prevy = 0;

        for (i = 0; i < ncap; i++) {
            float a = (float)i/(float)(ncap-1)*NSVG_PI;
            float ax = cosf(a) * w, ay = sinf(a) * w;
            float x = px - dlx*ax - dx*ay;
            float y = py - dly*ax - dy*ay;

            if (i > 0)
                nsvg__addEdge(r, prevx, prevy, x, y);

            prevx = x;
            prevy = y;

            if (i == 0) {
                lx = x; ly = y;
            } else if (i == ncap-1) {
                rx = x; ry = y;
            }
        }

        if (connect) {
            nsvg__addEdge(r, left.x, left.y, lx, ly);
            nsvg__addEdge(r, rx, ry, right.x, right.y);
        }

        left.x = lx; left.y = ly;
        right.x = rx; right.y = ry;
    }

    public void nsvg__bevelJoin(NSVGrasterizer r, NSVGpoint left, NSVGpoint right, NSVGpoint p0, NSVGpoint p1, float lineWidth)
    {
        float w = lineWidth * 0.5f;
        float dlx0 = p0.dy, dly0 = -p0.dx;
        float dlx1 = p1.dy, dly1 = -p1.dx;
        float lx0 = p1.x - (dlx0 * w), ly0 = p1.y - (dly0 * w);
        float rx0 = p1.x + (dlx0 * w), ry0 = p1.y + (dly0 * w);
        float lx1 = p1.x - (dlx1 * w), ly1 = p1.y - (dly1 * w);
        float rx1 = p1.x + (dlx1 * w), ry1 = p1.y + (dly1 * w);

        nsvg__addEdge(r, lx0, ly0, left.x, left.y);
        nsvg__addEdge(r, lx1, ly1, lx0, ly0);

        nsvg__addEdge(r, right.x, right.y, rx0, ry0);
        nsvg__addEdge(r, rx0, ry0, rx1, ry1);

        left.x = lx1; left.y = ly1;
        right.x = rx1; right.y = ry1;
    }

    public void nsvg__miterJoin(NSVGrasterizer r, NSVGpoint left, NSVGpoint right, NSVGpoint p0, NSVGpoint p1, float lineWidth)
    {
        float w = lineWidth * 0.5f;
        float dlx0 = p0.dy, dly0 = -p0.dx;
        float dlx1 = p1.dy, dly1 = -p1.dx;
        float lx0, rx0, lx1, rx1;
        float ly0, ry0, ly1, ry1;

        if ((p1.flags & NSVG_PT_LEFT) != 0) {
            lx0 = lx1 = p1.x - p1.dmx * w;
            ly0 = ly1 = p1.y - p1.dmy * w;
            nsvg__addEdge(r, lx1, ly1, left.x, left.y);

            rx0 = p1.x + (dlx0 * w);
            ry0 = p1.y + (dly0 * w);
            rx1 = p1.x + (dlx1 * w);
            ry1 = p1.y + (dly1 * w);
            nsvg__addEdge(r, right.x, right.y, rx0, ry0);
            nsvg__addEdge(r, rx0, ry0, rx1, ry1);
        } else {
            lx0 = p1.x - (dlx0 * w);
            ly0 = p1.y - (dly0 * w);
            lx1 = p1.x - (dlx1 * w);
            ly1 = p1.y - (dly1 * w);
            nsvg__addEdge(r, lx0, ly0, left.x, left.y);
            nsvg__addEdge(r, lx1, ly1, lx0, ly0);

            rx0 = rx1 = p1.x + p1.dmx * w;
            ry0 = ry1 = p1.y + p1.dmy * w;
            nsvg__addEdge(r, right.x, right.y, rx1, ry1);
        }

        left.x = lx1; left.y = ly1;
        right.x = rx1; right.y = ry1;
    }

    static float atan2f(float a, float b) {
        return (float)Math.atan2(a, b);
    }


    static float sqrtf(float a) {
        return (float)Math.sqrt(a);
    }
    static float ceilf(float a) {
        return (float)Math.ceil(a);
    }
    static float fmodf(float a, float b) {
        //return (float)Math.mod(a);
        return a % b;
    }

    static float floorf(float a) {
        return (float)Math.floor(a);
    }

    static float acosf(float a) {
        return (float)Math.acos(a);
    }
    static float cosf(float a) {
        return (float)Math.cos(a);
    }

    static float sinf(float a) {
        return (float)Math.sin(a);
    }

    public void nsvg__roundJoin(NSVGrasterizer r, NSVGpoint left, NSVGpoint right, NSVGpoint p0, NSVGpoint p1, float lineWidth, int ncap)
    {
        int i, n;
        float w = lineWidth * 0.5f;
        float dlx0 = p0.dy, dly0 = -p0.dx;
        float dlx1 = p1.dy, dly1 = -p1.dx;
        float a0 = atan2f(dly0, dlx0);
        float a1 = atan2f(dly1, dlx1);
        float da = a1 - a0;
        float lx, ly, rx, ry;

        if (da < NSVG_PI) da += NSVG_PI*2;
        if (da > NSVG_PI) da -= NSVG_PI*2;

        n = (int)ceilf((nsvg__absf(da) / NSVG_PI) * (float)ncap);
        if (n < 2) n = 2;
        if (n > ncap) n = ncap;

        lx = left.x;
        ly = left.y;
        rx = right.x;
        ry = right.y;

        for (i = 0; i < n; i++) {
            float u = (float)i/(float)(n-1);
            float a = a0 + u*da;
            float ax = cosf(a) * w, ay = sinf(a) * w;
            float lx1 = p1.x - ax, ly1 = p1.y - ay;
            float rx1 = p1.x + ax, ry1 = p1.y + ay;

            nsvg__addEdge(r, lx1, ly1, lx, ly);
            nsvg__addEdge(r, rx, ry, rx1, ry1);

            lx = lx1; ly = ly1;
            rx = rx1; ry = ry1;
        }

        left.x = lx; left.y = ly;
        right.x = rx; right.y = ry;
    }

    public void nsvg__straightJoin(NSVGrasterizer r, NSVGpoint left, NSVGpoint right, NSVGpoint p1, float lineWidth)
    {
        float w = lineWidth * 0.5f;
        float lx = p1.x - (p1.dmx * w), ly = p1.y - (p1.dmy * w);
        float rx = p1.x + (p1.dmx * w), ry = p1.y + (p1.dmy * w);

        nsvg__addEdge(r, lx, ly, left.x, left.y);
        nsvg__addEdge(r, right.x, right.y, rx, ry);

        left.x = lx; left.y = ly;
        right.x = rx; right.y = ry;
    }

    static int nsvg__curveDivs(float r, float arc, float tol)
    {
        float da = acosf(r / (r + tol)) * 2.0f;
        int divs = (int)ceilf(arc / da);
        if (divs < 2) divs = 2;
        return divs;
    }


    public void nsvg__expandStroke(NSVGrasterizer r, NSVGpoint[] points, int npoints, boolean closed, int lineJoin, int lineCap, float lineWidth)
    {
        int ncap = nsvg__curveDivs(lineWidth*0.5f, NSVG_PI, r.tessTol);	// Calculate divisions per half circle.
        NSVGpoint left = new NSVGpoint();
        NSVGpoint right = new NSVGpoint();
        NSVGpoint firstLeft = new NSVGpoint();
        NSVGpoint firstRight = new NSVGpoint();
        NSVGpoint[] p = points;
        int p0, p1;
        int j, s, e;

        // Build stroke edges
        if (closed) {
            // Looping
            p0 = npoints-1;
            p1 = 0;
            s = 0;
            e = npoints;
        } else {
            // Add cap
            p0 = 0;
            p1 = 1;
            s = 1;
            e = npoints-1;
        }

        if (closed) {
            nsvg__initClosed(left, right, p[p0], p[p1], lineWidth);
            firstLeft = left;
            firstRight = right;
        } else {
            // Add cap
            float dx = p[p1].x - p[p0].x;
            float dy = p[p1].y - p[p0].y;
            nsvg__normalizeD(dx, dy);
            dx = normalX;
            dy = normalY;
            if (lineCap == NSVG_CAP_BUTT)
                nsvg__buttCap(r, left, right, p[p0], dx, dy, lineWidth, false);
		else if (lineCap == NSVG_CAP_SQUARE)
                nsvg__squareCap(r, left, right, p[p0], dx, dy, lineWidth, false);
		else if (lineCap == NSVG_CAP_ROUND)
                nsvg__roundCap(r, left, right, p[p0], dx, dy, lineWidth, ncap, false);
        }

        for (j = s; j < e; ++j) {
            if (((p[p1].flags & NSVG_PT_CORNER9) != 0)) {
                if (lineJoin == NSVG_JOIN_ROUND)
                    nsvg__roundJoin(r, left, right, p[p0], p[p1], lineWidth, ncap);
			else if (lineJoin == NSVG_JOIN_BEVEL || (p[p1].flags & NSVG_PT_BEVEL) != 0)
                    nsvg__bevelJoin(r, left, right, p[p0], p[p1], lineWidth);
			else
                nsvg__miterJoin(r, left, right, p[p0], p[p1], lineWidth);
            } else {
                nsvg__straightJoin(r, left, right, p[p1], lineWidth);
            }
            p0 = p1++;
        }

        if (closed) {
            // Loop it
            nsvg__addEdge(r, firstLeft.x, firstLeft.y, left.x, left.y);
            nsvg__addEdge(r, right.x, right.y, firstRight.x, firstRight.y);
        } else {
            // Add cap
            float dx = p[p1].x - p[p0].x;
            float dy = p[p1].y - p[p0].y;
            nsvg__normalizeD(dx, dy);
            dx = normalX;
            dy = normalY;
            if (lineCap == NSVG_CAP_BUTT)
                nsvg__buttCap(r, right, left, p[p1], -dx, -dy, lineWidth, true);
		else if (lineCap == NSVG_CAP_SQUARE)
                nsvg__squareCap(r, right, left, p[p1], -dx, -dy, lineWidth, true);
		else if (lineCap == NSVG_CAP_ROUND)
                nsvg__roundCap(r, right, left, p[p1], -dx, -dy, lineWidth, ncap, true);
        }
    }

    public void nsvg__prepareStroke(NSVGrasterizer r, float miterLimit, int lineJoin)
    {
        int i, j;
        int p0, p1;

        NSVGpoint[] p = r.points;

        p0 = r.npoints-1;
        p1 = 0;
        for (i = 0; i < r.npoints; i++) {
            // Calculate segment direction and length
            p[p0].dx = p[p1].x - p[p0].x;
            p[p0].dy = p[p1].y - p[p0].y;
            p[p0].len = nsvg__normalizeD(p[p0].dx, p[p0].dy);
            p[p0].dx = normalX;
            p[p0].dy = normalY;
            // Advance
            p0 = p1++;
        }

        // calculate joins
        p0 = r.npoints-1;
        p1 = 0;
        for (j = 0; j < r.npoints; j++) {
            float dlx0, dly0, dlx1, dly1, dmr2, cross;
            dlx0 = p[p0].dy;
            dly0 = -p[p0].dx;
            dlx1 = p[p1].dy;
            dly1 = -p[p1].dx;
            // Calculate extrusions
            p[p1].dmx = (dlx0 + dlx1) * 0.5f;
            p[p1].dmy = (dly0 + dly1) * 0.5f;
            dmr2 = p[p1].dmx*p[p1].dmx + p[p1].dmy*p[p1].dmy;
            if (dmr2 > 0.000001f) {
                float s2 = 1.0f / dmr2;
                if (s2 > 600.0f) {
                    s2 = 600.0f;
                }
                p[p1].dmx *= s2;
                p[p1].dmy *= s2;
            }

            // Clear flags, but keep the corner.
            p[p1].flags = ((p[p1].flags & NSVG_PT_CORNER) != 0) ? NSVG_PT_CORNER : 0;

            // Keep track of left turns.
            cross = p[p1].dx * p[p0].dy - p[p0].dx * p[p1].dy;
            if (cross > 0.0f)
                p[p1].flags |= NSVG_PT_LEFT;

            // Check to see if the corner needs to be beveled.
            if ((p[p1].flags & NSVG_PT_CORNER) != 0) {
                if ((dmr2 * miterLimit*miterLimit) < 1.0f || lineJoin == NSVG_JOIN_BEVEL || lineJoin == NSVG_JOIN_ROUND) {
                    p[p1].flags |= NSVG_PT_BEVEL;
                }
            }

            p0 = p1++;
        }
    }

    public void nsvg__flattenShapeStroke(NSVGrasterizer r, NSVGshape shape, float scale)
    {
        int i, j;
        boolean closed;
        NSVGpath path;
        NSVGpoint p0;
        NSVGpoint p1;
        float miterLimit = shape.miterLimit;
        int lineJoin = shape.strokeLineJoin;
        int lineCap = shape.strokeLineCap;
        float lineWidth = shape.strokeWidth * scale;

        for (path = shape.paths; path != null; path = path.next) {
            // Flatten path
            r.npoints = 0;
            nsvg__addPathPoint(r, path.pts[0]*scale, path.pts[1]*scale, NSVG_PT_CORNER);
            for (i = 0; i < path.npts-1; i += 3) {
                float[] p = path.pts;
                int pp = i*2;
                nsvg__flattenCubicBez(r, p[pp + 0]*scale,p[pp + 1]*scale, p[pp + 2]*scale,p[pp + 3]*scale, p[pp + 4]*scale,p[pp + 5]*scale, p[pp + 6]*scale,p[pp + 7]*scale, 0, NSVG_PT_CORNER);
            }
            if (r.npoints < 2)
                continue;

            closed = path.closed;

            // If the first and last points are the same, remove the last, mark as closed path.
            p0 = r.points[r.npoints-1];
            p1 = r.points[0];
            if (nsvg__ptEquals(p0.x,p0.y, p1.x,p1.y, r.distTol)) {
                r.npoints--;
                p0 = r.points[r.npoints-1];
                closed = true;
            }

            if (shape.strokeDashCount > 0) {
                int idash = 0;
                boolean dashState = true;
                float totalDist = 0, dashLen, allDashLen, dashOffset;
                NSVGpoint cur;

                if (closed)
                    nsvg__appendPathPoint(r, r.points[0]);

                // Duplicate points . points2.
                nsvg__duplicatePoints(r);

                r.npoints = 0;
                cur = r.points2[0];
                nsvg__appendPathPoint(r, cur);

                // Figure out dash offset.
                allDashLen = 0;
                for (j = 0; j < shape.strokeDashCount; j++)
                    allDashLen += shape.strokeDashArray[j];
                if ((shape.strokeDashCount & 1) != 0)
                    allDashLen *= 2.0f;
                // Find location inside pattern
                dashOffset = fmodf(shape.strokeDashOffset, allDashLen);
                if (dashOffset < 0.0f)
                    dashOffset += allDashLen;

                while (dashOffset > shape.strokeDashArray[idash]) {
                    dashOffset -= shape.strokeDashArray[idash];
                    idash = (idash + 1) % shape.strokeDashCount;
                }
                dashLen = (shape.strokeDashArray[idash] - dashOffset) * scale;

                for (j = 1; j < r.npoints2; ) {
                    float dx = r.points2[j].x - cur.x;
                    float dy = r.points2[j].y - cur.y;
                    float dist = sqrtf(dx*dx + dy*dy);

                    if ((totalDist + dist) > dashLen) {
                        // Calculate intermediate point
                        float d = (dashLen - totalDist) / dist;
                        float x = cur.x + dx * d;
                        float y = cur.y + dy * d;
                        nsvg__addPathPoint(r, x, y, NSVG_PT_CORNER);

                        // Stroke
                        if (r.npoints > 1 && dashState) {
                            nsvg__prepareStroke(r, miterLimit, lineJoin);
                            nsvg__expandStroke(r, r.points, r.npoints, false, lineJoin, lineCap, lineWidth);
                        }
                        // Advance dash pattern
                        dashState = !dashState;
                        idash = (idash+1) % shape.strokeDashCount;
                        dashLen = shape.strokeDashArray[idash] * scale;
                        // Restart
                        cur.x = x;
                        cur.y = y;
                        cur.flags = NSVG_PT_CORNER;
                        totalDist = 0.0f;
                        r.npoints = 0;
                        nsvg__appendPathPoint(r, cur);
                    } else {
                        totalDist += dist;
                        cur = r.points2[j];
                        nsvg__appendPathPoint(r, cur);
                        j++;
                    }
                }
                // Stroke any leftover path
                if (r.npoints > 1 && dashState)
                    nsvg__expandStroke(r, r.points, r.npoints, false, lineJoin, lineCap, lineWidth);
            } else {
                nsvg__prepareStroke(r, miterLimit, lineJoin);
                nsvg__expandStroke(r, r.points, r.npoints, closed, lineJoin, lineCap, lineWidth);
            }
        }
    }

    //static int nsvg__cmpEdge(NSVGedge p, NSVGedge q)
    //{
    //    final NSVGedge a = p;
	//    final NSVGedge b = q;
//
    //    if (a.y0 < b.y0) return -1;
    //    if (a.y0 > b.y0) return  1;
    //    return 0;
    //}


    static NSVGactiveEdge nsvg__addActive(NSVGrasterizer r, NSVGedge e, float startPoint)
    {
        NSVGactiveEdge z;

        if (r.freelist != null) {
            // Restore from freelist.
            z = r.freelist;
            r.freelist = z.next;
        } else {
            // Alloc new edge.
            z = new NSVGactiveEdge();
            if (z == null) return null;
        }

        float dxdy = (e.x1 - e.x0) / (e.y1 - e.y0);
//	STBTT_assert(e.y0 <= start_point);
        // round dx down to avoid going too far
        if (dxdy < 0)
            z.dx = (int)(-floorf(NSVG__FIX * -dxdy));
        else
            z.dx = (int)floorf(NSVG__FIX * dxdy);
        z.x = (int)floorf(NSVG__FIX * (e.x0 + dxdy * (startPoint - e.y0)));
//	z.x -= off_x * FIX;
        z.ey = e.y1;
        z.next = null;
        z.dir = e.dir;

        return z;
    }

    public void nsvg__freeActive(NSVGrasterizer r, NSVGactiveEdge z)
    {
        z.next = r.freelist;
        r.freelist = z;
    }

    public void nsvg__fillScanline(byte[] scanline, int len, int x0, int x1, int maxWeight, int[] xmin, int[] xmax)
    {
        int i = x0 >> NSVG__FIXSHIFT;
        int j = x1 >> NSVG__FIXSHIFT;
        if (i < xmin[0]) xmin[0] = i;
        if (j > xmax[0]) xmax[0] = j;
        if (i < len && j >= 0) {
            if (i == j) {
                // x0,x1 are the same pixel, so compute combined coverage
                scanline[i] = (byte)(scanline[i] + ((x1 - x0) * maxWeight >> NSVG__FIXSHIFT));
            } else {
                if (i >= 0) // add antialiasing for x0
                    scanline[i] = (byte)(scanline[i] + (((NSVG__FIX - (x0 & NSVG__FIXMASK)) * maxWeight) >> NSVG__FIXSHIFT));
			else
                i = -1; // clip

                if (j < len) // add antialiasing for x1
                    scanline[j] = (byte)(scanline[j] + (((x1 & NSVG__FIXMASK) * maxWeight) >> NSVG__FIXSHIFT));
			else
                j = len; // clip

                for (++i; i < j; ++i) // fill pixels between x0 and x1
                    scanline[i] = (byte)(scanline[i] + maxWeight);
            }
        }
    }

    // note: this routine clips fills that extend off the edges... ideally this
// wouldn't happen, but it could happen if the truetype glyph bounding boxes
// are wrong, or if the user supplies a too-small bitmap
    public void nsvg__fillActiveEdges(
            byte[] scanline, int len, NSVGactiveEdge e, int maxWeight,
            int[] xmin,
            int[] xmax,
            int fillRule
    )
    {
        // non-zero winding fill
        int x0 = 0, w = 0;

        if (fillRule == NSVG_FILLRULE_NONZERO) {
            // Non-zero
            while (e != null) {
                if (w == 0) {
                    // if we're currently at zero, we need to record the edge start point
                    x0 = e.x; w += e.dir;
                } else {
                    int x1 = e.x; w += e.dir;
                    // if we went to zero, we need to draw
                    if (w == 0)
                        nsvg__fillScanline(scanline, len, x0, x1, maxWeight, xmin, xmax);
                }
                e = e.next;
            }
        } else if (fillRule == NSVG_FILLRULE_EVENODD) {
            // Even-odd
            while (e != null) {
                if (w == 0) {
                    // if we're currently at zero, we need to record the edge start point
                    x0 = e.x; w = 1;
                } else {
                    int x1 = e.x; w = 0;
                    nsvg__fillScanline(scanline, len, x0, x1, maxWeight, xmin, xmax);
                }
                e = e.next;
            }
        }
    }

    static float nsvg__clampf(float a, float mn, float mx) { return a < mn ? mn : (a > mx ? mx : a); }

    static int nsvg__RGBA(int r, int g, int b, int a)
    {
        return (r) | (g << 8) | (b << 16) | (a << 24);
    }

    static int nsvg__lerpRGBA(int c0, int c1, float u)
    {
        int iu = (int)(nsvg__clampf(u, 0.0f, 1.0f) * 256.0f);
        int r = (((c0) & 0xff)*(256-iu) + (((c1) & 0xff)*iu)) >> 8;
        int g = (((c0>>>8) & 0xff)*(256-iu) + (((c1>>>8) & 0xff)*iu)) >> 8;
        int b = (((c0>>>16) & 0xff)*(256-iu) + (((c1>>>16) & 0xff)*iu)) >> 8;
        int a = (((c0>>>24) & 0xff)*(256-iu) + (((c1>>>24) & 0xff)*iu)) >> 8;
        return nsvg__RGBA((int)r & 0xFF, (int)g & 0xFF, (int)b & 0xFF, (int)a & 0xFF);
    }

    static int nsvg__applyOpacity(int c, float u)
    {
        int iu = (int)(nsvg__clampf(u, 0.0f, 1.0f) * 256.0f);
        int r = (c) & 0xff;
        int g = (c>>8) & 0xff;
        int b = (c>>16) & 0xff;
        int a = (((c>>24) & 0xff)*iu) >> 8;
        return nsvg__RGBA((int)r & 0xFF, (int)g & 0xFF, (int)b & 0xFF, (int)a & 0xFF);
    }

    static int nsvg__div255(int x)
    {
        return ((x+1) * 257) >> 16;
    }

    public void nsvg__scanlineSolid(
        byte[] dst,
        int dstPos,
        int count,
        byte[] cover,
        int coverPos,
        int x, int y,
        float tx, float ty,
        float scale,
        NSVGcachedPaint cache
    )
    {

        if (cache.type == NSVG_PAINT_COLOR) {
            int i, cr, cg, cb, ca;
            cr = cache.colors[0] & 0xff;
            cg = (cache.colors[0] >> 8) & 0xff;
            cb = (cache.colors[0] >> 16) & 0xff;
            ca = (cache.colors[0] >> 24) & 0xff;

            for (i = 0; i < count; i++) {
                int r,g,b;
                int a = nsvg__div255((int)cover[coverPos + 0] * ca);
                int ia = 255 - a;
                // Premultiply
                r = nsvg__div255(cr * a);
                g = nsvg__div255(cg * a);
                b = nsvg__div255(cb * a);

                // Blend over
                r += nsvg__div255(ia * (int)dst[dstPos + 0]);
                g += nsvg__div255(ia * (int)dst[dstPos + 1]);
                b += nsvg__div255(ia * (int)dst[dstPos + 2]);
                a += nsvg__div255(ia * (int)dst[dstPos + 3]);

                dst[dstPos + 0] = (byte)r;
                dst[dstPos + 1] = (byte)g;
                dst[dstPos + 2] = (byte)b;
                dst[dstPos + 3] = (byte)a;

                coverPos++;
                dstPos += 4;
            }
        } else if (cache.type == NSVG_PAINT_LINEAR_GRADIENT) {
            // TODO: spread modes.
            // TODO: plenty of opportunities to optimize.
            float fx, fy, dx, gy;
            float[] t = cache.xform;
            int i, cr, cg, cb, ca;
            int c;

            fx = ((float)x - tx) / scale;
            fy = ((float)y - ty) / scale;
            dx = 1.0f / scale;

            for (i = 0; i < count; i++) {
                int r,g,b,a,ia;
                gy = fx*t[1] + fy*t[3] + t[5];
                c = cache.colors[(int)nsvg__clampf(gy*255.0f, 0, 255.0f)];
                cr = (c) & 0xff;
                cg = (c >> 8) & 0xff;
                cb = (c >> 16) & 0xff;
                ca = (c >> 24) & 0xff;

                a = nsvg__div255((int)cover[coverPos + 0] * ca);
                ia = 255 - a;

                // Premultiply
                r = nsvg__div255(cr * a);
                g = nsvg__div255(cg * a);
                b = nsvg__div255(cb * a);

                // Blend over
                r += nsvg__div255(ia * (int)dst[dstPos + 0]);
                g += nsvg__div255(ia * (int)dst[dstPos + 1]);
                b += nsvg__div255(ia * (int)dst[dstPos + 2]);
                a += nsvg__div255(ia * (int)dst[dstPos + 3]);

                dst[dstPos + 0] = (byte)r;
                dst[dstPos + 1] = (byte)g;
                dst[dstPos + 2] = (byte)b;
                dst[dstPos + 3] = (byte)a;

                coverPos++;
                dstPos += 4;
                fx += dx;
            }
        } else if (cache.type == NSVG_PAINT_RADIAL_GRADIENT) {
            // TODO: spread modes.
            // TODO: plenty of opportunities to optimize.
            // TODO: focus (fx,fy)
            float fx, fy, dx, gx, gy, gd;
            float[] t = cache.xform;
            int i, cr, cg, cb, ca;
            int c;

            fx = ((float)x - tx) / scale;
            fy = ((float)y - ty) / scale;
            dx = 1.0f / scale;

            for (i = 0; i < count; i++) {
                int r,g,b,a,ia;
                gx = fx*t[0] + fy*t[2] + t[4];
                gy = fx*t[1] + fy*t[3] + t[5];
                gd = sqrtf(gx*gx + gy*gy);
                c = cache.colors[(int)nsvg__clampf(gd*255.0f, 0, 255.0f)];
                cr = (c) & 0xff;
                cg = (c >> 8) & 0xff;
                cb = (c >> 16) & 0xff;
                ca = (c >> 24) & 0xff;

                a = nsvg__div255((int)cover[coverPos + 0] * ca);
                ia = 255 - a;

                // Premultiply
                r = nsvg__div255(cr * a);
                g = nsvg__div255(cg * a);
                b = nsvg__div255(cb * a);

                // Blend over
                r += nsvg__div255(ia * (int)dst[dstPos + 0]);
                g += nsvg__div255(ia * (int)dst[dstPos + 1]);
                b += nsvg__div255(ia * (int)dst[dstPos + 2]);
                a += nsvg__div255(ia * (int)dst[dstPos + 3]);

                dst[dstPos + 0] = (byte)r;
                dst[dstPos + 1] = (byte)g;
                dst[dstPos + 2] = (byte)b;
                dst[dstPos + 3] = (byte)a;

                coverPos++;
                dstPos += 4;
                fx += dx;
            }
        }
    }

    public void nsvg__rasterizeSortedEdges(NSVGrasterizer r, float tx, float ty, float scale, NSVGcachedPaint cache, int fillRule)
    {
        NSVGactiveEdge active = null;
        int y, s;
        int e = 0;
        int maxWeight = (255 / NSVG__SUBSAMPLES);  // weight per vertical scanline
        int[] xmin = new int[1];
        int[] xmax = new int[1];

        for (y = 0; y < r.height; y++) {
            Arrays.fill(r.scanline, 0, r.width, (byte)0);
            xmin[0] = r.width;
            xmax[0] = 0;
            for (s = 0; s < NSVG__SUBSAMPLES; s++) {
                // find center of pixel for this scanline
                float scany = (float)(y*NSVG__SUBSAMPLES + s) + 0.5f;
                NSVGactiveEdge step = active;

                // update all active edges;
                // remove all active edges that terminate before the center of this scanline
                while (step != null) {
                    NSVGactiveEdge z = step;
                    if (z.ey <= scany) {
					step = z.next; // delete from list
//					NSVG__assert(z.valid);
                        nsvg__freeActive(r, z);
                    } else {
                        z.x += z.dx; // advance to position for current scanline
                        step = step.next; // advance through list
                    }
                }

                // resort the list if needed
                for (;;) {
                    boolean changed = false;
                    step = active;
                    while (step != null && step.next != null) {
                        if ((step).x > (step).next.x) {
                            NSVGactiveEdge t = step;
                            NSVGactiveEdge q = t.next;
                            t.next = q.next;
                            q.next = t;
						    step = q;
                            changed = true;
                        }
                        step = (step).next;
                    }
                    if (!changed) break;
                }

                // insert all edges that start before the center of this scanline -- omit ones that also end on this scanline
                while (e < r.nedges && r.edges[e].y0 <= scany) {
                    if (r.edges[e].y1 > scany) {
                        NSVGactiveEdge z = nsvg__addActive(r, r.edges[e], scany);
                        if (z == null) break;
                        // find insertion point
                        if (active == null) {
                            active = z;
                        } else if (z.x < active.x) {
                            // insert at front
                            z.next = active;
                            active = z;
                        } else {
                            // find thing to insert AFTER
                            NSVGactiveEdge p = active;
                            while (p.next != null && p.next.x < z.x)
                                p = p.next;
                            // at this point, p.next.x is NOT < z.x
                            z.next = p.next;
                            p.next = z;
                        }
                    }
                    e++;
                }

                // now process all active edges in non-zero fashion
                if (active != null)
                    nsvg__fillActiveEdges(r.scanline, r.width, active, maxWeight, xmin, xmax, fillRule);
            }
            // Blit
            if (xmin[0] < 0) xmin[0] = 0;
            if (xmax[0] > r.width-1) xmax[0] = r.width-1;
            if (xmin[0] <= xmax[0]) {
                nsvg__scanlineSolid(
                        r.bitmap,
                        (y * r.stride) + xmin[0]*4,
                        xmax[0]-xmin[0]+1,
                        r.scanline,
                        xmin[0],
                        xmin[0], y,
                        tx,ty, scale, cache
                );
            }
        }

    }

    public void nsvg__unpremultiplyAlpha(byte[] image, int w, int h, int stride)
    {
        int x,y;

        // Unpremultiply
        for (y = 0; y < h; y++) {
            byte[] row = image;
            int rowpos = y*stride;
            for (x = 0; x < w; x++) {
                int r = row[rowpos + 0], g = row[rowpos + 1], b = row[rowpos + 2], a = row[rowpos + 3];
                if (a != 0) {
                    row[rowpos + 0] = (byte)(r*255/a);
                    row[rowpos + 1] = (byte)(g*255/a);
                    row[rowpos + 2] = (byte)(b*255/a);
                }
                rowpos += 4;
            }
        }

        // Defringe
        for (y = 0; y < h; y++) {
            byte[] row = image;
            int rowpos = y*stride;
            for (x = 0; x < w; x++) {
                int r = 0, g = 0, b = 0, a = row[rowpos + 3], n = 0;
                if (a == 0) {
                    if (x-1 > 0 && row[rowpos + -1] != 0) {
                        r += row[rowpos + -4];
                        g += row[rowpos + -3];
                        b += row[rowpos + -2];
                        n++;
                    }
                    if (x+1 < w && row[rowpos + 7] != 0) {
                        r += row[rowpos + 4];
                        g += row[rowpos + 5];
                        b += row[rowpos + 6];
                        n++;
                    }
                    if (y-1 > 0 && row[rowpos + -stride+3] != 0) {
                        r += row[rowpos + -stride];
                        g += row[rowpos + -stride+1];
                        b += row[rowpos + -stride+2];
                        n++;
                    }
                    if (y+1 < h && row[rowpos + stride+3] != 0) {
                        r += row[rowpos + stride];
                        g += row[rowpos + stride+1];
                        b += row[rowpos + stride+2];
                        n++;
                    }
                    if (n > 0) {
                        row[rowpos + 0] = (byte)(r/n);
                        row[rowpos + 1] = (byte)(g/n);
                        row[rowpos + 2] = (byte)(b/n);
                    }
                }
                rowpos += 4;
            }
        }
    }


    public void nsvg__initPaint(NSVGcachedPaint cache, NSVGpaint paint, float opacity)
    {
        int i, j;
        NSVGgradient grad;

        cache.type = paint.type;

        if (paint.type == NSVG_PAINT_COLOR) {
            cache.colors[0] = nsvg__applyOpacity(paint.color, opacity);
            return;
        }

        grad = paint.gradient;

        cache.spread = grad.spread;
        System.arraycopy(cache.xform, 0, grad.xform, 0, 6);

        if (grad.nstops == 0) {
            for (i = 0; i < 256; i++)
                cache.colors[i] = 0;
        } if (grad.nstops == 1) {
        for (i = 0; i < 256; i++)
            cache.colors[i] = nsvg__applyOpacity(grad.stops[i].color, opacity);
    } else {
        int ca, cb = 0;
        float ua, ub, du, u;
        int ia, ib, count;

        ca = nsvg__applyOpacity(grad.stops[0].color, opacity);
        ua = nsvg__clampf(grad.stops[0].offset, 0, 1);
        ub = nsvg__clampf(grad.stops[grad.nstops-1].offset, ua, 1);
        ia = (int)(ua * 255.0f);
        ib = (int)(ub * 255.0f);
        for (i = 0; i < ia; i++) {
            cache.colors[i] = ca;
        }

        for (i = 0; i < grad.nstops-1; i++) {
            ca = nsvg__applyOpacity(grad.stops[i].color, opacity);
            cb = nsvg__applyOpacity(grad.stops[i+1].color, opacity);
            ua = nsvg__clampf(grad.stops[i].offset, 0, 1);
            ub = nsvg__clampf(grad.stops[i+1].offset, 0, 1);
            ia = (int)(ua * 255.0f);
            ib = (int)(ub * 255.0f);
            count = ib - ia;
            if (count <= 0) continue;
            u = 0;
            du = 1.0f / (float)count;
            for (j = 0; j < count; j++) {
                cache.colors[ia+j] = nsvg__lerpRGBA(ca,cb,u);
                u += du;
            }
        }

        for (i = ib; i < 256; i++)
            cache.colors[i] = cb;
    }

    }

/*
public void dumpEdges(NSVGrasterizer r, const char* name)
{
	float xmin = 0, xmax = 0, ymin = 0, ymax = 0;
	NSVGedge *e = null;
	int i;
	if (r.nedges == 0) return;
	FILE* fp = fopen(name, "w");
	if (fp == null) return;

	xmin = xmax = r.edges[0].x0;
	ymin = ymax = r.edges[0].y0;
	for (i = 0; i < r.nedges; i++) {
		e = &r.edges[i];
		xmin = nsvg__minf(xmin, e.x0);
		xmin = nsvg__minf(xmin, e.x1);
		xmax = nsvg__maxf(xmax, e.x0);
		xmax = nsvg__maxf(xmax, e.x1);
		ymin = nsvg__minf(ymin, e.y0);
		ymin = nsvg__minf(ymin, e.y1);
		ymax = nsvg__maxf(ymax, e.y0);
		ymax = nsvg__maxf(ymax, e.y1);
	}

	fprintf(fp, "<svg viewBox=\"%f %f %f %f\" xmlns=\"http://www.w3.org/2000/svg\">", xmin, ymin, (xmax - xmin), (ymax - ymin));

	for (i = 0; i < r.nedges; i++) {
		e = &r.edges[i];
		fprintf(fp ,"<line x1=\"%f\" y1=\"%f\" x2=\"%f\" y2=\"%f\" style=\"stroke:#000;\" />", e.x0,e.y0, e.x1,e.y1);
	}

	for (i = 0; i < r.npoints; i++) {
		if (i+1 < r.npoints)
			fprintf(fp ,"<line x1=\"%f\" y1=\"%f\" x2=\"%f\" y2=\"%f\" style=\"stroke:#f00;\" />", r.points[i].x, r.points[i].y, r.points[i+1].x, r.points[i+1].y);
		fprintf(fp ,"<circle cx=\"%f\" cy=\"%f\" r=\"1\" style=\"fill:%s;\" />", r.points[i].x, r.points[i].y, r.points[i].flags == 0 ? "#f00" : "#0f0");
	}

	fprintf(fp, "</svg>");
	fclose(fp);
}
*/

    void nsvgRasterize(
        NSVGrasterizer r,
        NSVGimage image, float tx, float ty, float scale,
        byte[] dst, int w, int h, int stride
    )
    {
        NSVGshape shape = null;
        NSVGedge e = null;
        NSVGcachedPaint cache = null;
        int i;

        r.bitmap = dst;
        r.width = w;
        r.height = h;
        r.stride = stride;

        if (w > r.cscanline) {
            r.cscanline = w;
            r.scanline = Arrays.copyOf(r.scanline, w);
        }

        for (i = 0; i < h; i++)
            Arrays.fill(dst, i*stride, i*stride + w*4, (byte)0);

        for (shape = image.shapes; shape != null; shape = shape.next) {
            if ((shape.flags & NSVG_FLAGS_VISIBLE) == 0)
                continue;

            if (shape.fill.type != NSVG_PAINT_NONE) {
                nsvg__resetPool(r);
                r.freelist = null;
                r.nedges = 0;

                nsvg__flattenShape(r, shape, scale);

                // Scale and translate edges
                for (i = 0; i < r.nedges; i++) {
                    e = r.edges[i];
                    e.x0 = tx + e.x0;
                    e.y0 = (ty + e.y0) * NSVG__SUBSAMPLES;
                    e.x1 = tx + e.x1;
                    e.y1 = (ty + e.y1) * NSVG__SUBSAMPLES;
                }

                // Rasterize edges
                Arrays.sort(r.edges, 0, r.nedges);

                // now, traverse the scanlines and find the intersections on each scanline, use non-zero rule
                nsvg__initPaint(cache, shape.fill, shape.opacity);

                nsvg__rasterizeSortedEdges(r, tx,ty,scale, cache, shape.fillRule);
            }
            if (shape.stroke.type != NSVG_PAINT_NONE && (shape.strokeWidth * scale) > 0.01f) {
                nsvg__resetPool(r);
                r.freelist = null;
                r.nedges = 0;

                nsvg__flattenShapeStroke(r, shape, scale);

//			dumpEdges(r, "edge.svg");

                // Scale and translate edges
                for (i = 0; i < r.nedges; i++) {
                    e = r.edges[i];
                    e.x0 = tx + e.x0;
                    e.y0 = (ty + e.y0) * NSVG__SUBSAMPLES;
                    e.x1 = tx + e.x1;
                    e.y1 = (ty + e.y1) * NSVG__SUBSAMPLES;
                }

                // Rasterize edges
                Arrays.sort(r.edges, 0, r.nedges);

                // now, traverse the scanlines and find the intersections on each scanline, use non-zero rule
                nsvg__initPaint(cache, shape.stroke, shape.opacity);

                nsvg__rasterizeSortedEdges(r, tx,ty,scale, cache, NSVG_FILLRULE_NONZERO);
            }
        }

        nsvg__unpremultiplyAlpha(dst, w, h, stride);

        r.bitmap = null;
        r.width = 0;
        r.height = 0;
        r.stride = 0;
    }
}
