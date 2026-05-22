# restaurant-service

Catalog of restaurants and menu items. Owns search/filtering UX.

## Endpoints

| Method | Path                                          | Description                        |
|--------|-----------------------------------------------|------------------------------------|
| POST   | `/restaurants`                                | Create restaurant (ADMIN)          |
| GET    | `/restaurants`                                | List with paging + filters         |
| GET    | `/restaurants/{id}`                           | Restaurant detail with menu        |
| POST   | `/restaurants/{id}/menu-items`                | Add menu item (ADMIN)              |
| GET    | `/restaurants/search?q=&cuisine=&minRating=`  | Full search (paged, sorted)        |

## Data model

```
Restaurant
├── id, name, description, cuisine, address, rating, isOpen

MenuItem (JOINED inheritance)
├── id, restaurantId, name, priceCents, isAvailable
├── VegItem  (subclass)
└── NonVegItem (subclass — meatType, spiceLevel)
```

Inheritance is intentional: it directly exercises the *UUIDs & Inheritance* and *JPA Queries* curriculum days, and the menu domain has a natural taxonomy.

## Search implementation

- Spring Data `Specification<Restaurant>` composed from filter params.
- `Pageable` for paging + sorting (`name`, `rating`, `distance`).
- Index on `(cuisine, rating)`; trigram index on `name` for `q` ILIKE search.

## Events emitted

- None (read-mostly service; admin writes are sync REST).

## Port

`8002`
