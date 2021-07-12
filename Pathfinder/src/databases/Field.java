package databases;

public class Field {

	public int id, gsize, pcolor, tcolor, lcolor;
	public String mapName;
	
	public Field(int id, String mapNname, int gsize, int pcolor, int tcolor, int lcolor) {
		this.id = id;
		this.gsize = gsize;
		this.pcolor = pcolor;
		this.tcolor = tcolor;
		this.lcolor = lcolor;
		this.mapName = mapNname;
	}
	
	public Field() {
		
	}

}
