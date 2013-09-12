Sling Resource Merger
=====================

Goal of this repository is to provide services to get "merged" resources, depending either on resource resolver search path or sling:resourceSuperType hierarchy.

Proposed architecture
---------------------

The way we would like to solve those issues is:
* Only put under /apps what's really needed
* Use a merged resource provider to request resources using /merge/... instead of /apps/... or /libs/...
* That merged resource would be an aggregate of resources and properties from the corresponding search paths defined in the ResourceResolver

Magic properties
----------------
* sling:hideProperties (String or String[]): contains the list of properties to hide.
  * Wildcard * can be used to hide all properties
* sling:hideNodes (String or String[]): contains the list of child nodes to hide.
  * Wildcard * can be used to hide all child nodes
* sling:orderBefore (String): contains the name of the sibling node where the current node has to be moved before.

Use cases
---------

(A) How to add/override one or more properties

* Create the matching property within /apps
* The property will have priority based on Sling Resource Resolver configuration

Changing the property type is supported. This means if you use a different property type than the one under /libs, your property type will be used.

(B) How to override one or more auto-created properties

By default, auto-created properties like jcr:primaryType will not be overlaid as you might not be able to respect the node type currently under /libs. You will have to explicitly hide that property and redine it to change such a property.

* Create the node under /apps with the desired jcr:primaryType
* sling:hideProperties = jcr:primaryType
* In this case, the property defined under /apps will have the priority on the one defined under /libs

(C) How to override a node and its children

This can be achieved by combining (A) and (F)

(D) How to delete one or more properties

* Create a property of type String or String~[]: sling:hideProperties and define the properties to delete
* Examples:
  * *
  * ["*"]
  * jcr:title
  * ["jcr:title", "jcr:description"]

(E) How to delete a node (and its children)

* Create the corresponding node under /apps
* Create a property of type String: sling:hideProperties = *
* Create a property of type String: sling:hideNodes = *

(F) How to delete children of a node (but keep the properties of the node)

* Create the corresponding node under /apps
* Create a property of type String: sling:hideNodes = *

(G) How to reorder nodes

There are two ways of reordering a node:

(G1) By deleting children and recreating them
* Create the corresponding parent node under /apps
* Create a property of type String: sling:hideNodes = *
* Recreate each child nodes in the desired order
  * Child nodes that have not been created will be considered as deleted
  * Properties of the child nodes will be inherited from /libs (unless there are specific overriding instructions)
  * You directly see under /apps the new ordered children list
  * If you have a lot of child nodes, you'll have to create a lot of them. Copying them from /libs might me a workaround, but you should clean all their properties to ensure getting latest content when upgrading /libs through a patch
  * If a new child node comes to /libs through an update, it will be considered as deleted until you replicate the change to /apps

(G2) By using the sling:orderBefore property:
* Create the corresponding node under /apps
* Create a property of type String: sling:orderBefore = previousSiblingName
  * If you quickly need to reorder one node, the effort is minimal
  * Looks more like a diff: if a new child node comes to /libs through an update, there is nothing to change in /apps
  * There is no direct view of what would be the final result
