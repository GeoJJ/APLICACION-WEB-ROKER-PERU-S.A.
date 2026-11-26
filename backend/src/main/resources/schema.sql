-- Compatibilidad con bases anteriores a las ventas presenciales de Andrea.
-- Un pedido presencial usa id_cliente_presencial; el cliente registrado es opcional.
-- Hibernate update no retira necesariamente el NOT NULL de una tabla existente.
ALTER TABLE IF EXISTS pedidos ALTER COLUMN id_cliente DROP NOT NULL;
