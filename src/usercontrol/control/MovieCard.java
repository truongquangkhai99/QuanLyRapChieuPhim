package usercontrol.control;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.Blob;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import Connector.Connector;
import Model.LoaiPhim;
import Model.Phim;
import Model.Phim_LoaiPhim;
import Model.KhachHang_Vote;
import controller.LoginController;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Text;
import plugin.AlertBox;
import plugin.AlertBox.MyButtonType;

public class MovieCard extends AnchorPane implements Initializable {	
	@FXML public ImageView image;
	@FXML public Text title;
	@FXML public FlowPane category;
	@FXML public FlowPane director;
	@FXML public Text length;
	@FXML public Text sumary;
	@FXML private BorderPane ratting;
	public RattingBar rattingBar = new RattingBar();
	public ContextMenu menu = new ContextMenu();
	public Phim phim=null;
	
	private int giaVe, soGhe, thoiLuong;
	private  LocalTime gioBatDau;
	
	public int getGiaVe() {
		return giaVe;
	}

	public void setGiaVe(int giaVe) {
		this.giaVe = giaVe;
	}

	public int getSoGhe() {
		return soGhe;
	}

	public void setSoGhe(int soGhe) {
		this.soGhe = soGhe;
	}

	public int getThoiLuong() {
		return thoiLuong;
	}

	public void setThoiLuong(int thoiLuong) {
		this.thoiLuong = thoiLuong;
	}

	public LocalTime getGioBatDau() {
		return gioBatDau;
	}

	public void setGioBatDau(LocalTime gioBatDau) {
		this.gioBatDau = gioBatDau;
	}

	public MovieCard() {
		super();
		
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../view/MovieCard.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);
		try {
			fxmlLoader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		image.setOnContextMenuRequested(e -> {
			menu.show(this, e.getScreenX(), e.getScreenY());
		});
	}
	
	public MovieCard(Phim p) {
		super();
		phim=p;
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../view/MovieCard.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);
		try {
			fxmlLoader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		image.setOnContextMenuRequested(e -> {
			menu.show(this, e.getScreenX(), e.getScreenY());
		});
		title.setText(" "+p.getTenPhim());
		Connector<Phim_LoaiPhim> c=new Connector<Phim_LoaiPhim>();
		Image img=c.convertToBufferImage(p.getHinhAnh());
		//image.setImage(img);
		Connector.setImage(image, img);
		Connector<LoaiPhim> cloai=new Connector<LoaiPhim>();
		List<LoaiPhim> loais=cloai.select(LoaiPhim.class, "select * from LOAIPHIM where MaLoai in(select MaLoai from PHIM_LOAIPHIM where MaPhim='"+p.getMaPhim()+"')");
		for(LoaiPhim lp:loais) {
			category.getChildren().add(new Label(lp.getTenLoai()+" "));
		}
		director.getChildren().add(new Label(" "+p.getTenDaoDien()));
		length.setText("Thời lượng: "+p.getThoiLuong());
		sumary.setText("Mô tả: "+p.getMota());
		image.setOnContextMenuRequested(e -> {
			menu.show(this, e.getScreenX(), e.getScreenY());
		});
		rattingBar.info.set("(" + p.getRating() + " - " + p.getNumberVote() + " vote)");
		rattingBar.ratting.set(p.getRating());
		rattingBar.vote.set(p.getNumberVote());
	}
	
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ratting.setCenter(rattingBar);
		
		for(int i=0;i<rattingBar.bar.length;i++) {
			ImageView imgv=rattingBar.bar[i];
			int j=i;
			imgv.setOnMouseClicked(e->{
				Connector<KhachHang_Vote> c=new Connector<KhachHang_Vote>();
				Connector<Phim>cp=new Connector<Phim>();
				List<Phim> ps=cp.selectPhim("select * from PHIM where MaPhim='"+phim.getMaPhim()+"'");
				ArrayList<KhachHang_Vote> kh_v=new ArrayList<KhachHang_Vote>();
				kh_v.addAll(c.select(KhachHang_Vote.class, "select * from KHACHHANG_VOTE where MaTaiKhoan='"+LoginController.taikhoan.getMaTaiKhoan()+"' and MaPhim='"+phim.getMaPhim()+"'"));
				float oldValue=0;
				if(kh_v.size()>0) {
					oldValue=kh_v.get(0).getVote();
				}
				int newValue=j+1;
				float rating=0;
				int numVote=0;
				if(ps.size()>0) {
					Phim currentPhim=ps.get(ps.size()-1);
					rating=currentPhim.getRating();
					numVote=currentPhim.getNumberVote();
				}
				if(kh_v.size()==0){
					numVote++;
					rating=(oldValue*numVote+newValue)/(numVote);
					cp.update("update PHIM set Rating='"+rating+"', NumberVote='"+numVote+"' where MaPhim='"+phim.getMaPhim()+"'");
					c.insert("insert into KHACHHANG_VOTE values('"+LoginController.taikhoan.getMaTaiKhoan()+"','"+phim.getMaPhim()+"','"+newValue+"')");
					rattingBar.info.set("(" + rating + " - " + numVote + " vote)");
					rattingBar.ratting.set(newValue);
					rattingBar.vote.set(numVote);
				}
				else {
					int vote=kh_v.get(kh_v.size()-1).getVote();
					rating=(rating*numVote+(newValue-vote))/numVote;
					cp.update("update PHIM set Rating='"+rating+"' where MaPhim='"+phim.getMaPhim()+"'");
					c.update("update KHACHHANG_VOTE set Vote='"+newValue+"' where MaTaiKhoan='"+LoginController.taikhoan.getMaTaiKhoan()+"' and MaPhim='"+phim.getMaPhim()+"'");
					rattingBar.info.set("(" + rating + " - " + numVote + " vote)");
					rattingBar.ratting.set(newValue);
					rattingBar.vote.set(numVote);
				}
			});
		}
	}
}
