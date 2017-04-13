# Lattice Miner v2.0

> **Lattice Miner 2.0** is a data mining prototype developed by the LARIM research laboratory at _Université du Québec en Outaouais_ under the supervision of Professor Rokia Missaoui.  It allows the generation of _clusters_ (called formal concepts) and _association rules_ (including logical implications) given a binary relation between a collection of objects (or individuals) and a set of attributes (or properties).  The focus of Lattice Miner is on pattern (knowledge) _discovery_ _visualization_, _exploration_ and _approximation_ through a lattice representation of either a flat or a nested structure.

> Lattice Miner is a public-domain Java platform whose main functions include all low-level operations and structures for the representation and manipulation of input data, lattices and association rules. The interface offers a context editor and concept lattice manipulator to assist the user in a set of interactive and targeted data mining tasks. The architecture of Lattice Miner is open and modular enough to allow the integration of new features and facilities.

> The latest release called Lattice Miner 2.0 of April 2017 includes the computation of implications with _negation_, as well as _triadic_ (tridimensional) implication calculation.

> For more information about the tool, please contact Professor Rokia Missaoui rokia.missaoui@uqo.ca.

## Latest release

You can download the latest release from [here](https://github.com/LarimUQO/lattice-miner/releases/latest).

Sample of binary and triadic context files are in the the [_files_]](https://github.com/LarimUQO/lattice-miner/tree/master/files) folder.

## Installation from Source code

Clone the repository with:

```
git clone https://github.com/LarimUQO/lattice-miner.git
```

This project has a Maven pom file. Open it with your favorite Java IDE.

After building the project with `mvn install` you can run it with:

```
java -jar target/lattice-miner-2.0-beta-1.jar
```

## Instructions for Eclipse IDE

- On Eclipse you can import the project using  `File > Import... > Maven > Existing Maven Projects` and browse to the `pom.xml` file.

- Then right click on the project from the Package Explorer and select `Run As > 7 Maven install`. This should create a new build.


Laboratoire LARIM (http://larim.uqo.ca/) - Université du Québec en Outaouais (UQO) - Canada

&copy; 2017 All Rights Reserved.
