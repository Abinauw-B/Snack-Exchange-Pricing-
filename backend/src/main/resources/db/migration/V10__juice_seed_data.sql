-- Seed data for 7 Juice Flavours and Initial 20L Active Batches

INSERT INTO products (id, name, flavour, description, default_cup_size_ml, default_cup_price, current_cup_price, min_cup_price, max_cup_price) VALUES
(1, 'Fresh Mango Juice', 'MANGO', 'Sweet fresh Alphanso mango pulp juice', 250, 20.00, 20.00, 18.00, 25.00),
(2, 'Zesty Lemon Juice', 'LEMON', 'Refreshing squeezed lemonade with mint touch', 250, 20.00, 20.00, 18.00, 25.00),
(3, 'Cool Mint Cooler', 'MINT', 'Chilled mint and lime mocktail blend', 250, 20.00, 20.00, 18.00, 25.00),
(4, 'Orange Sunrise', 'ORANGE', 'Pure Valencia orange juice loaded with vitamin C', 250, 20.00, 20.00, 18.00, 25.00),
(5, 'Strawberry Delight', 'STRAWBERRY', 'Fresh strawberry nectar crush', 250, 20.00, 20.00, 18.00, 25.00),
(6, 'Royal Grape Juice', 'GRAPE', 'Rich black grape extract cooler', 250, 20.00, 20.00, 18.00, 25.00),
(7, 'Lychee Mist', 'LYCHEE', 'Exotic lychee fruit punch', 250, 20.00, 20.00, 18.00, 25.00);

-- Initial 20L Active Batches (20,000 ml each = 80 cups of 250ml)
INSERT INTO juice_batches (product_id, batch_code, container_capacity_ml, initial_volume_ml, remaining_volume_ml, cup_size_ml, status) VALUES
(1, 'BATCH-MNG-001', 20000, 20000, 20000, 250, 'ACTIVE'),
(2, 'BATCH-LMN-001', 20000, 20000, 20000, 250, 'ACTIVE'),
(3, 'BATCH-MNT-001', 20000, 20000, 20000, 250, 'ACTIVE'),
(4, 'BATCH-ORG-001', 20000, 20000, 20000, 250, 'ACTIVE'),
(5, 'BATCH-STR-001', 20000, 20000, 20000, 250, 'ACTIVE'),
(6, 'BATCH-GRP-001', 20000, 20000, 20000, 250, 'ACTIVE'),
(7, 'BATCH-LYC-001', 20000, 20000, 20000, 250, 'ACTIVE');
