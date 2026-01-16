public class Villager implements Pnj {
    private final String name;
    private final Set<String> playersWhoTalked = Collections.synchronizedSet(new HashSet<>());

    // Position du PNJ dans le monde
    public int worldX, worldY;

    public Pnj(String name, int worldX, int worldY) {
        this.name = name;
        this.worldX = worldX;
        this.worldY = worldY;
        this.hasTalkedToPlayer = false; // Initialement, le joueur n'a pas parlé au PNJ
        this.taskCompleted = false; // Initialement, la tâche n'est pas accomplie
    }

    /**
     * Méthode publique appelée quand un joueur parle au PNJ.
     * Si c'est la première fois que ce joueur parle au PNJ, on appelle givingTask.
     * Sinon, on appelle defaultText.
     *
     * @param player le joueur qui parle
     * @param proximityRange la distance maximale pour parler au PNJ
     * @return le texte que le PNJ prononce ou null si le joueur est trop loin
     */
    public String speak(Player player, int proximityRange) {
        if (!isPlayerClose(player, proximityRange)) {
            return null; // Le joueur est trop loin pour parler au PNJ
        }

        if (!hasTalkedToPlayer) {
            hasTalkedToPlayer = true; // Marque que le joueur a parlé au PNJ
            return givingTask(player);
        } else {
            return defaultText(player);
        }
    }

    /**
     * Vérifie si le joueur est à proximité du PNJ.
     *
     * @param player le joueur
     * @param proximityRange la distance maximale pour être considéré comme proche
     * @return true si le joueur est proche, false sinon
     */
    public boolean isPlayerClose(Player player, int proximityRange) {
        int deltaX = Math.abs(player.worldX - this.worldX);
        int deltaY = Math.abs(player.worldY - this.worldY);
        return deltaX <= proximityRange && deltaY <= proximityRange;
    }

    /**
     * Texte unique (ou comportement) quand on donne la quête au joueur.
     * Ici on renvoie une chaîne, mais on peut étendre pour créer un objet Quest.
     */
    protected String givingTask(Player player) {
        return String.format(
            "%s: Bonjour ! J'ai besoin de ton aide. Va récupérer la perle du lac et reviens me voir.",
            name
        );
    }

    /**
     * Texte par défaut pour les conversations suivantes.
     */
    protected String defaultText(Player player) {
        return String.format(
            "%s: Oh, c'est encore toi. As-tu retrouvé la perle du lac ?",
            name
        );
    }

    protected String endTask(Player player) {
        return String.format(
            "%s: Félicitations ! Tu as accompli la tâche. Merci pour ton aide !",
            name
        );
    }
    /**
     * Marque la tâche comme accomplie.
     */
    public void completeTask() {
        this.taskCompleted = true;
    }
}