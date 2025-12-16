package eirb.pg203;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ComponentList<T extends CalendarComponent> implements Iterable<T> {
  private List<T> components;

  // constructeurs
  public ComponentList() {
    this.components = new ArrayList<>();
  }
  public ComponentList(List<T> components) {
    this.components = new ArrayList<>(components);
  }

  // getter
  public List<T> getComponents() {
    return new ArrayList<>(components);
  }
  // opérations de base
  public void add(T component) {
    components.add(component);
  }
  public boolean remove(T component) {
    return components.remove(component);
  }
  public T get(int index) {
    return components.get(index);
  }
  public int size() {
    return components.size();
  }
  public boolean isEmpty() {
    return components.isEmpty();
  }
  public void clear() {
    components.clear();
  }

  // iterateur
  @Override
  public Iterator<T> iterator() {
    return components.iterator();
  }

  // méthode pour ajouter tous les éléments d'une autre liste
  public void addAll(ComponentList<T> otherList) {
    this.components.addAll(otherList.components);
  }

  // vérifier si la liste contient un élément
  public boolean contains(T component) {
    return components.contains(component);
  }

  // affichage selon le type
  public String toStringByType(String type) {
    String output = "";

    if (type.equals(CommandParser.OUTPUT_TEXT)) {
      for (T component : components) {
        output += component.toString() + "\n\n";
      }
    } else if (type.equals(CommandParser.OUTPUT_HTML)) {
      int i = 1;
      int j = 1;
      for (T component : components) {
        boolean isEvent = component instanceof Event;
        boolean isTodo = component instanceof Todo;

        String borderColor = isEvent ? "#3498db" : "#2ecc71";
        String backgroundColor = isEvent ? "#ecf5fb" : "#ecf9f1";

        output += String.format(
            "<div style=\"border-left:6px solid %s; padding:15px; margin-bottom:20px; "
                + "border-radius:8px; background-color:%s; box-shadow:0 2px 6px rgba(0,0,0,0.08); "
                + "font-family:Arial, sans-serif;\">\n",
            borderColor, backgroundColor);

        output += String.format("<h3 style=\"margin-top:0; color:%s;\">%s</h3>\n", borderColor,
            isEvent      ? "Event #" + i
                : isTodo ? "Todo #" + j
                         : "Component");

        output += String.format("<p style=\"line-height:1.6; color:#333;\">%s</p>\n",
            component.toString().replaceAll("\n{1,}", "<br>"));

        output += "</div>\n";

        if (isEvent) {
          i++;
        } else if (isTodo) {
          j++;
        }
      }
    } else {
      // partie ics ne sera pas traitée ici
      if (!type.equals(CommandParser.OUTPUT_ICS)) {
        throw new IllegalArgumentException("Unsupported type: " + type);
      }
    }
    return output;
  }

  // not sure si je vais les utiliser apres :

  // fonction pour compter les todos
  public int countTodos() {
    int count = 0;
    for (T component : components) {
      if (component instanceof Todo) {
        count++;
      }
    }
    return count;
  }

  // fonction de filtrage par type
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