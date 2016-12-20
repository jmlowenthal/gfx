package gfx.tick2;

import java.awt.image.BufferedImage;
import java.util.List;

public class Renderer {

    private final double EPSILON = 0.0001;

    // The number of times a ray can bounce for reflection
    private int bounces;

    // The width and height of the image in pixels
    private int width, height;

    private Vector3 backgroundColor = new Vector3(0.1);

    public Renderer(int width, int height, int bounces) {
        this.width = width;
        this.height = height;
        this.bounces = bounces;
    }

    protected Vector3 trace(Scene scene, Ray ray, int bouncesLeft) {

        RaycastHit closestHit = scene.findClosestIntersection(ray);

        // If no object has been hit, return a background colour
        SceneObject object = closestHit.getObjectHit();
        if (object == null) {
            return backgroundColor;
        }

        Vector3 P = closestHit.getLocation();
        Vector3 N = closestHit.getNormal();

        // Calculate direct illumination at that point
        Vector3 directIllumination = this.illuminate(scene, object, P, N);

        // If no bounces left, or no reflection, return direct illumination only
        if (bouncesLeft == 0 || object.getReflectivity() == 0) {
            return directIllumination;

        } else {
        	Vector3 R = ray.getDirection().scale(-1).reflectIn(N).normalised();
			Ray reflectedRay = new Ray(P.add(R.scale(EPSILON)), R);
			Vector3 reflectedIllumination = trace(scene, reflectedRay, bouncesLeft - 1);

            // Scale direct and reflective illumination to conserve light
            directIllumination = directIllumination.scale(1.0 - object.getReflectivity());
            reflectedIllumination = reflectedIllumination.scale(object.getReflectivity());

            return directIllumination.add(reflectedIllumination);
        }
    }

    private Vector3 illuminate(Scene scene, SceneObject object, Vector3 P, Vector3 N) {

        Vector3 colourToReturn = new Vector3(0);

        Vector3 I_a = scene.getAmbientLighting();

        Vector3 C_diff = object.getColour();     // Diffuse colour defined by the object
        Vector3 C_spec = new Vector3(1);         // Specular colour is white

        double k_d = object.getPhong_kD();
        double k_s = object.getPhong_kS();
        double alpha = object.getPhong_n();

        // Add ambient light term to start with
        colourToReturn = colourToReturn.add(I_a.scale(C_diff));

        // Loop over each point light source
        List<PointLight> pointLights = scene.getPointLights();
        for (int i = 0; i < pointLights.size(); i++) {

            PointLight light = pointLights.get(i);
            double distanceToLight = light.getPosition().subtract(P).magnitude();
            Vector3 I = light.getIlluminationAt(distanceToLight);

			Vector3 L = light.getPosition().subtract(P).normalised();
			Vector3 R = L.reflectIn(N);
			Vector3 V = P.scale(-1).normalised();
			
			Vector3 diffuse = C_diff.scale(k_d).scale(I).scale(N.dot(L));
			Vector3 specular = C_spec.scale(k_s).scale(I).scale(Math.pow(Math.max(0, R.dot(V)), alpha));

            // Check if point P is in shadow from that light or not
            Ray shadowRay = new Ray(P.add(L.scale(EPSILON)), L);			
			if (scene.findClosestIntersection(shadowRay).getDistance() >= distanceToLight && N.dot(L) > 0)
				colourToReturn = colourToReturn.add(diffuse).add(specular);
        }

        return colourToReturn;
    }

    public BufferedImage render(Scene scene) {

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Camera camera = new Camera(width, height);

        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                Ray ray = camera.castRay(x, y);
                Vector3 pixel = trace(scene, ray, bounces);
                image.setRGB(x, y, pixel.toRGB());
            }
            System.out.println(String.format("%.2f", 100 * y / (float) (height - 1)) + "% completed");
        }

        return image;
    }
}
