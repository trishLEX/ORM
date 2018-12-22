package GenService.Postgres.Math.Tables;

import ru.bmstu.ORM.Service.ColumnAnnotations.*;
import ru.bmstu.ORM.Service.Tables.Entity;

import java.util.Objects;

@Table(db = "math", schema = "public", name = "powers")
public class Powers extends Entity {
	@PK
	@Column(name = "x", nullable = false)
	private Integer x;

	@PK
	@Column(name = "square", nullable = false)
	private Integer square;

	@PK
	@Column(name = "cube", nullable = false)
	private Integer cube;

	public Integer getX() {
		return this.x;
	}

	public void setX(Integer x) {
		this.x = x;
	}

	public Integer getSquare() {
		return this.square;
	}

	public void setSquare(Integer square) {
		this.square = square;
	}

	public Integer getCube() {
		return this.cube;
	}

	public void setCube(Integer cube) {
		this.cube = cube;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (this.getClass() != obj.getClass())
			return false;

		Powers other = (Powers) obj;
		return Objects.equals(this.x, other.x) && Objects.equals(this.square, other.square) && Objects.equals(this.cube, other.cube);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.x, this.square, this.cube);
	}

	@Override
	public String toString() {
		return "Powers {\n" +
			"\tx: " + this.x + "\n" +
			"\tsquare: " + this.square + "\n" +
			"\tcube: " + this.cube + "\n" +
			"}";
	}
}