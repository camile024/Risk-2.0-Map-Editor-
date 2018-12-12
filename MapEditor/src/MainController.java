import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class MainController {

	
	
	@FXML
	Button fxEditPortsBtn;
	@FXML
	Label fxZoomLevel;
	@FXML
	Label fxX;
	@FXML
	Label fxY;
	@FXML
	Label fxDebug;
	@FXML
	HBox fxCanvasBox;
	@FXML
	VBox fxMenuPanel;
	@FXML
	ComboBox<String> fxPortCombo;
	
	@FXML
	Canvas fxCanvas;	//visible buffer, scaled
	private ImageView imgView;
	private Image img;
	
	private Canvas bgCanvas; //background drawing area 1:1
	private ImageView bgImgView;
	
	private Canvas metaScaledCanvas;	//scaled picking canvas/area
	private ImageView metaScaledImgView;
	
	private Canvas metaCanvas; //province map 1:1
	private ImageView metaImgView;
	
	private int zoomLevel = 1;
	private double lastX = 0;
	private double lastY = 0;
	
	private double stageWidth;
	private double stageHeight;
	
	private double mouseX = 0;
	private double mouseY = 0;
	
	private final Color border = Color.web("0x000000");
	private final Color sea = Color.web("0x7b9ab9");
	private final Color white = Color.web("0xffffff");
	private final Color seaBorder = Color.web("0x494d94");
	private final Color yellow = Color.YELLOW;
	private final Color red = Color.RED;
	
	private boolean mapGenerated = false;
	private boolean neighboursGenerated = false;
	
	int currentPort = 0;
	private boolean editingPorts = false;
	
	/* Province data to be exported */
	private HashMap<Color, Province> provinceList;
	private ArrayList<ArrayList<Province>> portData;

	private HashMap<Coordinate, Boolean> filledCoords;
	
	@FXML
	public void fxMapLoad() {
		bgCanvas = new Canvas();
		metaCanvas = new Canvas();
		metaScaledCanvas = new Canvas();
		
		img = new Image("blank_map_gen1.png");
		
		imgView = new ImageView(img);
		imgView.setPreserveRatio(true);
		imgView.setFitWidth(img.getWidth());
		imgView.setFitHeight(img.getHeight());

		clearBackground();
		metaImgView = new ImageView(new Image("blank_map_gen1.png"));
		metaImgView.setPreserveRatio(true);
		metaImgView.setFitWidth(img.getWidth());
		metaImgView.setFitHeight(img.getHeight());
		
		metaScaledImgView = new ImageView(new Image("blank_map_gen1.png"));
		metaScaledImgView.setPreserveRatio(true);
		metaScaledImgView.setFitWidth(img.getWidth());
		metaScaledImgView.setFitHeight(img.getHeight());
		
		
		
		fxCanvas.setWidth(fxCanvasBox.getWidth());
		fxCanvas.setHeight(fxCanvasBox.getHeight());
		
		metaScaledCanvas.setWidth(metaScaledImgView.getFitWidth());
		metaScaledCanvas.setHeight(metaScaledImgView.getFitHeight());
		
		setListeners();
		zoomLevel = 22;
		updateZoom();
		drawMap();
		addZoomEvent();
		addScrollEvent();	
		
		ArrayList<String> list = new ArrayList<String>();
		portData = new ArrayList<ArrayList<Province>>();
		for (int i = 0; i < 38; i++) {
			portData.add(new ArrayList<Province>());
			list.add(String.valueOf(i+1));
		}
		
		fxPortCombo.setItems(FXCollections.observableArrayList(list));
	}
	
	
	@FXML
	public void fxPortSelect() {
		String selected = fxPortCombo.getValue();
		currentPort = (Integer.valueOf(selected)) - 1;
		clearForeground();
		clearBackground();
		updateZoom();
		drawPortProvinces();
		swapBuffers();
		drawMap();
		fxDebug.setText("Selected port: " + currentPort);
	}
	
	@FXML
	public void fxLoadGenerated() {
		img = new Image(this.getClass().getResourceAsStream("generated.png"));
		imgView = new ImageView(img);
		imgView.setPreserveRatio(true);
		imgView.setFitWidth(img.getWidth());
		imgView.setFitHeight(img.getHeight());

		bgImgView = new ImageView(img);
		imgView.setPreserveRatio(true);
		imgView.setFitWidth(img.getWidth());
		imgView.setFitHeight(img.getHeight());
		
		fxCanvas.setWidth(fxCanvasBox.getWidth());
		fxCanvas.setHeight(fxCanvasBox.getHeight());
		setListeners();
		zoomLevel = 22;
		updateZoom();
		drawMap();
		addZoomEvent();
		addScrollEvent();	
	}
	
	@FXML
	public void fxGenerateMap() {
		
		provinceList = new HashMap<Color, Province>();
		
		filledCoords = new HashMap<Coordinate, Boolean>();
		metaCanvas.setWidth(metaImgView.getImage().getWidth());
		metaCanvas.setHeight(metaImgView.getImage().getHeight());
		metaCanvas.getGraphicsContext2D().drawImage(metaImgView.getImage(), 0, 0);
		
		
		double width = img.getWidth();
		double height = img.getHeight();

    	Color provinceColor = Color.web("0xFFFFEE");
    	fxDebug.setText("Initialising.");
    	int provinceNum = 0;
		for (int x = 0; x < (int)width; x++) {
			for (int y = 0; y < (int)height; y++) {
				Color c = img.getPixelReader().getColor(x, y);
				
				if (c.equals(white) && isUnfilled(x, y, filledCoords)) {
					
					
					fillProvince(x,y, provinceColor, img, metaCanvas);
					provinceColor = getSnapshot(metaCanvas).getPixelReader().getColor(x, y);
					Province p = new Province(provinceColor, x, y);
					provinceList.put(provinceColor, p);
					provinceColor = advanceColor(provinceColor);
					provinceNum++;
					fxDebug.setText("Province: " + provinceNum +". Last color: " + p.id);
					//updateProgress(x+y, width+height);
					
				}
			}
		}
				
		imgView.setImage(getSnapshot(metaCanvas));
		bgImgView.setImage(getSnapshot(metaCanvas));
		metaScaledImgView.setImage(getSnapshot(metaCanvas));
		metaImgView.setImage(getSnapshot(metaCanvas));
		
		File file = new File("generated.png");

	    try {
	        ImageIO.write(SwingFXUtils.fromFXImage(getSnapshot(metaCanvas), null), "png", file);
	    } catch (IOException e) {
	        // TODO: handle exception here
	    }
	    
		mapGenerated = true;
		drawMap();

	}
	
	@FXML
	public void fxEditPorts() {
		editingPorts = !editingPorts;
		fxEditPortsBtn.setUnderline(editingPorts);
		clearForeground();
		clearBackground();
		clearMeta();
		updateZoom();
		drawMap();
		drawPortProvinces();
		swapBuffers();
		drawMap();
	}
	
	@FXML
	public void fxExport() {
		File f2 = new File("sql_neighbours.sql");
		File f = new File("sql_provinces.sql");
		File f3 = new File("sql_seas.sql");
		FileWriter fw2, fw, fw3;
		
		try {
			fw2 = new FileWriter(f2);
			fw = new FileWriter(f);
			fw3 = new FileWriter(f3);
		
			
			
			/* Now we have all the provinces ready and map exported as a .png */
			
			/* Construct the query: */
			
			/* Assign port/sea numbers */
			/* For each sea.. */
			for (int i = 0; i < portData.size(); i++) {
				/* For each province in that sea... */
				for (Province firstProvince : portData.get(i)) {
					/* Set it as a port */
					firstProvince.setPort(true);
					/* Construct query */
					fw3.append("INSERT INTO belongs_sea VALUES ('" + String.valueOf(i+1) + "', '" + firstProvince.id + "');\r\n");
				}
			}
			
			/* Province list + neighbours (constructing both queries at once) */
			for (Province p : provinceList.values()) {

				/* Province data */
				fw.append("INSERT INTO provinces (ProvinceID, Owner, AttackMod, DefenseMod, DefenseNeighbourMod, Loyalty, Resources, IsPort, IsCapital, X, Y)\r\n" + 
						"VALUES ('" + p.id + "', " + "NULL, " + p.getAttack() + ", " + p.getDefence() + ", " + p.getNeighbourDefence() + ", " + p.getLoyalty() + ", " +
						p.getResources() + ", " + String.valueOf(p.isPort()).toUpperCase() + ", " + String.valueOf(p.isCapital()).toUpperCase() + ", " + p.coord.x + ", " +
						p.coord.y +
						");\r\n");
				
				/* Neighbours */
				for (Province neighbour : p.getNeighbours()) {
					fw2.append("INSERT INTO neighbour VALUES ('" + p.id + "', '" + neighbour.id + "');\r\n");
				}
				
			}
			
			/* Close files */
			fw.close();
			fw2.close();
			fw3.close();
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		
		
		
	}
	
	
	public Image getSnapshot(Canvas c) {
		SnapshotParameters snapParams= new SnapshotParameters();
		snapParams.setFill(Color.TRANSPARENT);
		return c.snapshot(snapParams, null);
	}
	
	private void clearForeground() {
		imgView.setImage(new Image("blank_map_gen1.png"));
		
	}
	
	private void clearBackground() {
		bgImgView = new ImageView(new Image("blank_map_gen1.png"));
		bgImgView.setPreserveRatio(true);
		bgImgView.setFitWidth(bgImgView.getImage().getWidth());
		bgImgView.setFitHeight(bgImgView.getImage().getHeight());
		bgCanvas.setWidth(bgImgView.getFitWidth());
		bgCanvas.setHeight(bgImgView.getFitHeight());
		bgCanvas.getGraphicsContext2D().clearRect(0, 0, bgCanvas.getWidth(), bgCanvas.getHeight());
		bgCanvas.getGraphicsContext2D().drawImage(bgImgView.getImage(), 0, 0, bgImgView.getFitWidth(), bgImgView.getFitHeight());
	}
	
	private void clearMeta() {
		metaImgView = new ImageView(new Image("generated.png"));
		metaImgView.setPreserveRatio(true);
		metaImgView.setFitWidth(metaImgView.getImage().getWidth());
		metaImgView.setFitHeight(metaImgView.getImage().getHeight());
		metaCanvas.setWidth(metaImgView.getFitWidth());
		metaCanvas.setHeight(metaImgView.getFitHeight());
		
		metaScaledImgView.setImage(new Image("generated.png"));

	}


	public void drawPortProvinces() {
		for (Province p : portData.get(currentPort)) {
			fillArea(p.coord.x, p.coord.y, red, bgImgView.getImage(), bgCanvas);
		}
	}
	
	@FXML
	public void fxGenerateNeighbours() {
		Image bgImg = metaImgView.getImage();
		
		long updates = 0;
		for (Province p : provinceList.values()) {
			HashMap<Coordinate, Boolean> filledCoords = new HashMap<Coordinate, Boolean>();
			Stack<Coordinate> stack = new Stack<Coordinate>();
			Stack<Integer> blacksFound = new Stack<Integer>();
			stack.push(new Coordinate(p.coord.x,p.coord.y));
			blacksFound.push(0);
			while (!stack.isEmpty()) {
				
				Coordinate coord = stack.pop();
				int blacks = blacksFound.pop();
				
				Color currentC = bgImg.getPixelReader().getColor(coord.x, coord.y);
				
				/* Mark pixel we're on */
				filledCoords.put(coord, true);
				
				/* If we're NOT on our own territory or we're on a border */
				if ((!currentC.equals(p.colour)) || currentC.equals(border)) {
					/* If we're on a border */
					if (currentC.equals(border)) {
						/* We might've come from a black but there's our nearby */
						if (hasNearbyColour(coord, p.colour, bgImg)) {
							blacks = 0;
						} else { //nope, just delved further into blacks
							blacks++;
						}
					/* Success! We've found a neighbour. Add to the list */
					} else {
						Province neighbour = provinceList.get(currentC);
						if (neighbour != null) {
							neighbour.addNeighbour(p);
							p.addNeighbour(neighbour);
						}
						
						updates++;
						blacks = 5; //stop looking from this 'thread'
					}
				/* We're either on a map element or inside our own territory */
				} else if (!currentC.equals(p.colour)){ /* it must be a map element, do nothing */
					continue;
				}
				
				/* If we're on our own, we might've just returned from a black */
				if (currentC.equals(p.colour)) {
					blacks = 0;
				}
				
				/* If we're not too far in a border, or in our own territory spawn more thingies */
				if (blacks <= 2) {
					if (coord.x+1 < bgImg.getWidth() && isUnfilled(coord.x+1, coord.y, filledCoords)) {
						stack.push(new Coordinate(coord.x+1, coord.y));
						blacksFound.push(blacks);
					}
					if (coord.x-1 > 0 && isUnfilled(coord.x-1, coord.y, filledCoords)) {
						stack.push(new Coordinate(coord.x-1, coord.y));
						blacksFound.push(blacks);
					}
					
					if (coord.y+1 < bgImg.getHeight() && isUnfilled(coord.x, coord.y+1, filledCoords)) {
						stack.push(new Coordinate(coord.x, coord.y+1));
						blacksFound.push(blacks);
					}
					if (coord.y-1 > 0 && isUnfilled(coord.x, coord.y-1, filledCoords)) {
						stack.push(new Coordinate(coord.x, coord.y-1));
						blacksFound.push(blacks);
					}
					
				} else {
					continue;//for debugging
				}
			}
		}
		neighboursGenerated = true;
		addClickEvent();
		fxDebug.setText("Finished! Updates: " + updates);
	}
	
	private boolean hasNearbyColour(Coordinate coord, Color c, Image reference) {
		boolean result = false;
		if (coord.x-1 > 0) {
			result |= reference.getPixelReader().getColor(coord.x-1, coord.y).equals(c);
		}
		if (coord.x+1 < reference.getWidth()) {
			result |=reference.getPixelReader().getColor(coord.x+1, coord.y).equals(c);
		}
		if (coord.y+1 > reference.getHeight()) {
			result |= reference.getPixelReader().getColor(coord.x, coord.y+1).equals(c);
		}
		if (coord.y-1 > 0) {
			result |= reference.getPixelReader().getColor(coord.x, coord.y-1).equals(c);
		}
		return result;
	}
	
	private void fillProvince(int x, int y, Color c, Image img, Canvas canvas) {
		PriorityQueue<Coordinate> queue = new PriorityQueue<Coordinate>();
		queue.add(new Coordinate(x,y));
		while (!queue.isEmpty()) {
			
			Coordinate coord = queue.poll();
			
			canvas.getGraphicsContext2D().getPixelWriter().setColor(coord.x, coord.y, c);
			
			filledCoords.put(coord, true);
			
			if (coord.x+1 < img.getWidth() && img.getPixelReader().getColor(coord.x+1, coord.y).equals(white) && isUnfilled(coord.x+1, coord.y, filledCoords)) {
				queue.add(new Coordinate(coord.x+1, coord.y));
			}
			if (coord.x-1 >= 0 && img.getPixelReader().getColor(coord.x-1, coord.y).equals(white) && isUnfilled(coord.x-1, coord.y, filledCoords)) {
				queue.add(new Coordinate(coord.x-1, coord.y));
			}
			
			if (coord.y+1 < img.getHeight() && img.getPixelReader().getColor(coord.x, coord.y+1).equals(white) && isUnfilled(coord.x, coord.y+1, filledCoords)) {
				queue.add(new Coordinate(coord.x, coord.y+1));
			}
			if (coord.y-1 >= 0 && img.getPixelReader().getColor(coord.x, coord.y-1).equals(white) && isUnfilled(coord.x, coord.y-1, filledCoords)) {
				queue.add(new Coordinate(coord.x, coord.y-1));
			}
		}
	}
	
	private void fillArea(int x, int y, Color c, Image img, Canvas canvas) {
		HashMap<Coordinate, Boolean> filled = new HashMap<Coordinate, Boolean>();
		PriorityQueue<Coordinate> queue = new PriorityQueue<Coordinate>();
		Color initialColor = img.getPixelReader().getColor(x, y);
		queue.add(new Coordinate(x,y));
		while (!queue.isEmpty()) {
			Coordinate coord = queue.poll();
			canvas.getGraphicsContext2D().getPixelWriter().setColor(coord.x, coord.y, c);
			filled.put(coord, true);
			if (coord.x+1 < img.getWidth() && img.getPixelReader().getColor(coord.x+1, coord.y).equals(initialColor) && isUnfilled(coord.x+1, coord.y, filled)) {
				queue.add(new Coordinate(coord.x+1, coord.y));
			}
			if (coord.x-1 >= 0 && img.getPixelReader().getColor(coord.x-1, coord.y).equals(initialColor) && isUnfilled(coord.x-1, coord.y, filled)) {
				queue.add(new Coordinate(coord.x-1, coord.y));
			}
			
			if (coord.y+1 < img.getHeight() && img.getPixelReader().getColor(coord.x, coord.y+1).equals(initialColor) && isUnfilled(coord.x, coord.y+1, filled)) {
				queue.add(new Coordinate(coord.x, coord.y+1));
			}
			if (coord.y-1 >= 0 && img.getPixelReader().getColor(coord.x, coord.y-1).equals(initialColor) && isUnfilled(coord.x, coord.y-1, filled)) {
				queue.add(new Coordinate(coord.x, coord.y-1));
			}
		}
	}
	
	
	
	private boolean isUnfilled(int x, int y, HashMap<Coordinate, Boolean> filledCoords) {
		Coordinate coord = new Coordinate(x,y);
		/* Check if a pixel has already been put for these coords */
		if (filledCoords.get(coord) != null) {
			if (filledCoords.get(coord).booleanValue() == true) {
				return false;
			}
		}
		
		filledCoords.put(coord, true);
		return true;
		
	}
	
	private Color advanceColor(Color c) {
		Color returnVal = null;
		do {
			if (c.getBlue() < 0.08) {
				if (c.getGreen() < 0.08) {
					returnVal = Color.color(c.getRed() - 0.09, 1, 1);
				} else {
					returnVal =  Color.color(c.getRed(), c.getGreen()  - 0.08 , 1);
				}
			} else {
				returnVal =  Color.color(c.getRed(), c.getGreen(), c.getBlue() - 0.08);
			}
		} while ((returnVal.equals(border) || returnVal.equals(seaBorder) || returnVal.equals(sea) || returnVal.equals(white)));
		return returnVal;
	}
	
	
	@FXML
	public void fxHighlightColours() {
		bgCanvas.setWidth(bgImgView.getImage().getWidth());
		bgCanvas.setHeight(bgImgView.getImage().getHeight());
		bgCanvas.getGraphicsContext2D().drawImage(bgImgView.getImage(), 0, 0);
		
		int i = 0;
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				Color c = img.getPixelReader().getColor(x, y);
				
				if (! (c.equals(border) || c.equals(seaBorder) || c.equals(sea) || c.equals(white))) {
					bgCanvas.getGraphicsContext2D().getPixelWriter().setColor(x, y, red); //change this to sea/border if doing corrections instead of highlighting
					i++;
				}
			}
		}
		
		imgView.setImage(getSnapshot(bgCanvas));
		fxDebug.setText("Set " + i + " pixels.");
		File file = new File("highlights.png");

	    try {
	        ImageIO.write(SwingFXUtils.fromFXImage(getSnapshot(bgCanvas), null), "png", file);
	    } catch (IOException e) {
	        // TODO: handle exception here
	    }
		drawMap();
	}
	
	private void onMouseWheelUp() {
		if (zoomLevel < 60) {
			zoomLevel++;
		}
		updateZoom();
	}
	
	private void onMouseWheelDown() {
		if (zoomLevel > 22) {
			zoomLevel--;
		}
		updateZoom();
	}
	
	
	private void updateZoom() {
		imgView.setFitWidth(zoomLevel * 0.03 * imgView.getImage().getWidth());
		imgView.setFitHeight(zoomLevel * 0.03 * imgView.getImage().getHeight());
		metaScaledImgView.setFitWidth(zoomLevel * 0.03 * metaScaledImgView.getImage().getWidth());
		metaScaledImgView.setFitHeight(zoomLevel * 0.03 * metaScaledImgView.getImage().getHeight());
		fxZoomLevel.setText(String.valueOf(zoomLevel));
	}
	
	private void swapBuffers() {
		imgView.setImage(getSnapshot(bgCanvas));
		updateZoom();
		drawMap();
	}
	
	private void drawMap() {
		/* scaled foreground */
		fxCanvas.getGraphicsContext2D().clearRect(0, 0, fxCanvas.getWidth(), fxCanvas.getHeight());
		fxCanvas.getGraphicsContext2D().drawImage(imgView.getImage(), lastX, lastY, imgView.getFitWidth(), imgView.getFitHeight());
		
		/* un-scaled background */
		bgCanvas.getGraphicsContext2D().clearRect(0, 0, bgCanvas.getWidth(), bgCanvas.getHeight());
		bgCanvas.getGraphicsContext2D().drawImage(bgImgView.getImage(), 0, 0, bgImgView.getFitWidth(), bgImgView.getFitHeight());
		
		/* scaled meta */
		metaScaledCanvas.getGraphicsContext2D().clearRect(0, 0, metaScaledCanvas.getWidth(), metaScaledCanvas.getHeight());
		metaScaledCanvas.getGraphicsContext2D().drawImage(metaScaledImgView.getImage(), lastX, lastY, metaScaledImgView.getFitWidth(), metaScaledImgView.getFitHeight());
		
		/* un-scaled meta */
		metaCanvas.getGraphicsContext2D().clearRect(0, 0, metaCanvas.getWidth(), metaCanvas.getHeight());
		metaCanvas.getGraphicsContext2D().drawImage(metaImgView.getImage(), 0, 0, metaImgView.getFitWidth(), metaImgView.getFitHeight());
	}
	
	
	
	private void clipBorders() {
		 /* X */
        if (imgView.getFitWidth() + lastX < stageWidth) {
        	lastX = stageWidth - imgView.getFitWidth();
        }
        if (lastX > 0) {
        	lastX = 0;
        }
        /* Y */
        if (imgView.getFitHeight() + lastY + fxMenuPanel.getHeight() < stageHeight) {
        	lastY = stageHeight - imgView.getFitHeight() - fxMenuPanel.getHeight();
        }
        if (lastY > 0) {
        	lastY = 0;
        }
        recenter();
	}
	
	
	
	private void recenter() {
		if (imgView.getFitWidth() < stageWidth) {
        	lastX = (stageWidth - imgView.getFitWidth()) / 2;
        }
        if (imgView.getFitHeight() < stageHeight + fxMenuPanel.getHeight()) {
        	lastY = (stageHeight - fxMenuPanel.getHeight() - imgView.getFitHeight()) / 2;
        }
	}
	
	
	
	private void setListeners() {
		
		/* Set stage width and height to reflect always */
		
		stageWidth = ((BorderPane)(fxCanvas.getParent().getParent())).getWidth();
		stageHeight = ((BorderPane)(fxCanvas.getParent().getParent())).getHeight();
		((BorderPane)(fxCanvas.getParent().getParent())).widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
				stageWidth = ((BorderPane)(fxCanvas.getParent().getParent())).getWidth();
				
			}
		});
		((BorderPane)(fxCanvas.getParent().getParent())).heightProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
				stageHeight = ((BorderPane)(fxCanvas.getParent().getParent())).getHeight();
			}
		});
		
		
		/* Bind canvas to parenting HBOX + redraw */
		
		fxCanvasBox.widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
				fxCanvas.setWidth(fxCanvasBox.getWidth());
				fxCanvas.setLayoutX(fxCanvasBox.getLayoutX());
				drawMap();
			}
		});
		fxCanvasBox.heightProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
				fxCanvas.setHeight(fxCanvasBox.getHeight());
				fxCanvas.setLayoutY(fxCanvasBox.getLayoutY());
				drawMap();
			}
		});
	}
	
	
	private void scrollFrame() {
		final int SCROLL_AREA = 100;
		boolean moved = false;
		/* Check if within map picture */
		if (mouseX > lastX && mouseX <  lastX + imgView.getFitWidth()
				&& mouseY > fxMenuPanel.getHeight() && mouseY <  lastY + imgView.getFitHeight() + 50) {
			/* right side */
			if (mouseX > stageWidth - SCROLL_AREA) {
	    		lastX -= 0.1 * (mouseX - (stageWidth - SCROLL_AREA));
	    		moved = true;
	    	} 
		
			/* left side */
	    	if (mouseX < SCROLL_AREA) {
	    		lastX += 0.1 * (SCROLL_AREA - mouseX);
	    		moved = true;
	    	} 
	    	
	    	/* bottom side */
			if (mouseY > stageHeight - SCROLL_AREA) {
	    		lastY -= 0.1 * (mouseY - (stageHeight - SCROLL_AREA));
	    		moved = true;
	    	} 
			
			/* top side */
			if (mouseY < fxMenuPanel.getHeight() + SCROLL_AREA) {
	    		lastY += 0.1 * (fxMenuPanel.getHeight() + SCROLL_AREA - mouseY);
	    		moved = true;
	    	} 
	    	
	    	/* if anything changed redraw */
	    	if (moved) {
		    	clipBorders();
		        drawMap();
	    	}
		}
	}
	
	private void addZoomEvent() {
		fxCanvas.addEventFilter(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent event) { 
            	double widthBefore = imgView.getFitWidth();
            	double heightBefore = imgView.getFitHeight();
                if (event.getDeltaY() > 1) {
                	onMouseWheelUp();
                	lastX -= 0.5 *  (imgView.getFitWidth() - widthBefore);
                	lastY -= 0.5 *  (imgView.getFitHeight() - heightBefore);
                	
                } else if (event.getDeltaY() < 1) {
                	onMouseWheelDown();
                	lastX -= 0.5 * (imgView.getFitWidth() - widthBefore);
                	lastY -= 0.5 * (imgView.getFitHeight() - heightBefore);
                }
                
                /* Clip borders */
               clipBorders();
                drawMap();
                
            }
        });
	}
	
	private void addClickEvent() {
		fxCanvas.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) { 
            	if (mapGenerated && neighboursGenerated) {
            		/* Set up maps */
            		updateZoom();
            		drawMap();
            		double x = event.getX();
            		double y = event.getY();
            		Coordinate coord = new Coordinate((int)x,(int)y);
            		/* Get picked color from current view */
            		Province p = provinceList.get(getSnapshot(metaScaledCanvas).getPixelReader().getColor(coord.x, coord.y));
            		if (p != null) {
	            		/* If editing ports, add current province (if it doesn't exist yet) to the sea store */
	            		if (editingPorts) {
	            			if (!portData.get(currentPort).contains(p)) {
	            				portData.get(currentPort).add(p);
	            			} else {
	            				portData.get(currentPort).remove(p);
	            			}
	            			drawPortProvinces();
	            		} else { //not editing ports - just show neighbours
	            			LinkedList<Province> neighbours = p.getNeighbours();
	            			/* Fill backgrounds */
	            			
		            		fillArea(p.coord.x, p.coord.y, red, bgImgView.getImage(), bgCanvas);
		            		for (Province neighbour : neighbours) {
		            			fillArea(neighbour.coord.x, neighbour.coord.y, Color.GRAY, bgImgView.getImage(), bgCanvas);
		            		}
	            		}
	            		/* Swap */
	            		swapBuffers();
	            		clearBackground();
	            		drawMap();
	            		fxDebug.setText("Selected: " + p.id);
	            	}
            	}
            	
            }
		});
	}
	
	
	private void addScrollEvent() {
		Timer timer = new Timer();
		final TimerTask scrollTask = new TimerTask() {
			@Override
			public void run() {
				Platform.runLater(new Runnable(){
					@Override
					public void run() {
						scrollFrame();
					}
				});
				
			}
		};
		timer.scheduleAtFixedRate(scrollTask, 0, 10);
		
		fxCanvasBox.getParent().addEventFilter(MouseEvent.ANY, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
            	fxX.setText(String.valueOf(event.getX()));
            	fxY.setText(String.valueOf(event.getY()));
            	mouseX = event.getX();
            	mouseY = event.getY();
            	clipBorders();
                drawMap();
            }
        });
	}
}
