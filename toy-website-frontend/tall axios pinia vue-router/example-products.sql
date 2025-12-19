-- 示例产品数据 SQL 脚本
-- 用于后端项目的 data.sql 文件或直接执行

-- 注意：根据你的数据库类型调整语法
-- H2/PostgreSQL: CURRENT_TIMESTAMP
-- MySQL: NOW()

-- 示例产品数据
INSERT INTO products (name, description, price, category, stock, image_url, created_at) VALUES
('Teddy Bear', 'A soft and cuddly teddy bear perfect for children of all ages', 29.99, 'Stuffed Animals', 50, 'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400', CURRENT_TIMESTAMP),
('LEGO Classic Set', 'Building blocks for creative play and learning. 500 pieces included.', 49.99, 'Building Blocks', 30, 'https://images.unsplash.com/photo-1558618047-3c8c76ca7d13?w=400', CURRENT_TIMESTAMP),
('Remote Control Car', 'Electric remote control car with LED lights and rechargeable battery', 39.99, 'Electronics', 25, 'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400', CURRENT_TIMESTAMP),
('Jigsaw Puzzle', '1000 piece jigsaw puzzle featuring beautiful landscape for family fun', 19.99, 'Puzzles', 40, 'https://images.unsplash.com/photo-1606092195730-5d7b9af1efc5?w=400', CURRENT_TIMESTAMP),
('Superhero Action Figure', 'Detailed action figure with multiple accessories and poseable joints', 24.99, 'Action Figures', 35, 'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400', CURRENT_TIMESTAMP),
('Monopoly Board Game', 'Classic board game for 2-8 players. Perfect for family game night.', 34.99, 'Board Games', 20, 'https://images.unsplash.com/photo-1606092195730-5d7b9af1efc5?w=400', CURRENT_TIMESTAMP),
('Doll House', 'Beautiful wooden doll house with furniture and accessories included', 79.99, 'Dolls', 15, 'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400', CURRENT_TIMESTAMP),
('Art Supplies Set', 'Complete art set with crayons, markers, colored pencils, and sketchbook', 24.99, 'Arts & Crafts', 45, 'https://images.unsplash.com/photo-1606092195730-5d7b9af1efc5?w=400', CURRENT_TIMESTAMP),
('Musical Keyboard', 'Electronic keyboard with 61 keys and multiple sound effects', 89.99, 'Musical Toys', 12, 'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400', CURRENT_TIMESTAMP),
('Science Experiment Kit', 'Educational science kit with 50+ experiments for curious minds', 44.99, 'Educational', 28, 'https://images.unsplash.com/photo-1606092195730-5d7b9af1efc5?w=400', CURRENT_TIMESTAMP);

-- 如果表不存在，先创建表（仅用于参考，实际应该通过 JPA 实体类创建）
/*
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    category VARCHAR(100),
    stock INT NOT NULL DEFAULT 0,
    image_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
*/

