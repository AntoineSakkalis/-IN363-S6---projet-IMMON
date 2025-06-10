package fr.insa.reseau.common;

/**
 * Représentation la plus simple possible d’une trame.
 * Chaque champ est PUBLIC pour qu’on puisse y accéder directement

 *
 * Format d’échange choisi : une ligne texte avec des « ; » comme séparateurs.
 */
public class Trame {

    /* --------- les données de la trame (toutes publiques) --------- */
    public int    type;          // 0 = DATA, 1 = ACK, … (à définir dans l’énum plus tard)
    public String ipServerSrc;
    public String ipServerDst;
    public int idClientSrc;
    public int idClientDst;
    public String data;          // chaîne de 0 et 1 déjà compressée + parité

    /* --------- transforme la trame en une seule ligne texte --------- */
    public String toLine() {
        return  type + ";" +
        		idClientSrc + ";" +
        		idClientDst + ";" +
        		ipServerSrc + ";" +
                ipServerDst + ";" +
                data;
    }

    /* --------- recrée une trame depuis la ligne texte --------- */
    public static Trame fromLine(String line) {
        String[] parts = line.split(";", -1);   // -1  => on garde les champs vides éventuels
        Trame t    = new Trame();
        t.type     = Integer.parseInt(parts[0]);
        t.idClientSrc    = Integer.parseInt(parts[1]);
        t.idClientDst    = Integer.parseInt(parts[2]);
        t.ipServerSrc    = parts[3];
        t.ipServerDst    = parts[4];
        t.data           = parts[5];
        return t;
    }

}
