# 1. Einarbeitung und Vorbereitung

a. Arbeiten Sie das Kubernetes Tutorial [Kub21a] durch und machen Sie sich mit den Grundlagen von Kubernetes (k8s) vertraut.

b. *Empfohlen*: sichten Sie Kapitel 17 (Microservice "Rezept" für k8s) in [Wol18].

c. Erstellen Sie ein eigenes k8s-Cluster mit Minikube [Kub21c] (docker-desktop und VS Code Remote Containers bieten auch welche).

# 2. PetClinic-Backend V2 (k8s-basic)

a. **Zusammenfassung**: Suchen Sie sich einen k8s-Cluster aus und erstellen Sie eine k8s-Konfiguration, um Ihr PetClinic-Backend zu implementieren und zu testen.

b. **Grundlage**: Bringen Sie die PetClinic Kubernetes Demo [SPC25c] zum Laufen und untersuchen Sie das System.

c. **Microservices**: Implementieren Sie ihre Microservices auf Basis bestehender Container mit k8s-Deployments & -Services. Nutzen Sie k8s Service Discovery bei der Kommunikation zwischen Microservices (statt Eureka).

d. **Datenbank**: Implementieren Sie die mysql Datenbank als k8s Stateful Application mit einem *PersistentVolume* [Kub21b]. Nutzen sie dazu die Realisierung mit Helm, wie in der Demo gezeigt.

e. **Experiment**: Skalieren Sie einen Microservice auf 3 Pods und machen Sie das resultierende Load Balancing im Response sichtbar.

f. **Experiment**: Löschen Sie den einzelnen Pod des anderen Microservice. Was passiert?

# 3. PetClinic-Backend V3 (k8s-istio)

a. *Optional*: sichten Sie die Istio Einführung [WP20].

b. **Grundlage**: Bringen Sie die PetClinic Istio Demo [SPC25d] zum Laufen und untersuchen Sie das System. Es gibt auch ein Video dazu [Sue24].

c. **Migration**: Erstellen Sie nach dem Beispiel der PetClinic Istio Demo [SPC25d] ein Repository für ihre PetClinic Erweiterung aus der 2. Aufgabe.

d. **Experiment**: Beobachten Sie einige Aufrufe Ihrer Microservices mit *Prometheus* und *Grafana*.

e. **Experiment**: Lassen Sie sich mit *Jaeger* die Traces von Service Aufrufen zeigen.

# Abgabe: Das Deliverable umfasst:

Die beiden Lösungen V2/V3 für PetClinic-Backends sind prinzipiell unabhängig voneinander zu bearbeiten, beide in der gezeigten Reihenfolge zu bearbeiten. Zum Bestehen muss **mindestens eine Version** mit den Experimenten realisiert sein.

1. Erweiterung des Reports um **Skizzen** und **Screenshots** der Lösungen (PDF)

2. **Git Repo** der Lösung mit **Readme**.
