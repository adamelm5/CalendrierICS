# Document de design
L'application est un parser et visualiseur de fichiers ICS (iCalendar) qui permet de filtrer et afficher des événements et tâches (todos) selon différents critères. L'architecture suit une approche modulaire avec une séparation claire des responsabilités.


## Schéma général

Flux de données typique :

- L'utilisateur exécute la commande avec des arguments spécifiques
- Main initialise le CommandParser pour interpréter ces arguments
- Selon le type demandé (events/todos), le flux est dirigé vers FilterIcsEvents ou FilterIcsTodos
- Ces classes filtrent le fichier ICS selon les critères (dates pour events, statuts pour todos)
- Les résultats (blocs ICS en texte) sont passés à outputs pour conversion en objets sauf si l'option demandée par l'utilisateur est -ics (inutil de convertir les blocs ics en objets pour reconvertir en ics)
- outputs utilise ComponentList pour gérer les collections et formater la sortie finale
- La sortie est générée dans le format spécifié (texte, HTML, ou ICS)

<img width="1868" height="3529" alt="deepseek_mermaid_20251216_0bca97(1)" src="https://github.com/user-attachments/assets/ffeca39f-89ce-4f4c-943c-166a8bc1df12" />


## Utilisation du polymorphisme

Le **polymorphisme** est utilisé à plusieurs niveaux dans l'application :

**1. Polymorphisme par héritage**

        // CalendarComponent définit une interface commune
        public abstract class CalendarComponent {
            public abstract String toString();
        }
        // Event et Todo implémentent différemment toString()
        public class Event extends CalendarComponent {
            @Override
            public String toString() {
                return String.format(" Start: %s\n  End: %s\n  Summary: %s...", ...);
            }
        }
        public class Todo extends CalendarComponent {
            @Override 
            public String toString() {
                return String.format(" Start: %s\n  Due: %s\n  Summary: %s...", ...);
            }
        }

**2. Polymorphisme dans ComponentList**

        public class ComponentList<T extends CalendarComponent> {
            // Peut traiter indifféremment Events et Todos
            public String toStringByType(String type) {
                for (T component : components) {
                    boolean isEvent = component instanceof Event;
                    boolean isTodo = component instanceof Todo;
                    // Traitement polymorphique selon le type réel
                }
            }
        }

**3. Méthodes polymorphiques**

        // getType() est redéfini dans les sous-classes
        public class CalendarComponent {
            public String getType() { return "generic"; }
        }
        public class Event extends CalendarComponent {
            @Override
            public String getType() { return "events"; }
        }
        public class Todo extends CalendarComponent {
            @Override
            public String getType() { return "todos"; }
        }

## Utilisation de la déléguation



## Utilisation de l'héritage

Hiérarchie d'héritage principale :

                CalendarComponent (abstraite)
                /                            \
      (concrète) Event                   Todo (concrète)

CalendarComponent est une classe **abstraite** qui définit les attributs communs à tous les composants (UID, summary, location, etc.), elle fournit des implémentations par défaut pour les getters et déclare la méthode abstraite toString().

Event et Todo **héritent** de CalendarComponent, elles ajoutent leurs attributs spécifiques et setters pour ceux modifiables, implémentent toString() pour les tâches à venir et redéfinissent getType()

De cette façon, les classes Event et Todo ont été factorisées, et n'importe quel changement dans l'un des champs communs des deux classes necessitera moins d'efforts.

## Utilisation de la généricité

ComponentList est le meilleur exemple de **généricité** dans le projet :

        public class ComponentList<T extends CalendarComponent> implements Iterable<T> {
            private List<T> components;
            public void add(T component) {
                components.add(component);
            }   
            public T get(int index) {
                return components.get(index);
            }
        }

**Utilisation dans la classe :**

Cette classe est principalement utilisée dans la classe outputs qui est résponsable de la transformation de la chaine de la liste de la chaines de caractère de blocs EVENT et TODOS en une seule chaine de caractère qui sera retourné par le programm dans le format demandé. Cette conversion se fait en suivant ces étapes :
1.   CalendarComponent icsToCalendarComponent(String icsString, String componentType); 
se charge de la conversions d'un bloc "BEGIN:TODO|EVENT ..... END:TODO|EVENT" en objet correspondant
2.   public ComponentList<CalendarComponent> icsListToCalendarComponentsList(List<String> icsStrings);
refait ceci pour tous les éléments figurant dans la liste de ces blocs et stoque ces objets dans la ComponentList
3.   Dans la classe ComponentList, une méthode toStringByType() a été définie, elle permet la conversion de cette liste en chaine de caractère regroupant l'intégralité des informations qu'elle contient, selon le format mentionné. C'est exactement ce qui a été implémenté dans la méthode  
 public ComponentList<CalendarComponent> icsListToCalendarComponentsList(List<String> icsStrings);

Selon les exigences du projet : on ne peut que extraire une seule catégorie de CalendarComponent, soit Event, soit Todo. On aura donc besoin que d'une liste du type spécific demandé, une solution possible était de distinguer entre les deux cas dans le main et créer une liste de Todo ou de Events seulement, mais cette classe nous a permis de factoriser tout cela sans besoin à vérifier le type des éléments attendus.

Cette façon de faire sera plus fléxible pour des changements à venir, elle permet de travailler avec Event, Todo, ou tout autre futur sous-type de CalendarComponent, elle permettera aussi en théorie à l'utilisateur de choisir plusieurs catégories (une liste de Todos et Events plannifiés pour une période précise par exemple), ensuite les filtrer selon leur type pour un affichage organisé, en uilisant cette méthode par exemple :

        public class ComponentList<T extends CalendarComponent> {
            public ComponentList<T> filterByType(Class<?> typeClass) {
                ComponentList<T> filteredList = new ComponentList<>();
                for (T component : components) {
                    if (typeClass.isInstance(component)) {
                        filteredList.add(component);
                    }
                }
                return filteredList;
            }
        }

## Utilisation des exceptions

**1. Validation des arguments avec IllegalArgumentException** 

**2. Validation des formats de date**

**3. Gestion des erreurs de fichier**

Dans les fichiers FilterIcs responsables de la lecture du fichier et du filtrage des blocs "BEGIN: .... END: ....", la lecture de fichiers ics est exigée, une gestion des erreurs de lecture est donc indispensable. Exemple :

        public class FilterIcsEvents {
            public static List<String> extractTodayEventsICS(String date, String filePath) {
                try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                    ...
                } catch (IOException e) {
                    System.err.println("Erreur: " + e.getMessage());
                    return new ArrayList<>();
                }
            }
        }

**4. Validation des options invalides**

exemple:

        public class outputs {
            public String generateOutput(List<String> icsStrings, String outputType) {
                if (outputType.equals(CommandParser.OUTPUT_TEXT)) {
                    ...
                } else if (outputType.equals(CommandParser.OUTPUT_HTML)) {
                    ...
                } else if (outputType.equals(CommandParser.OUTPUT_HTML)) {
                    ...
                } else {
                    throw new IllegalArgumentException("Unsupported output type: " + outputType);
                }
            }
        }

Cette exemple montre l'aspect du programme ou les seuls arguments qui seront accéptés comme type de sortie sont OUTPUT_TEXT, OUTPUT_HTML et OUTPUT_HTML, n'importe quelle autre argument sera rejeté.

**5. Classes utilitaires non instanciables**

exemple :

        public class DateUtils {
            DateUtils() throws UnsupportedOperationException {
                throw new UnsupportedOperationException("dateUtils is an utility class only");
            }
            ...
        }

Cette exception prévient le développeur d'avoir créé un objet dont son allouage ne rapporte rien; ce types de classes sont de type utilitaire seulement, elles comportent généralement des méthodes statiques pour un usage libre dans les autres classes
