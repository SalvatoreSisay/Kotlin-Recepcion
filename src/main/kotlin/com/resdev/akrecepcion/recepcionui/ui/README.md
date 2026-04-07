# ui

Capa UI (JavaFX):
- Controllers (MVC) y helpers de render.
- Sin SQL aqui. La UI habla con Repositories/Services.

Nota: los controllers actuales aun viven en el paquete raiz para no romper FXML.
Cuando queramos refactor, hay que actualizar `fx:controller` en cada FXML y `module-info.java` (opens).

