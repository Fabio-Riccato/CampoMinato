import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CampoMinato implements ActionListener {
    static int righe = 14;
    static int colonne = 18;
    static Casella[][] caselle = new Casella[righe][colonne];
    static JTextField jT;
    static JTextField countBand;
    public static int bandierine = 40;
    public static boolean end = false;

    JFrame frame;

    ImageIcon iconaChiara, iconaScura;
    ImageIcon iconaHover;
    ImageIcon iconaClick;
    ImageIcon iconaClickChiara;
    ImageIcon iconaBomba;

    public static void main(String[] args) {
        new CampoMinato();
    }

    public CampoMinato() {
        int larg = 1200;
        int alt = 1200;
        JFrame f = new JFrame("Campo Minato");
        f.setBounds(100, 100, larg, alt);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setLayout(null);
        this.frame = f;

        JPanel p = new JPanel();
        p.setBounds(70, 0, larg - 150, alt - 150);
        p.setLayout(new GridLayout(righe, colonne));

        int larghezzaCasella = (larg - 150) / colonne;
        int altezzaCasella = (alt - 150) / righe;

        // Carica immagini
        iconaChiara = scalaIcona("campoMinato/src/Immagini/campoC.jpeg", larghezzaCasella, altezzaCasella);
        iconaScura = scalaIcona("campoMinato/src/immagini/campo.png", larghezzaCasella, altezzaCasella);
        iconaHover = scalaIcona("campoMinato/src/immagini/campoC+.jpeg", larghezzaCasella, altezzaCasella);
        iconaClick = scalaIcona("campoMinato/src/immagini/cliccato.png", larghezzaCasella, altezzaCasella);
        iconaClickChiara = scalaIcona("campoMinato/src/immagini/cliccatoC.PNG", larghezzaCasella, altezzaCasella);
        iconaBomba = scalaIcona("campoMinato/src/immagini/corvo.jpeg", larghezzaCasella, altezzaCasella);

        for (int k = 0; k < righe; k++) {
            for (int j = 0; j < colonne; j++) {
                boolean casellaChiara = (k + j) % 2 == 0;
                ImageIcon iconaCasellaNormale = casellaChiara ? iconaChiara : iconaScura;
                Casella casella = new Casella(false, 0, iconaCasellaNormale, iconaHover, scalaIcona("campoMinato/src/immagini/Bandiera.jpeg", larghezzaCasella, altezzaCasella));
                casella.setChiara(casellaChiara);
                casella.addActionListener(this);
                p.add(casella);
                caselle[k][j] = casella;
            }
        }

        inserisciBombe();
        leggiVicine();

        f.add(p);

        jT = new JTextField();
        jT.setBounds(100, alt - 130, 200, 40);
        f.add(jT);
        countBand = new JTextField("Bandierine disponibili: "+bandierine);
        countBand.setBounds(350, alt - 130, 200, 40);
        countBand.setForeground(Color.RED);
        countBand.setFont(new Font("Arial",Font.BOLD, 15));
        f.add(countBand);

        f.setVisible(true);
    }

    public static void dimBand(){
        if(bandierine > 0){
            bandierine--;
            countBand.setText("Bandierine disponibili: "+bandierine);
        }
    }

    public static void aumentaBand(){
        if(bandierine < 40){
            bandierine++;
            countBand.setText("Bandierine disponibili: "+bandierine);
        }
    }

    public void inserisciBombe() {
        int riga, colonna;
        for (int i = 0; i < 40; i++) {
            do {
                riga = (int) (Math.random() * righe);
                colonna = (int) (Math.random() * colonne);
            } while (caselle[riga][colonna].isBomba());
            caselle[riga][colonna].setBomba(true);
        }
    }

    private ImageIcon scalaIcona(String path, int larghezza, int altezza) {
        ImageIcon icona = new ImageIcon(path);
        Image img = icona.getImage().getScaledInstance(larghezza, altezza, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object sorgente = e.getSource();

        for (int k = 0; k < righe; k++) {
            for (int j = 0; j < colonne; j++) {
                Casella c = caselle[k][j];
                if (sorgente.equals(c) && !c.isCliccata() && !c.isBandierina()) {
                    jT.setText("Riga: " + k + "    Colonna: " + j);
                    if (c.isBomba()) {
                        c.setIcon(iconaBomba);
                        end = true;
                        setBombe();
                    } else {
                        if (vittoria()) {
                            vinto();
                            end = true;
                        }
                        scopriZonaVuota(k, j);
                    }
                    return;
                }
            }
        }
    }

    public void vinto(){
        ImageIcon icon = scalaIcona("campoMinato/src/immagini/gg.png", 600, 300);
        JLabel labelImmagine = new JLabel(icon);
        labelImmagine.setBounds((frame.getWidth() - 600) / 2, (frame.getHeight() - 300) / 2, 600, 300);
        JLabel labelTesto = new JLabel("Hai vinto!!!", SwingConstants.CENTER);
        labelTesto.setBounds((frame.getWidth() - 600) / 2, (frame.getHeight() - 300) / 2 + 310, 600, 50);
        labelTesto.setFont(new Font("Jokerman", Font.BOLD, 36));
        labelTesto.setForeground(Color.black);
        frame.getLayeredPane().add(labelImmagine, JLayeredPane.POPUP_LAYER);
        frame.getLayeredPane().add(labelTesto, JLayeredPane.POPUP_LAYER);
        labelImmagine.setVisible(true);
        labelTesto.setVisible(true);
        labelTesto.setOpaque(true);

        Timer timer = new Timer(5000, e -> System.exit(0));
        timer.setRepeats(false);
        timer.start();
    }

    private void scopriZonaVuota(int riga, int colonna) {
        if (riga < 0 || riga >= righe || colonna < 0 || colonna >= colonne) return;

        Casella c = caselle[riga][colonna];
        if (c.isCliccata()) return;

        c.setCliccata(true);
        c.removeActionListener(this);

        if (c.getVicino() == 0) {
            if (c.isChiara()) {
                c.setIcon(iconaClickChiara);
            } else {
                c.setIcon(iconaClick);
            }

            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (i != 0 || j != 0) {
                        scopriZonaVuota(riga + i, colonna + j);
                    }
                }
            }
        } else {
            int numero = c.getVicino();
            String path = "campoMinato/src/immagini/cliccato" + numero + ".PNG";
            c.setIcon(scalaIcona(path, (1200 - 150) / colonne, (1200 - 150) / righe));
        }
    }

    public boolean vittoria(){
        for (int i = 0; i < righe; i++) {
            for (int j = 0; j < colonne; j++) {
                if (!caselle[i][j].isBomba() && !caselle[i][j].isCliccata()) {
                    return false;
                }
            }
        }
        return true;
    }

    public void setBombe() {
        for (int i = 0; i < righe; i++) {
            for (int j = 0; j < colonne; j++) {
                if (caselle[i][j].isBomba() && !caselle[i][j].isBandierina()) {
                    caselle[i][j].setIcon(iconaBomba);
                    caselle[i][j].setCliccata(true);
                }
                caselle[i][j].removeActionListener(this);
            }
        }

        ImageIcon icon = scalaIcona("campoMinato/src/immagini/haiPerso.png", 600, 300);
        JLabel labelImmagine = new JLabel(icon);
        labelImmagine.setBounds((frame.getWidth() - 600) / 2, (frame.getHeight() - 300) / 2, 600, 300);
        JLabel labelTesto = new JLabel("Hai perso!!!", SwingConstants.CENTER);
        labelTesto.setBounds((frame.getWidth() - 600) / 2, (frame.getHeight() - 300) / 2 + 310, 600, 50);
        labelTesto.setFont(new Font("Jokerman", Font.BOLD, 36));
        labelTesto.setForeground(Color.black);
        frame.getLayeredPane().add(labelImmagine, JLayeredPane.POPUP_LAYER);
        frame.getLayeredPane().add(labelTesto, JLayeredPane.POPUP_LAYER);
        labelImmagine.setVisible(true);
        labelTesto.setVisible(true);
        labelTesto.setOpaque(true);

        Timer timer = new Timer(5000, e -> System.exit(0));
        timer.setRepeats(false);
        timer.start();
    }

    public void leggiVicine() {
        for (int i = 0; i < righe; i++) {
            for (int j = 0; j < colonne; j++) {
                caselle[i][j].setVicino(contaBombeVicini(i, j));
            }
        }
    }

    private int contaBombeVicini(int riga, int colonna) {
        int contatore = 0;
        for (int i = riga - 1; i <= riga + 1; i++) {
            for (int j = colonna - 1; j <= colonna + 1; j++) {
                if (i >= 0 && i < righe && j >= 0 && j < colonne && !(i == riga && j == colonna)) {
                    if (caselle[i][j].isBomba()) {
                        contatore++;
                    }
                }
            }
        }
        return contatore;
    }
}



