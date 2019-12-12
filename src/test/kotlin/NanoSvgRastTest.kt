import org.junit.*
import java.util.*

class NanoSvgRastTest {
    @Test
    fun test() {
        val rast = NanoSvgRast()
        val r = rast.nsvgCreateRasterizer()
        val image = NanoSvgRast.NSVGimage();
        val w = 8
        val h = 8
        val img = ByteArray(w * h * 4)
        rast.nsvg__straightJoin(r, NanoSvgRast.NSVGpoint(0f, 0f), NanoSvgRast.NSVGpoint(128f, 128f), NanoSvgRast.NSVGpoint(1f, 1f), 4f)
        println(r.nedges)
        println(r.cedges)
        rast.nsvgRasterize(r, image, 0f, 0f, 1f, img, w, h, w*4)

        println(img.toList())

        /*
        NSVGimage image;
        image = nsvgParseFromFile("test.svg", "px", 96);

        // Create rasterizer (can be used to render multiple images).
        struct NSVGrasterizer rast = nsvgCreateRasterizer();
        // Allocate memory for image
        unsigned char* img = malloc(w*h*4);
        // Rasterize
        nsvgRasterize(rast, image, 0,0,1, img, w, h, w*4);
         */
    }
}